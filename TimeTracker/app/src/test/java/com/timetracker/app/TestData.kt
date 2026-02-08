package com.timetracker.app

import com.timetracker.app.data.model.Category
import com.timetracker.app.data.model.Template
import com.timetracker.app.data.model.TimeBlock
import java.time.LocalDate
import java.time.LocalDateTime

object TestData {

    // Categories
    val workCategory = Category(
        id = 1,
        name = "工作",
        color = "#FF6B6B",
        icon = "work"
    )

    val studyCategory = Category(
        id = 2,
        name = "学习",
        color = "#4ECDC4",
        icon = "school"
    )

    val restCategory = Category(
        id = 3,
        name = "休息",
        color = "#96CEB4",
        icon = "coffee"
    )

    val allCategories = listOf(workCategory, studyCategory, restCategory)

    // Time Blocks
    fun createTimeBlock(
        id: Long = 0,
        title: String = "测试时间块",
        categoryId: Long? = 1,
        startHour: Int = 9,
        durationHours: Int = 1,
        date: LocalDate = LocalDate.of(2024, 2, 6)
    ): TimeBlock {
        return TimeBlock(
            id = id,
            categoryId = categoryId,
            title = title,
            startTime = LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, startHour, 0),
            endTime = LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, startHour + durationHours, 0),
            date = date
        )
    }

    val morningWorkBlock = createTimeBlock(
        id = 1,
        title = "上午工作",
        categoryId = 1,
        startHour = 9,
        durationHours = 3
    )

    val afternoonStudyBlock = createTimeBlock(
        id = 2,
        title = "下午学习",
        categoryId = 2,
        startHour = 14,
        durationHours = 2
    )

    val eveningRestBlock = createTimeBlock(
        id = 3,
        title = "晚间休息",
        categoryId = 3,
        startHour = 20,
        durationHours = 1
    )

    val allTimeBlocks = listOf(morningWorkBlock, afternoonStudyBlock, eveningRestBlock)

    // Templates
    val workTemplate = Template(
        id = 1,
        categoryId = 1,
        name = "深度工作",
        defaultDuration = 120,
        isFrequent = true
    )

    val studyTemplate = Template(
        id = 2,
        categoryId = 2,
        name = "学习",
        defaultDuration = 90,
        isFrequent = true
    )

    val restTemplate = Template(
        id = 3,
        categoryId = 3,
        name = "休息",
        defaultDuration = 30,
        isFrequent = false
    )

    val allTemplates = listOf(workTemplate, studyTemplate, restTemplate)

    // Test dates
    val testDate = LocalDate.of(2024, 2, 6)
    val testDateTime = LocalDateTime.of(2024, 2, 6, 9, 0)
}
