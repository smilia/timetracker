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
import androidx.core.app.NotificationCompat
import com.timetracker.app.MainActivity
import com.timetracker.app.R
import com.timetracker.app.data.local.database.TimeTrackerDatabase
import com.timetracker.app.data.model.TimeBlock
import com.timetracker.app.data.model.toModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class LockScreenNotificationService : Service() {

    @Inject
    lateinit var database: TimeTrackerDatabase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var updateJob: Job? = null

    companion object {
        const val CHANNEL_ID = "lockscreen_time_tracker_v2"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.timetracker.app.START_LOCKSCREEN_NOTIFICATION"
        const val ACTION_STOP = "com.timetracker.app.STOP_LOCKSCREEN_NOTIFICATION"

        fun start(context: Context) {
            val intent = Intent(context, LockScreenNotificationService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LockScreenNotificationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
        }
        return START_STICKY
    }

    private fun startService() {
        try {
            val notification = createNotification(null, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            startPeriodicUpdate()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun stopService() {
        updateJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                updateNotification()
                delay(60000) // 每分钟更新一次
            }
        }
    }

    private suspend fun updateNotification() {
        try {
            val today = LocalDate.now()
            val todayMillis = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val tomorrowMillis = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

            val timeBlocks = database.timeBlockDao().getTimeBlocksForWidget(todayMillis, tomorrowMillis)

            val currentTime = LocalDateTime.now()
            val currentTimeMillis = java.time.ZoneId.systemDefault().let {
                currentTime.atZone(it).toInstant().toEpochMilli()
            }

            val currentBlockEntity = timeBlocks.find { block ->
                block.startTime <= currentTimeMillis && block.endTime > currentTimeMillis
            }
            val currentBlock = currentBlockEntity?.toModel()

            val nextBlockEntity = timeBlocks
                .filter { it.startTime > currentTimeMillis }
                .minByOrNull { it.startTime }
            val nextBlock = nextBlockEntity?.toModel()

            val notification = createNotification(currentBlock, nextBlock)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotification(currentBlock: TimeBlock?, nextBlock: TimeBlock?): Notification {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val currentTime = LocalDateTime.now()

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏显示
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setShowWhen(false)
            .setColor(0xFF6366F1.toInt()) // 主题蓝色
            .setColorized(true)

        if (currentBlock != null) {
            // 正在进行中的时间块
            val remainingMinutes = java.time.Duration.between(
                currentTime,
                currentBlock.endTime
            ).toMinutes()

            builder.setContentTitle("⏱️ ${currentBlock.title}")
                .setContentText("剩余 ${remainingMinutes}分钟 · ${currentBlock.startTime.format(timeFormatter)}-${currentBlock.endTime.format(timeFormatter)}")

            // 添加详细内容
            val bigText = buildString {
                appendLine("当前活动: ${currentBlock.title}")
                appendLine("时间: ${currentBlock.startTime.format(timeFormatter)} - ${currentBlock.endTime.format(timeFormatter)}")
                appendLine("剩余: ${remainingMinutes}分钟")
                currentBlock.note?.let { appendLine("备注: $it") }
            }
            builder.setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("⏱️ 进行中 · ${currentBlock.title}")
                .bigText(bigText))
        } else {
            builder.setContentTitle("📅 时间记录")
                .setContentText("当前无进行中的活动")

            if (nextBlock != null) {
                val timeUntilNext = java.time.Duration.between(
                    currentTime,
                    nextBlock.startTime
                ).toMinutes()

                builder.setContentText("下个活动: ${nextBlock.title} (${timeUntilNext}分钟后)")

                val bigText = buildString {
                    appendLine("下个活动: ${nextBlock.title}")
                    appendLine("开始时间: ${nextBlock.startTime.format(timeFormatter)}")
                    appendLine("还有: ${timeUntilNext}分钟")
                }
                builder.setStyle(NotificationCompat.BigTextStyle()
                    .setBigContentTitle("⏳ 即将开始 · ${nextBlock.title}")
                    .bigText(bigText))
            }
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "锁屏时间显示",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "在锁屏界面显示当前进行中的时间块"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        serviceScope.cancel()
    }
}
