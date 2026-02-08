package com.timetracker.app.data.model

data class Template(
    val id: Long = 0,
    val color: String,  // 颜色代码，如 "#FF5722"
    val name: String,
    val defaultDuration: Int,
    val isFrequent: Boolean = false,
    val timeNature: TimeNature = TimeNature.PRODUCTIVE,  // 时间性质：元气满满、摸鱼时光、中性
    val usageCount: Int = 0  // 使用次数统计
)

fun Template.toEntity() = com.timetracker.app.data.local.entity.TemplateEntity(
    id = id,
    color = color,
    name = name,
    defaultDuration = defaultDuration,
    isFrequent = isFrequent,
    timeNature = timeNature.name,
    usageCount = usageCount
)

fun com.timetracker.app.data.local.entity.TemplateEntity.toModel() = Template(
    id = id,
    color = color,
    name = name,
    defaultDuration = defaultDuration,
    isFrequent = isFrequent,
    timeNature = try {
        TimeNature.valueOf(timeNature)
    } catch (e: Exception) {
        TimeNature.PRODUCTIVE
    },
    usageCount = usageCount
)
