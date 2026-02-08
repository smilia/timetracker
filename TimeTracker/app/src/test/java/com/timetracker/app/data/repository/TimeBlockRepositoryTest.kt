package com.timetracker.app.data.repository

import com.timetracker.app.data.local.dao.TimeBlockDao
import com.timetracker.app.data.local.entity.TimeBlockEntity
import com.timetracker.app.data.model.TimeBlock
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class TimeBlockRepositoryTest {

    private lateinit var timeBlockDao: TimeBlockDao
    private lateinit var repository: TimeBlockRepository

    @Before
    fun setup() {
        timeBlockDao = mockk(relaxed = true)
        repository = TimeBlockRepository(timeBlockDao)
    }

    @Test
    fun `getTimeBlocksByDate should return time blocks for specific date`() = runTest {
        // Given
        val date = LocalDate.of(2024, 2, 6)
        val dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val entities = listOf(
            TimeBlockEntity(
                id = 1,
                categoryId = 1,
                title = "工作",
                startTime = dateMillis + 9 * 60 * 60 * 1000,
                endTime = dateMillis + 12 * 60 * 60 * 1000,
                date = dateMillis
            )
        )
        
        coEvery { timeBlockDao.getTimeBlocksByDate(dateMillis) } returns flowOf(entities)

        // When
        val result = repository.getTimeBlocksByDate(date).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("工作", result[0].title)
        coVerify { timeBlockDao.getTimeBlocksByDate(dateMillis) }
    }

    @Test
    fun `insertTimeBlock should call dao insert`() = runTest {
        // Given
        val timeBlock = TimeBlock(
            id = 0,
            categoryId = 1,
            title = "学习",
            startTime = LocalDateTime.of(2024, 2, 6, 14, 0),
            endTime = LocalDateTime.of(2024, 2, 6, 16, 0),
            date = LocalDate.of(2024, 2, 6)
        )
        
        coEvery { timeBlockDao.insertTimeBlock(any()) } returns 1L

        // When
        val result = repository.insertTimeBlock(timeBlock)

        // Then
        assertEquals(1L, result)
        coVerify { timeBlockDao.insertTimeBlock(any()) }
    }

    @Test
    fun `deleteTimeBlock should call dao delete`() = runTest {
        // Given
        val timeBlock = TimeBlock(
            id = 1,
            categoryId = 1,
            title = "会议",
            startTime = LocalDateTime.of(2024, 2, 6, 10, 0),
            endTime = LocalDateTime.of(2024, 2, 6, 11, 0),
            date = LocalDate.of(2024, 2, 6)
        )
        
        coEvery { timeBlockDao.deleteTimeBlock(any()) } just Runs

        // When
        repository.deleteTimeBlock(timeBlock)

        // Then
        coVerify { timeBlockDao.deleteTimeBlock(any()) }
    }

    @Test
    fun `hasOverlappingBlocks should return true when blocks overlap`() = runTest {
        // Given
        val date = LocalDate.of(2024, 2, 6)
        val startTime = LocalDateTime.of(2024, 2, 6, 9, 0)
        val endTime = LocalDateTime.of(2024, 2, 6, 12, 0)
        
        val overlappingBlocks = listOf(
            TimeBlockEntity(
                id = 1,
                categoryId = 1,
                title = "现有工作",
                startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 10 * 60 * 60 * 1000,
                endTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 11 * 60 * 60 * 1000,
                date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        )
        
        coEvery { timeBlockDao.getOverlappingBlocks(any(), any(), any()) } returns overlappingBlocks

        // When
        val result = repository.hasOverlappingBlocks(startTime, endTime, date)

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasOverlappingBlocks should return false when no overlap`() = runTest {
        // Given
        val date = LocalDate.of(2024, 2, 6)
        val startTime = LocalDateTime.of(2024, 2, 6, 9, 0)
        val endTime = LocalDateTime.of(2024, 2, 6, 12, 0)
        
        coEvery { timeBlockDao.getOverlappingBlocks(any(), any(), any()) } returns emptyList()

        // When
        val result = repository.hasOverlappingBlocks(startTime, endTime, date)

        // Then
        assertFalse(result)
    }

    @Test
    fun `timeBlock duration should be calculated correctly`() {
        // Given
        val timeBlock = TimeBlock(
            id = 1,
            categoryId = 1,
            title = "工作",
            startTime = LocalDateTime.of(2024, 2, 6, 9, 0),
            endTime = LocalDateTime.of(2024, 2, 6, 12, 30),
            date = LocalDate.of(2024, 2, 6)
        )

        // Then
        assertEquals(210, timeBlock.durationMinutes)
        assertEquals(3.5f, timeBlock.durationHours, 0.01f)
    }
}
