package com.timetracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val color: String,  // 颜色代码，如 "#FF5722"
    val name: String,
    val defaultDuration: Int,
    val isFrequent: Boolean = false,
    val timeNature: String = "PRODUCTIVE",  // PRODUCTIVE, UNPRODUCTIVE, NEUTRAL
    val usageCount: Int = 0  // 使用次数统计
)
