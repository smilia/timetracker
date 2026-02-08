package com.timetracker.app.service.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.timetracker.app.MainActivity
import com.timetracker.app.R
import com.timetracker.app.data.local.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 定时提醒管理器 - 使用 AlarmManager 实现精确的每小时固定时间点提醒
 */
class IntervalReminderManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "interval_reminder_channel_v2"
        const val NOTIFICATION_ID = 3001
        const val ACTION_REMINDER = "com.timetracker.app.REMINDER"
        
        // DataStore Keys
        val INTERVAL_REMINDER_ENABLED = booleanPreferencesKey("interval_reminder_enabled")
        val INTERVAL_REMINDER_MINUTES = stringPreferencesKey("interval_reminder_minutes")
        
        // 默认提醒时间点（每小时的分钟数）
        val DEFAULT_REMINDER_MINUTES = listOf(15, 30, 45, 0) // 0 表示整点（60分）

        fun startReminder(context: Context) {
            val manager = IntervalReminderManager(context)
            manager.startReminder()
        }

        fun stopReminder(context: Context) {
            val manager = IntervalReminderManager(context)
            manager.stopReminder()
        }

        fun restartIfEnabled(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = context.dataStore.data.first()[INTERVAL_REMINDER_ENABLED] ?: false
                if (enabled) {
                    startReminder(context)
                }
            }
        }
        
        // 获取用户选择的提醒时间点
        suspend fun getSelectedMinutes(context: Context): List<Int> {
            val minutesString = context.dataStore.data.first()[INTERVAL_REMINDER_MINUTES]
            return if (minutesString.isNullOrEmpty()) {
                DEFAULT_REMINDER_MINUTES
            } else {
                minutesString.split(",").mapNotNull { it.toIntOrNull() }
            }
        }
        
        // 设置下一个提醒（静态方法供 ReminderReceiver 调用）
        fun scheduleNextReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            
            CoroutineScope(Dispatchers.IO).launch {
                // 获取用户选择的提醒时间点
                val selectedMinutes = getSelectedMinutes(context)
                if (selectedMinutes.isEmpty()) return@launch
                
                // 找到下一个提醒时间点
                val currentMinute = now.minute
                val currentSecond = now.second
                
                // 将分钟转换为可比较的数值（0表示60分/整点）
                val sortedMinutes = selectedMinutes.sortedBy { if (it == 0) 60 else it }
                
                // 找到下一个时间点
                var nextMinute = -1
                var addHours = 0
                
                for (minute in sortedMinutes) {
                    val compareMinute = if (minute == 0) 60 else minute
                    if (compareMinute > currentMinute || 
                        (compareMinute == currentMinute && currentSecond < 55)) {
                        nextMinute = minute
                        break
                    }
                }
                
                // 如果当前小时没有剩余的时间点，使用第一个时间点并加一小时
                if (nextMinute == -1) {
                    nextMinute = selectedMinutes.first()
                    addHours = 1
                }

                var nextReminder = now
                    .withMinute(if (nextMinute == 0) 0 else nextMinute)
                    .withSecond(0)
                    .withNano(0)
                
                if (addHours > 0) {
                    nextReminder = nextReminder.plusHours(addHours.toLong())
                }

                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    action = ACTION_REMINDER
                }
                
                // 使用固定的 requestCode
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                val triggerTime = nextReminder.toInstant().toEpochMilli()
                
                android.util.Log.d("IntervalReminder", "Scheduling next reminder at: ${nextReminder.toLocalTime()}, triggerTime=$triggerTime")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
        }
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createNotificationChannel()
    }

    fun startReminder() {
        // 使用协程异步保存设置，避免阻塞主线程
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { preferences ->
                preferences[INTERVAL_REMINDER_ENABLED] = true
            }

            // 取消之前的提醒
            stopReminderInternal()

            // 只设置下一个最近的提醒
            scheduleNextReminder(context)
            
            // 显示启动通知
            showTestNotification()
        }
    }
    
    private fun showTestNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            val selectedMinutes = getSelectedMinutes(context)
            val minutesText = selectedMinutes.map { if (it == 0) "60" else it.toString() }.joinToString(", ")
            
            // 创建大文本样式
            val bigTextStyle = NotificationCompat.BigTextStyle()
                .setBigContentTitle("⏰ 定时提醒已启动")
                .bigText("将在每小时的 ${minutesText} 分钟提醒您记录时间\n养成良好的时间管理习惯！")
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("⏰ 定时提醒已启动")
                .setContentText("将在每小时的 ${minutesText} 分钟提醒您")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xFF6366F1.toInt()) // 主题蓝色
                .setColorized(true)
                .setStyle(bigTextStyle)
                .build()
            
            notificationManager.notify(9999, notification)
        }
    }

    fun stopReminder() {
        // 使用协程异步保存设置，避免阻塞主线程
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { preferences ->
                preferences[INTERVAL_REMINDER_ENABLED] = false
            }
            stopReminderInternal()
        }
    }
    
    private fun stopReminderInternal() {
        // 取消提醒（使用固定的 requestCode 0）
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "定时记录提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "每小时的15、30、45、60分钟提醒您记录当前活动"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

/**
 * 广播接收器 - 接收提醒并显示通知，同时设置下一个提醒
 */
class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == IntervalReminderManager.ACTION_REMINDER) {
            android.util.Log.d("IntervalReminder", "ReminderReceiver received alarm")
            
            // 显示通知
            showReminderNotification(context)
            
            // 设置下一个提醒
            IntervalReminderManager.scheduleNextReminder(context)
        }
    }
    
    private fun showReminderNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // 获取当前时间
        val currentTime = java.time.LocalDateTime.now()
        val timeText = String.format("%02d:%02d", currentTime.hour, currentTime.minute)
        
        // 创建大文本样式
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("⏰ 该记录时间了！")
            .bigText("现在是 $timeText\n记录一下你正在做的事情吧，养成时间管理的好习惯！")
        
        val notification = NotificationCompat.Builder(context, IntervalReminderManager.CHANNEL_ID)
            .setContentTitle("⏰ 该记录时间了！")
            .setContentText("现在是 $timeText，记录一下你正在做的事情吧")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(0xFF6366F1.toInt()) // 主题蓝色
            .setColorized(true)
            .setStyle(bigTextStyle)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .build()
        
        notificationManager.notify(IntervalReminderManager.NOTIFICATION_ID, notification)
    }
}
