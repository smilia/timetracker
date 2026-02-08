package com.timetracker.app.ui.screens.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.timetracker.app.TestData
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

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var timeBlockRepository: TimeBlockRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        timeBlockRepository = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)

        // Setup default mocks
        coEvery { categoryRepository.getAllCategories() } returns flowOf(TestData.allCategories)
        coEvery { timeBlockRepository.getTimeBlocksBetweenDates(any(), any()) } returns flowOf(TestData.allTimeBlocks)

        viewModel = StatisticsViewModel(timeBlockRepository, categoryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have WEEK range selected`() = runTest {
        // Then
        assertEquals(StatisticsViewModel.DateRange.WEEK, viewModel.selectedRange.value)
    }

    @Test
    fun `setDateRange should update selected range`() = runTest {
        // When
        viewModel.setDateRange(StatisticsViewModel.DateRange.MONTH)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(StatisticsViewModel.DateRange.MONTH, viewModel.selectedRange.value)
    }

    @Test
    fun `categoryStats should calculate correct percentages`() = runTest {
        // Given - 3 hours work, 2 hours study, 1 hour rest = 6 hours total
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val stats = viewModel.categoryStats.value

        // Then
        assertEquals(3, stats.size)

        val workStat = stats.find { it.category.id == 1L }
        assertNotNull(workStat)
        assertEquals(180, workStat?.totalMinutes) // 3 hours
        assertEquals(50f, workStat?.percentage ?: 0f, 0.1f) // 3/6 = 50%

        val studyStat = stats.find { it.category.id == 2L }
        assertNotNull(studyStat)
        assertEquals(120, studyStat?.totalMinutes) // 2 hours
        assertEquals(33.3f, studyStat?.percentage ?: 0f, 0.5f) // 2/6 = 33.3%
    }

    @Test
    fun `totalTime should sum all time blocks`() = runTest {
        // Given - 3 + 2 + 1 = 6 hours = 360 minutes
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val total = viewModel.totalTime.value

        // Then
        assertEquals(360, total)
    }

    @Test
    fun `averageDailyTime should calculate correctly for week`() = runTest {
        // Given - 360 minutes / 7 days
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val average = viewModel.averageDailyTime.value

        // Then
        assertEquals(51, average) // 360 / 7 = 51.4, truncated to 51
    }

    @Test
    fun `averageDailyTime should calculate correctly for month`() = runTest {
        // Given
        viewModel.setDateRange(StatisticsViewModel.DateRange.MONTH)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val average = viewModel.averageDailyTime.value

        // Then - 360 minutes / 30 days
        assertEquals(12, average) // 360 / 30 = 12
    }

    @Test
    fun `categoryStats should be sorted by total minutes descending`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val stats = viewModel.categoryStats.value

        // Then
        assertTrue(stats[0].totalMinutes >= stats[1].totalMinutes)
        assertTrue(stats[1].totalMinutes >= stats.getOrNull(2)?.totalMinutes ?: Long.MIN_VALUE)
    }

    @Test
    fun `dailyStats should group by date`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val dailyStats = viewModel.dailyStats.value

        // Then
        assertTrue(dailyStats.isNotEmpty())
        // All test blocks are on the same date
        assertEquals(1, dailyStats.size)
        assertEquals(360, dailyStats[0].totalMinutes)
    }

    @Test
    fun `categoryStats should be empty when no time blocks`() = runTest {
        // Given
        coEvery { timeBlockRepository.getTimeBlocksBetweenDates(any(), any()) } returns flowOf(emptyList())
        viewModel = StatisticsViewModel(timeBlockRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val stats = viewModel.categoryStats.value

        // Then
        assertTrue(stats.isEmpty())
    }

    @Test
    fun `totalTime should be zero when no time blocks`() = runTest {
        // Given
        coEvery { timeBlockRepository.getTimeBlocksBetweenDates(any(), any()) } returns flowOf(emptyList())
        viewModel = StatisticsViewModel(timeBlockRepository, categoryRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val total = viewModel.totalTime.value

        // Then
        assertEquals(0, total)
    }
}
