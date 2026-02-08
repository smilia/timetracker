package com.timetracker.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TimeTrackerApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER,
                "时间提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "定时提醒当前进行中的活动"
                enableVibration(true)
            }
            
            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE,
                "日程提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "日程开始前的提醒"
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(reminderChannel, scheduleChannel))
        }
    }
    
    companion object {
        const val CHANNEL_REMINDER = "reminder_channel"
        const val CHANNEL_SCHEDULE = "schedule_channel"
    }
}
