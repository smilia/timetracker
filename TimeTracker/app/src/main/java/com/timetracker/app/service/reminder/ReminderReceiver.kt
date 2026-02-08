package com.timetracker.app.service.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.timetracker.app.MainActivity
import com.timetracker.app.R
import com.timetracker.app.TimeTrackerApp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_REMINDER -> showReminderNotification(context)
            ACTION_SCHEDULE_REMINDER -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "日程提醒"
                showScheduleNotification(context, title)
            }
        }
    }
    
    private fun showReminderNotification(context: Context) {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        
        val notification = NotificationCompat.Builder(context, TimeTrackerApp.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ 时间提醒 $currentTime")
            .setContentText("该记录当前活动了，保持专注！")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
        
        // Schedule next reminder
        ReminderScheduler.scheduleNextReminder(context)
    }
    
    private fun showScheduleNotification(context: Context, title: String) {
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, TimeTrackerApp.CHANNEL_SCHEDULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📅 日程提醒")
            .setContentText("即将开始: $title")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 100, 300, 100, 300))
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_SCHEDULE, notification)
    }
    
    companion object {
        const val ACTION_REMINDER = "com.timetracker.app.REMINDER"
        const val ACTION_SCHEDULE_REMINDER = "com.timetracker.app.SCHEDULE_REMINDER"
        const val EXTRA_TITLE = "title"
        
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_SCHEDULE = 1002
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule reminders after boot
            ReminderScheduler.scheduleNextReminder(context)
        }
    }
}
