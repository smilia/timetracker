package com.timetracker.app.service.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.timetracker.app.data.local.database.TimeTrackerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar

object ReminderScheduler {
    
    fun scheduleNextReminder(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = androidx.room.Room.databaseBuilder(
                context,
                TimeTrackerDatabase::class.java,
                "timetracker.db"
            ).build()
            
            val settings = database.reminderSettingsDao().getSettingsSync()
            
            if (settings?.isEnabled != true) return@launch
            
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
                    // Schedule for when quiet hours end
                    scheduleReminderAt(context, quietEnd.plusMinutes(1))
                    return@launch
                }
            }
            
            // Schedule next reminder
            val nextReminderTime = LocalDateTime.now().plusMinutes(settings.intervalMinutes.toLong())
            scheduleReminderAt(context, nextReminderTime.toLocalTime())
        }
    }
    
    fun scheduleReminderAt(context: Context, time: LocalTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            
            // If time has passed, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    fun scheduleScheduleReminder(context: Context, scheduleId: Long, title: String, reminderTime: LocalDateTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SCHEDULE_REMINDER
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    private fun isInQuietHours(now: LocalTime, quietStart: LocalTime, quietEnd: LocalTime): Boolean {
        return if (quietStart.isBefore(quietEnd)) {
            now.isAfter(quietStart) && now.isBefore(quietEnd)
        } else {
            // Crosses midnight
            now.isAfter(quietStart) || now.isBefore(quietEnd)
        }
    }
}
