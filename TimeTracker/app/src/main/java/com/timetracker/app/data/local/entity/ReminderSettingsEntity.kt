package com.timetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_settings")
data class ReminderSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val isEnabled: Boolean = true,
    val intervalMinutes: Int = 30,
    val quietHoursStart: String? = "22:00",
    val quietHoursEnd: String? = "08:00",
    val vibrationEnabled: Boolean = true,
    val lockScreenNotificationEnabled: Boolean = false
)
