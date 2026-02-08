package com.timetracker.app.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.timetracker.app.data.local.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TimeTrackerDatabaseTest {

    private lateinit var db: TimeTrackerDatabase
    private lateinit var categoryDao: com.timetracker.app.data.local.dao.CategoryDao
    private lateinit var timeBlockDao: com.timetracker.app.data.local.dao.TimeBlockDao
    private lateinit var templateDao: com.timetracker.app.data.local.dao.TemplateDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TimeTrackerDatabase::class.java
        ).build()
        categoryDao = db.categoryDao()
        timeBlockDao = db.timeBlockDao()
        templateDao = db.templateDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadCategory() = runBlocking {
        // Given
        val category = CategoryEntity(
            name = "测试分类",
            color = "#FF0000",
            icon = "test"
        )

        // When
        val id = categoryDao.insertCategory(category)
        val byId = categoryDao.getCategoryById(id)

        // Then
        assertNotNull(byId)
        assertEquals("测试分类", byId?.name)
        assertEquals("#FF0000", byId?.color)
    }

    @Test
    fun writeAndReadTimeBlock() = runBlocking {
        // Given
        val date = System.currentTimeMillis()
        val timeBlock = TimeBlockEntity(
            categoryId = null,
            title = "测试时间块",
            startTime = date + 9 * 60 * 60 * 1000,
            endTime = date + 10 * 60 * 60 * 1000,
            date = date
        )

        // When
        val id = timeBlockDao.insertTimeBlock(timeBlock)
        val byId = timeBlockDao.getTimeBlockById(id)

        // Then
        assertNotNull(byId)
        assertEquals("测试时间块", byId?.title)
    }

    @Test
    fun getTimeBlocksByDate() = runBlocking {
        // Given
        val date = System.currentTimeMillis()
        val timeBlock1 = TimeBlockEntity(
            categoryId = null,
            title = "上午工作",
            startTime = date + 9 * 60 * 60 * 1000,
            endTime = date + 12 * 60 * 60 * 1000,
            date = date
        )
        val timeBlock2 = TimeBlockEntity(
            categoryId = null,
            title = "下午学习",
            startTime = date + 14 * 60 * 60 * 1000,
            endTime = date + 16 * 60 * 60 * 1000,
            date = date
        )

        // When
        timeBlockDao.insertTimeBlock(timeBlock1)
        timeBlockDao.insertTimeBlock(timeBlock2)
        val blocks = timeBlockDao.getTimeBlocksByDate(date).first()

        // Then
        assertEquals(2, blocks.size)
    }

    @Test
    fun updateTimeBlock() = runBlocking {
        // Given
        val date = System.currentTimeMillis()
        val timeBlock = TimeBlockEntity(
            categoryId = null,
            title = "原始标题",
            startTime = date + 9 * 60 * 60 * 1000,
            endTime = date + 10 * 60 * 60 * 1000,
            date = date
        )
        val id = timeBlockDao.insertTimeBlock(timeBlock)

        // When
        val updated = timeBlock.copy(id = id, title = "更新后的标题")
        timeBlockDao.updateTimeBlock(updated)
        val byId = timeBlockDao.getTimeBlockById(id)

        // Then
        assertEquals("更新后的标题", byId?.title)
    }

    @Test
    fun deleteTimeBlock() = runBlocking {
        // Given
        val date = System.currentTimeMillis()
        val timeBlock = TimeBlockEntity(
            categoryId = null,
            title = "待删除",
            startTime = date + 9 * 60 * 60 * 1000,
            endTime = date + 10 * 60 * 60 * 1000,
            date = date
        )
        val id = timeBlockDao.insertTimeBlock(timeBlock)

        // When
        timeBlockDao.deleteTimeBlockById(id)
        val byId = timeBlockDao.getTimeBlockById(id)

        // Then
        assertNull(byId)
    }

    @Test
    fun getOverlappingBlocks() = runBlocking {
        // Given
        val date = System.currentTimeMillis()
        
        // Create a block from 9:00 to 12:00
        val existingBlock = TimeBlockEntity(
            categoryId = null,
            title = "现有块",
            startTime = date + 9 * 60 * 60 * 1000,
            endTime = date + 12 * 60 * 60 * 1000,
            date = date
        )
        timeBlockDao.insertTimeBlock(existingBlock)

        // When - Check for overlap with 10:00 to 11:00
        val overlapping = timeBlockDao.getOverlappingBlocks(
            date + 10 * 60 * 60 * 1000,
            date + 11 * 60 * 60 * 1000,
            date
        )

        // Then
        assertEquals(1, overlapping.size)
    }

    @Test
    fun noOverlappingBlocks() = runBlocking {
        // Given
        val date = System.currentTimeMillis()
        
        // Create a block from 9:00 to 10:00
        val existingBlock = TimeBlockEntity(
            categoryId = null,
            title = "现有块",
            startTime = date + 9 * 60 * 60 * 1000,
            endTime = date + 10 * 60 * 60 * 1000,
            date = date
        )
        timeBlockDao.insertTimeBlock(existingBlock)

        // When - Check for no overlap with 11:00 to 12:00
        val overlapping = timeBlockDao.getOverlappingBlocks(
            date + 11 * 60 * 60 * 1000,
            date + 12 * 60 * 60 * 1000,
            date
        )

        // Then
        assertEquals(0, overlapping.size)
    }

    @Test
    fun writeAndReadTemplate() = runBlocking {
        // Given
        val category = CategoryEntity(name = "工作", color = "#FF0000", icon = "work")
        val categoryId = categoryDao.insertCategory(category)
        
        val template = TemplateEntity(
            categoryId = categoryId,
            name = "深度工作",
            defaultDuration = 120,
            isFrequent = true
        )

        // When
        val id = templateDao.insertTemplate(template)
        val byId = templateDao.getTemplateById(id)

        // Then
        assertNotNull(byId)
        assertEquals("深度工作", byId?.name)
        assertEquals(120, byId?.defaultDuration)
        assertTrue(byId?.isFrequent == true)
    }

    @Test
    fun getFrequentTemplates() = runBlocking {
        // Given
        val category = CategoryEntity(name = "工作", color = "#FF0000", icon = "work")
        val categoryId = categoryDao.insertCategory(category)
        
        val frequentTemplate = TemplateEntity(
            categoryId = categoryId,
            name = "常用模板",
            defaultDuration = 60,
            isFrequent = true
        )
        val normalTemplate = TemplateEntity(
            categoryId = categoryId,
            name = "普通模板",
            defaultDuration = 30,
            isFrequent = false
        )
        
        templateDao.insertTemplate(frequentTemplate)
        templateDao.insertTemplate(normalTemplate)

        // When
        val frequent = templateDao.getFrequentTemplates().first()

        // Then
        assertEquals(1, frequent.size)
        assertEquals("常用模板", frequent[0].name)
    }

    @Test
    fun writeAndReadReminderSettings() = runBlocking {
        // Given
        val settings = ReminderSettingsEntity(
            isEnabled = true,
            intervalMinutes = 30,
            quietHoursStart = "22:00",
            quietHoursEnd = "08:00",
            vibrationEnabled = true
        )

        // When
        db.reminderSettingsDao().insertSettings(settings)
        val retrieved = db.reminderSettingsDao().getSettings().first()

        // Then
        assertNotNull(retrieved)
        assertEquals(true, retrieved?.isEnabled)
        assertEquals(30, retrieved?.intervalMinutes)
        assertEquals("22:00", retrieved?.quietHoursStart)
    }

    @Test
    fun updateReminderSettings() = runBlocking {
        // Given
        val settings = ReminderSettingsEntity(
            isEnabled = true,
            intervalMinutes = 30
        )
        db.reminderSettingsDao().insertSettings(settings)

        // When
        val updated = settings.copy(intervalMinutes = 60, isEnabled = false)
        db.reminderSettingsDao().updateSettings(updated)
        val retrieved = db.reminderSettingsDao().getSettings().first()

        // Then
        assertEquals(60, retrieved?.intervalMinutes)
        assertEquals(false, retrieved?.isEnabled)
    }
}
