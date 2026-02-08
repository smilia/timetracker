package com.timetracker.app.service.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.timetracker.app.MainActivity
import com.timetracker.app.R
import com.timetracker.app.TimeTrackerApp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        
        // Check if reminders are still enabled
        val database = androidx.room.Room.databaseBuilder(
            context,
            com.timetracker.app.data.local.database.TimeTrackerDatabase::class.java,
            "timetracker.db"
        ).build()
        
        val settings = database.reminderSettingsDao().getSettingsSync()
        
        if (settings?.isEnabled != true) {
            return Result.success()
        }
        
        // Check quiet hours
        val now = LocalTime.now()
        val quietStart = settings.quietHoursStart?.let { 
            try { LocalTime.parse(it) } catch (e: Exception) { null }
        }
        val quietEnd = settings.quietHoursEnd?.let { 
            try { LocalTime.parse(it) } catch (e: Exception) { null }
        }
        
        if (quietStart != null && quietEnd != null) {
            if (isInQuietHours(now, quietStart, quietEnd)) {
                // Reschedule for when quiet hours end
                scheduleNextReminder(context, settings.intervalMinutes, quietEnd)
                return Result.success()
            }
        }
        
        // Show notification
        showReminderNotification(context)
        
        // Schedule next reminder
        scheduleNextReminder(context, settings.intervalMinutes, null)
        
        return Result.success()
    }
    
    private fun showReminderNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TimeTrackerApp.CHANNEL_REMINDER,
                "时间提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "定时提醒记录当前活动"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, TimeTrackerApp.CHANNEL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ 时间提醒 $currentTime")
            .setContentText("该记录当前活动了，保持专注！")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(ReminderReceiver.NOTIFICATION_ID_REMINDER, notification)
    }
    
    private fun isInQuietHours(now: LocalTime, quietStart: LocalTime, quietEnd: LocalTime): Boolean {
        return if (quietStart.isBefore(quietEnd)) {
            now.isAfter(quietStart) && now.isBefore(quietEnd)
        } else {
            // Crosses midnight
            now.isAfter(quietStart) || now.isBefore(quietEnd)
        }
    }
    
    companion object {
        const val WORK_TAG = "reminder_work"
        
        fun scheduleNextReminder(context: Context, intervalMinutes: Int, resumeTime: LocalTime?) {
            val workManager = WorkManager.getInstance(context)
            
            // Cancel existing work
            workManager.cancelAllWorkByTag(WORK_TAG)
            
            val delayMinutes = if (resumeTime != null) {
                // Calculate delay until quiet hours end
                val now = LocalTime.now()
                val delay = if (resumeTime.isAfter(now)) {
                    java.time.Duration.between(now, resumeTime).toMinutes()
                } else {
                    // Resume time is tomorrow
                    java.time.Duration.between(now, java.time.LocalTime.MAX).toMinutes() +
                    java.time.Duration.between(java.time.LocalTime.MIN, resumeTime).toMinutes() + 1
                }
                delay.coerceAtLeast(1)
            } else {
                intervalMinutes.toLong()
            }
            
            val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .addTag(WORK_TAG)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .setRequiresStorageNotLow(false)
                        .build()
                )
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                reminderWork
            )
        }
        
        fun cancelReminders(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        }
    }
}
