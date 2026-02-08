package com.timetracker.app.ui.screens.calendar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.timetracker.app.data.model.Category
import com.timetracker.app.data.model.TimeBlock
import com.timetracker.app.data.repository.CategoryRepository
import com.timetracker.app.data.repository.TimeBlockRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var timeBlockRepository: TimeBlockRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        timeBlockRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        
        // Setup default mocks
        coEvery { categoryRepository.initializeDefaultCategories() } just Runs
        coEvery { categoryRepository.getAllCategories() } returns flowOf(emptyList())
        coEvery { timeBlockRepository.getTimeBlocksByDate(any()) } returns flowOf(emptyList())
        
        viewModel = CalendarViewModel(timeBlockRepository, categoryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have current date selected`() = runTest {
        // Given
        val today = LocalDate.now()
        
        // Then
        assertEquals(today, viewModel.selectedDate.value)
    }

    @Test
    fun `selectDate should update selected date`() = runTest {
        // Given
        val newDate = LocalDate.of(2024, 2, 15)
        
        // When
        viewModel.selectDate(newDate)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertEquals(newDate, viewModel.selectedDate.value)
    }

    @Test
    fun `addTimeBlock should call repository when no overlap`() = runTest {
        // Given
        val title = "工作会议"
        val categoryId = 1L
        val startTime = LocalDateTime.of(2024, 2, 6, 9, 0)
        val endTime = LocalDateTime.of(2024, 2, 6, 10, 0)
        
        coEvery { timeBlockRepository.hasOverlappingBlocks(any(), any(), any()) } returns false
        coEvery { timeBlockRepository.insertTimeBlock(any()) } returns 1L
        
        // When
        viewModel.addTimeBlock(title, categoryId, startTime, endTime)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { timeBlockRepository.insertTimeBlock(any()) }
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addTimeBlock should show error when overlap exists`() = runTest {
        // Given
        val title = "工作会议"
        val categoryId = 1L
        val startTime = LocalDateTime.of(2024, 2, 6, 9, 0)
        val endTime = LocalDateTime.of(2024, 2, 6, 10, 0)
        
        coEvery { timeBlockRepository.hasOverlappingBlocks(any(), any(), any()) } returns true
        
        // When
        viewModel.addTimeBlock(title, categoryId, startTime, endTime)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify(exactly = 0) { timeBlockRepository.insertTimeBlock(any()) }
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value?.contains("重叠") == true)
    }

    @Test
    fun `moveTimeBlock should update time block position`() = runTest {
        // Given
        val timeBlock = TimeBlock(
            id = 1,
            categoryId = 1,
            title = "工作",
            startTime = LocalDateTime.of(2024, 2, 6, 9, 0),
            endTime = LocalDateTime.of(2024, 2, 6, 11, 0),
            date = LocalDate.of(2024, 2, 6)
        )
        val newStartTime = LocalDateTime.of(2024, 2, 6, 14, 0)
        
        coEvery { timeBlockRepository.hasOverlappingBlocks(any(), any(), any(), any()) } returns false
        coEvery { timeBlockRepository.updateTimeBlock(any()) } just Runs
        
        // When
        viewModel.moveTimeBlock(timeBlock, newStartTime)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { timeBlockRepository.updateTimeBlock(any()) }
    }

    @Test
    fun `resizeTimeBlock should enforce minimum 15 minutes`() = runTest {
        // Given
        val timeBlock = TimeBlock(
            id = 1,
            categoryId = 1,
            title = "工作",
            startTime = LocalDateTime.of(2024, 2, 6, 9, 0),
            endTime = LocalDateTime.of(2024, 2, 6, 10, 0),
            date = LocalDate.of(2024, 2, 6)
        )
        // Try to resize to less than 15 minutes
        val newEndTime = LocalDateTime.of(2024, 2, 6, 9, 10)
        
        // When
        viewModel.resizeTimeBlock(timeBlock, newEndTime)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify(exactly = 0) { timeBlockRepository.updateTimeBlock(any()) }
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value?.contains("15分钟") == true)
    }

    @Test
    fun `deleteTimeBlock should call repository delete`() = runTest {
        // Given
        val timeBlock = TimeBlock(
            id = 1,
            categoryId = 1,
            title = "工作",
            startTime = LocalDateTime.of(2024, 2, 6, 9, 0),
            endTime = LocalDateTime.of(2024, 2, 6, 10, 0),
            date = LocalDate.of(2024, 2, 6)
        )
        
        coEvery { timeBlockRepository.deleteTimeBlock(any()) } just Runs
        
        // When
        viewModel.deleteTimeBlock(timeBlock)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { timeBlockRepository.deleteTimeBlock(timeBlock) }
    }

    @Test
    fun `getCategoryById should return correct category`() = runTest {
        // Given
        val categories = listOf(
            Category(id = 1, name = "工作", color = "#FF0000", icon = "work"),
            Category(id = 2, name = "学习", color = "#00FF00", icon = "school")
        )
        
        coEvery { categoryRepository.getAllCategories() } returns flowOf(categories)
        
        // Re-create viewModel to trigger category loading
        viewModel = CalendarViewModel(timeBlockRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        val result = viewModel.getCategoryById(1)
        
        // Then
        assertEquals("工作", result?.name)
    }

    @Test
    fun `clearError should reset error message`() = runTest {
        // Given
        viewModel.addTimeBlock("Test", null, LocalDateTime.now(), LocalDateTime.now())
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Assume error occurred
        assertNotNull(viewModel.errorMessage.value)
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.errorMessage.value)
    }
}
