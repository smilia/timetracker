package com.timetracker.app.service.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.timetracker.app.MainActivity
import com.timetracker.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 番茄钟服务 - 前台服务显示倒计时通知
 */
class PomodoroService : Service() {

    companion object {
        const val CHANNEL_ID = "pomodoro_service_channel_v2"
        const val NOTIFICATION_ID = 4001
        const val ACTION_START = "com.timetracker.app.POMODORO_START"
        const val ACTION_PAUSE = "com.timetracker.app.POMODORO_PAUSE"
        const val ACTION_RESUME = "com.timetracker.app.POMODORO_RESUME"
        const val ACTION_STOP = "com.timetracker.app.POMODORO_STOP"
        const val ACTION_SKIP = "com.timetracker.app.POMODORO_SKIP"
        
        const val EXTRA_TITLE = "title"
        const val EXTRA_CYCLES = "cycles"
        const val EXTRA_CURRENT_CYCLE = "current_cycle"
        const val EXTRA_IS_WORK = "is_work"
        
        // 工作时间 25 分钟
        const val WORK_DURATION_MINUTES = 25
        // 休息时间 5 分钟
        const val BREAK_DURATION_MINUTES = 5
        
        fun startPomodoro(context: Context, title: String, cycles: Int) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_CYCLES, cycles)
                putExtra(EXTRA_CURRENT_CYCLE, 1)
                putExtra(EXTRA_IS_WORK, true)
            }
            context.startService(intent)
        }
        
        fun pausePomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }
        
        fun resumePomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }
        
        fun stopPomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
        
        fun skipPomodoro(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_SKIP
            }
            context.startService(intent)
        }
    }
    
    // 服务状态
    sealed class PomodoroState {
        object Idle : PomodoroState()
        data class Running(
            val title: String,
            val cycles: Int,
            val currentCycle: Int,
            val isWork: Boolean,
            val remainingSeconds: Int,
            val totalSeconds: Int
        ) : PomodoroState()
        data class Paused(
            val title: String,
            val cycles: Int,
            val currentCycle: Int,
            val isWork: Boolean,
            val remainingSeconds: Int,
            val totalSeconds: Int
        ) : PomodoroState()
    }
    
    private val _state = MutableStateFlow<PomodoroState>(PomodoroState.Idle)
    val state: StateFlow<PomodoroState> = _state
    
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    private var wakeLock: PowerManager.WakeLock? = null
    
    private var currentTitle = ""
    private var totalCycles = 1
    private var currentCycle = 1
    private var isWorkPhase = true
    private var remainingSeconds = WORK_DURATION_MINUTES * 60
    private var totalSeconds = WORK_DURATION_MINUTES * 60
    private var isPaused = false
    
    inner class LocalBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }
    
    private val binder = LocalBinder()
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("PomodoroService", "onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: "番茄钟"
                totalCycles = intent.getIntExtra(EXTRA_CYCLES, 1)
                currentCycle = intent.getIntExtra(EXTRA_CURRENT_CYCLE, 1)
                isWorkPhase = intent.getBooleanExtra(EXTRA_IS_WORK, true)
                remainingSeconds = if (isWorkPhase) WORK_DURATION_MINUTES * 60 else BREAK_DURATION_MINUTES * 60
                totalSeconds = remainingSeconds
                isPaused = false
                android.util.Log.d("PomodoroService", "Starting pomodoro: title=$currentTitle, cycles=$totalCycles")
                startTimer()
            }
            ACTION_PAUSE -> {
                android.util.Log.d("PomodoroService", "Pausing pomodoro")
                pauseTimer()
            }
            ACTION_RESUME -> {
                android.util.Log.d("PomodoroService", "Resuming pomodoro")
                resumeTimer()
            }
            ACTION_STOP -> {
                android.util.Log.d("PomodoroService", "Stopping pomodoro")
                stopTimer()
            }
            ACTION_SKIP -> {
                android.util.Log.d("PomodoroService", "Skipping phase")
                skipCurrentPhase()
            }
        }
        return START_STICKY
    }
    
    private fun startTimer() {
        android.util.Log.d("PomodoroService", "startTimer: remainingSeconds=$remainingSeconds, isWorkPhase=$isWorkPhase")
        updateState()
        val notification = createNotification()
        android.util.Log.d("PomodoroService", "Starting foreground with notification")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        // 获取唤醒锁确保计时准确
        acquireWakeLock()
        
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (remainingSeconds > 0) {
                if (!isPaused) {
                    delay(1000)
                    remainingSeconds--
                    updateState()
                    // 每5秒更新一次通知，减少电量消耗
                    if (remainingSeconds % 5 == 0 || remainingSeconds <= 10) {
                        updateNotification()
                    }
                } else {
                    delay(500) // 暂停时降低检查频率
                }
            }
            // 时间到
            onPhaseComplete()
        }
    }
    
    private fun pauseTimer() {
        isPaused = true
        updateState()
        updateNotification()
    }
    
    private fun resumeTimer() {
        isPaused = false
        updateState()
        updateNotification()
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        _state.value = PomodoroState.Idle
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun skipCurrentPhase() {
        timerJob?.cancel()
        onPhaseComplete()
    }
    
    private fun onPhaseComplete() {
        // 震动提醒
        vibrate()
        
        if (isWorkPhase) {
            // 工作阶段完成，进入休息阶段
            if (currentCycle < totalCycles) {
                // 还有下一个循环，进入休息
                isWorkPhase = false
                remainingSeconds = BREAK_DURATION_MINUTES * 60
                totalSeconds = remainingSeconds
                startTimer()
            } else {
                // 所有循环完成
                showCompleteNotification()
                stopTimer()
            }
        } else {
            // 休息阶段完成，进入下一个工作循环
            currentCycle++
            isWorkPhase = true
            remainingSeconds = WORK_DURATION_MINUTES * 60
            totalSeconds = remainingSeconds
            startTimer()
        }
    }
    
    private fun updateState() {
        if (isPaused) {
            _state.value = PomodoroState.Paused(
                title = currentTitle,
                cycles = totalCycles,
                currentCycle = currentCycle,
                isWork = isWorkPhase,
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds
            )
        } else {
            _state.value = PomodoroState.Running(
                title = currentTitle,
                cycles = totalCycles,
                currentCycle = currentCycle,
                isWork = isWorkPhase,
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds
            )
        }
    }
    
    private fun createNotification(): android.app.Notification {
        val phaseText = if (isWorkPhase) "专注中" else "休息中"
        val phaseEmoji = if (isWorkPhase) "🍅" else "☕"
        val phaseColor = if (isWorkPhase) 0xFF6366F1.toInt() else 0xFF14B8A6.toInt()
        val progress = ((totalSeconds - remainingSeconds).toFloat() / totalSeconds * 100).toInt()
        val timeText = formatTime(remainingSeconds)
        
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 暂停/继续按钮
        val pauseResumeIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, PomodoroService::class.java).apply {
                action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pauseResumeText = if (isPaused) "继续" else "暂停"
        
        // 跳过按钮
        val skipIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, PomodoroService::class.java).apply {
                action = ACTION_SKIP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 停止按钮
        val stopIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, PomodoroService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 创建大文本样式
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("$phaseEmoji $phaseText")
            .bigText("$currentTitle\n⏱️ $timeText  •  第 $currentCycle/$totalCycles 个")
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$phaseEmoji $phaseText · $currentTitle")
            .setContentText("⏱️ $timeText  •  第 $currentCycle/$totalCycles 个")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setProgress(100, progress, false)
            .setColor(phaseColor)
            .setColorized(true)
            .setStyle(bigTextStyle)
            .addAction(R.drawable.ic_notification, pauseResumeText, pauseResumeIntent)
            .addAction(R.drawable.ic_notification, "跳过", skipIntent)
            .addAction(R.drawable.ic_notification, "停止", stopIntent)
            .build()
    }
    
    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    private fun showCompleteNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 创建大文本样式
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("🎉 番茄钟完成！")
            .bigText("太棒了！你已完成 $totalCycles 个番茄钟\n继续保持专注，高效工作！")
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎉 番茄钟完成！")
            .setContentText("已完成 $totalCycles 个番茄钟")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(0xFF22C55E.toInt()) // 成功绿色
            .setColorized(true)
            .setStyle(bigTextStyle)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "番茄钟",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "番茄钟倒计时通知"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(null, null) // 倒计时期间不需要声音
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
        }
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TimeTracker::PomodoroWakeLock"
        )
        wakeLock?.acquire(30 * 60 * 1000L) // 30分钟
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
}
