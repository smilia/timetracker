package com.timetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_blocks",
    indices = [Index("startTime"), Index("date")]
)
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val color: String,  // 颜色代码，如 "#FF5722"
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val date: Long,
    val note: String? = null,
    val isCompleted: Boolean = false,
    val isReminderEnabled: Boolean = false,
    val isPomodoro: Boolean = false,
    val timeNature: String = "PRODUCTIVE",  // PRODUCTIVE, UNPRODUCTIVE, NEUTRAL
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
