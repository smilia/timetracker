package com.timetracker.app.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.timetracker.app.MainActivity
import com.timetracker.app.R
import com.timetracker.app.data.local.database.TimeTrackerDatabase
import com.timetracker.app.data.local.entity.TimeBlockEntity
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.data.model.toDisplayName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class EnhancedNotificationService : Service() {

    @Inject
    lateinit var database: TimeTrackerDatabase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var updateJob: Job? = null

    companion object {
        const val CHANNEL_ID = "enhanced_time_tracker_channel_v2"
        const val POMODORO_CHANNEL_ID = "pomodoro_channel_v2"
        const val REMINDER_CHANNEL_ID = "reminder_channel_v2"
        const val NOTIFICATION_ID = 1001
        const val POMODORO_NOTIFICATION_ID = 1002
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE_POMODORO = "ACTION_PAUSE_POMODORO"
        const val ACTION_RESUME_POMODORO = "ACTION_RESUME_POMODORO"
        const val ACTION_SKIP_POMODORO = "ACTION_SKIP_POMODORO"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
            ACTION_PAUSE_POMODORO -> pausePomodoro()
            ACTION_RESUME_POMODORO -> resumePomodoro()
            ACTION_SKIP_POMODORO -> skipPomodoro()
        }
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mainChannel = NotificationChannel(
                CHANNEL_ID,
                "时间追踪通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示当前时间块和今日统计"
                setShowBadge(false)
            }

            val pomodoroChannel = NotificationChannel(
                POMODORO_CHANNEL_ID,
                "番茄钟倒计时",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "番茄钟专注计时"
                setShowBadge(true)
            }

            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "时间块提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "时间块开始和结束提醒"
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(mainChannel, pomodoroChannel, reminderChannel))
        }
    }

    private fun startService() {
        val notification = createEnhancedNotification(null, null, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startPeriodicUpdate()
    }

    private fun stopService() {
        updateJob?.cancel()
        stopForeground(true)
        stopSelf()
    }

    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                updateNotification()
                // 每30秒更新一次，减少电量消耗
                // 时间块通常以15分钟为单位，不需要每秒更新
                delay(30000)
            }
        }
    }

    private suspend fun updateNotification() {
        try {
            val today = LocalDate.now()
            val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val tomorrowMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val timeBlocks = database.timeBlockDao().getTimeBlocksForWidget(todayMillis, tomorrowMillis)
            val currentTimeMillis = System.currentTimeMillis()

            val currentBlock = timeBlocks.find { block ->
                block.startTime <= currentTimeMillis && block.endTime > currentTimeMillis
            }

            val nextBlock = timeBlocks
                .filter { it.startTime > currentTimeMillis }
                .minByOrNull { it.startTime }

            val stats = calculateDailyStats(timeBlocks)

            val notification = createEnhancedNotification(currentBlock, nextBlock, stats)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateDailyStats(blocks: List<TimeBlockEntity>): DailyStats {
        var productiveMinutes = 0
        var unproductiveMinutes = 0
        var neutralMinutes = 0

        blocks.forEach { block ->
            val durationMinutes = ((block.endTime - block.startTime) / (1000 * 60)).toInt()
            val nature = try {
                TimeNature.valueOf(block.timeNature)
            } catch (e: Exception) {
                TimeNature.PRODUCTIVE
            }
            when (nature) {
                TimeNature.PRODUCTIVE -> productiveMinutes += durationMinutes
                TimeNature.UNPRODUCTIVE -> unproductiveMinutes += durationMinutes
                TimeNature.NEUTRAL -> neutralMinutes += durationMinutes
            }
        }

        val totalMinutes = productiveMinutes + unproductiveMinutes + neutralMinutes
        val efficiency = if (totalMinutes > 0) {
            (productiveMinutes.toFloat() / totalMinutes * 100).toInt()
        } else 0

        return DailyStats(
            productiveMinutes = productiveMinutes,
            unproductiveMinutes = unproductiveMinutes,
            neutralMinutes = neutralMinutes,
            totalMinutes = totalMinutes,
            efficiency = efficiency
        )
    }

    private fun createEnhancedNotification(
        currentBlock: TimeBlockEntity?,
        nextBlock: TimeBlockEntity?,
        stats: DailyStats?
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remoteViews = RemoteViews(packageName, R.layout.notification_enhanced).apply {
            if (currentBlock != null) {
                setTextViewText(R.id.tv_current_title, currentBlock.title)
                setTextViewText(R.id.tv_current_time, formatTimeRange(currentBlock))
                val nature = try {
                    TimeNature.valueOf(currentBlock.timeNature)
                } catch (e: Exception) {
                    TimeNature.PRODUCTIVE
                }
                setTextViewText(R.id.tv_current_nature, nature.toDisplayName())
                setInt(R.id.iv_current_indicator, "setColorFilter",
                    android.graphics.Color.parseColor(currentBlock.color))
                setTextViewText(R.id.tv_remaining, calculateRemaining(currentBlock))
            } else {
                setTextViewText(R.id.tv_current_title, "暂无进行中的时间块")
                setTextViewText(R.id.tv_current_time, "点击添加新的时间块")
                setTextViewText(R.id.tv_current_nature, "")
                setTextViewText(R.id.tv_remaining, "")
            }

            if (nextBlock != null) {
                setTextViewText(R.id.tv_next_title, "下一个: ${nextBlock.title}")
                setTextViewText(R.id.tv_next_time, formatStartTime(nextBlock))
                setInt(R.id.iv_next_indicator, "setColorFilter",
                    android.graphics.Color.parseColor(nextBlock.color))
            } else {
                setTextViewText(R.id.tv_next_title, "今日无更多安排")
                setTextViewText(R.id.tv_next_time, "")
            }

            stats?.let {
                setTextViewText(R.id.tv_stats_productive, "元气满满: ${it.productiveMinutes}分钟")
                setTextViewText(R.id.tv_stats_unproductive, "摸鱼时光: ${it.unproductiveMinutes}分钟")
                setTextViewText(R.id.tv_efficiency, "效率: ${it.efficiency}%")
                setProgressBar(R.id.progress_efficiency, 100, it.efficiency, false)
            }
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showPomodoroNotification(remainingSeconds: Int, isBreak: Boolean, cycle: Int, totalCycles: Int) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val title = if (isBreak) "☕ 休息中" else "🍅 专注中"
        val phaseColor = if (isBreak) 0xFF14B8A6.toInt() else 0xFF6366F1.toInt()
        val totalSeconds = if (isBreak) 5 * 60 else 25 * 60

        val pauseIntent = Intent(this, EnhancedNotificationService::class.java).apply {
            action = if (isBreak) ACTION_RESUME_POMODORO else ACTION_PAUSE_POMODORO
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 0, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(this, EnhancedNotificationService::class.java).apply {
            action = ACTION_SKIP_POMODORO
        }
        val skipPendingIntent = PendingIntent.getService(
            this, 1, skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 创建大文本样式
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("$title · 第 $cycle/$totalCycles 个")
            .bigText("⏱️ 剩余 $timeText\n保持专注，高效工作！")

        val notification = NotificationCompat.Builder(this, POMODORO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$title · 第 $cycle/$totalCycles 个")
            .setContentText("⏱️ 剩余 $timeText")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColor(phaseColor)
            .setColorized(true)
            .setStyle(bigTextStyle)
            .addAction(R.drawable.ic_notification, "暂停", pausePendingIntent)
            .addAction(R.drawable.ic_notification, "跳过", skipPendingIntent)
            .setProgress(totalSeconds, totalSeconds - remainingSeconds, false)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(POMODORO_NOTIFICATION_ID, notification)
    }

    fun showBlockReminderNotification(block: TimeBlockEntity, isStarting: Boolean) {
        val title = if (isStarting) "⏰ 时间块开始" else "✅ 时间块结束"
        val nature = try {
            TimeNature.valueOf(block.timeNature)
        } catch (e: Exception) {
            TimeNature.PRODUCTIVE
        }
        
        val natureText = nature.toDisplayName()
        val natureColor = when (nature) {
            TimeNature.PRODUCTIVE -> 0xFF6366F1.toInt() // 主题蓝
            TimeNature.UNPRODUCTIVE -> 0xFFFB7185.toInt() // 柔和红
            TimeNature.NEUTRAL -> 0xFF94A3B8.toInt() // 中性灰
        }
        
        // 创建大文本样式
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("$title · ${block.title}")
            .bigText("类型: $natureText\n点击打开应用查看详情")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, block.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$title · ${block.title}")
            .setContentText("类型: $natureText")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(natureColor)
            .setColorized(true)
            .setStyle(bigTextStyle)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(block.id.hashCode(), notification)
    }

    private fun formatTimeRange(block: TimeBlockEntity): String {
        val start = java.time.Instant.ofEpochMilli(block.startTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        val end = java.time.Instant.ofEpochMilli(block.endTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return String.format("%02d:%02d - %02d:%02d", start.hour, start.minute, end.hour, end.minute)
    }

    private fun formatStartTime(block: TimeBlockEntity): String {
        val start = java.time.Instant.ofEpochMilli(block.startTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return String.format("%02d:%02d 开始", start.hour, start.minute)
    }

    private fun calculateRemaining(block: TimeBlockEntity): String {
        val now = System.currentTimeMillis()
        val remainingMs = block.endTime - now
        if (remainingMs <= 0) return "即将结束"
        val remainingMinutes = (remainingMs / (1000 * 60)).toInt()
        return "剩余 ${remainingMinutes} 分钟"
    }

    private fun pausePomodoro() {
        sendBroadcast(Intent("POMODORO_PAUSE"))
    }

    private fun resumePomodoro() {
        sendBroadcast(Intent("POMODORO_RESUME"))
    }

    private fun skipPomodoro() {
        sendBroadcast(Intent("POMODORO_SKIP"))
    }

    data class DailyStats(
        val productiveMinutes: Int,
        val unproductiveMinutes: Int,
        val neutralMinutes: Int,
        val totalMinutes: Int,
        val efficiency: Int
    )

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        serviceScope.cancel()
    }
}