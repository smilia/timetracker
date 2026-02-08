package com.timetracker.app.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class TimeBlock(
    val id: Long = 0,
    val color: String,  // 颜色代码，如 "#FF5722"
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val date: LocalDate,
    val note: String? = null,
    val isCompleted: Boolean = false,
    val isReminderEnabled: Boolean = false,
    val isPomodoro: Boolean = false,
    val timeNature: TimeNature = TimeNature.PRODUCTIVE  // 时间性质：元气满满、摸鱼时光、中性
) {
    val durationMinutes: Long
        get() = java.time.Duration.between(startTime, endTime).toMinutes()
    
    val durationHours: Float
        get() = durationMinutes / 60f
}

fun TimeBlock.toEntity() = com.timetracker.app.data.local.entity.TimeBlockEntity(
    id = id,
    color = color,
    title = title,
    startTime = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    endTime = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    note = note,
    isCompleted = isCompleted,
    isReminderEnabled = isReminderEnabled,
    isPomodoro = isPomodoro,
    timeNature = timeNature.name
)

fun com.timetracker.app.data.local.entity.TimeBlockEntity.toModel() = TimeBlock(
    id = id,
    color = color,
    title = title,
    startTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(startTime),
        ZoneId.systemDefault()
    ),
    endTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(endTime),
        ZoneId.systemDefault()
    ),
    date = LocalDate.ofInstant(
        java.time.Instant.ofEpochMilli(date),
        ZoneId.systemDefault()
    ),
    note = note,
    isCompleted = isCompleted,
    isReminderEnabled = isReminderEnabled,
    isPomodoro = isPomodoro,
    timeNature = try {
        TimeNature.valueOf(timeNature)
    } catch (e: Exception) {
        TimeNature.PRODUCTIVE
    }
)
