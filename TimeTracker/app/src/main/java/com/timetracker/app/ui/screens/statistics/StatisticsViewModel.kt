package com.timetracker.app.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.model.Category
import com.timetracker.app.data.model.TimeBlock
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.data.repository.CategoryRepository
import com.timetracker.app.data.repository.TimeBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ColorStat(
    val color: String,
    val colorName: String,
    val totalMinutes: Long,
    val percentage: Float
)

data class DailyStat(
    val date: LocalDate,
    val totalMinutes: Long,
    val colorBreakdown: Map<String, Long>
)

data class ProductiveStat(
    val productiveMinutes: Long,
    val unproductiveMinutes: Long,
    val productivePercentage: Float,
    val dominantNature: TimeNature // 主导的时间性质
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val timeBlockRepository: TimeBlockRepository,
    private val categoryRepository: CategoryRepository,
    private val templateRepository: com.timetracker.app.data.repository.TemplateRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(DateRange.WEEK)
    val selectedRange: StateFlow<DateRange> = _selectedRange.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    private val _templates = MutableStateFlow<Map<String, com.timetracker.app.data.model.Template>>(emptyMap())
    
    private val _timeBlocks = MutableStateFlow<List<TimeBlock>>(emptyList())

    val colorStats: StateFlow<List<ColorStat>> = combine(
        _timeBlocks,
        _templates
    ) { blocks, templates ->
        calculateColorStats(blocks, templates)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyStats: StateFlow<List<DailyStat>> = _timeBlocks.map { blocks ->
        calculateDailyStats(blocks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTime: StateFlow<Long> = _timeBlocks.map { blocks ->
        blocks.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageDailyTime: StateFlow<Long> = combine(
        _timeBlocks,
        _selectedRange
    ) { blocks, range ->
        if (blocks.isEmpty()) 0
        else {
            val days = when (range) {
                DateRange.WEEK -> 7
                DateRange.MONTH -> 30
                DateRange.YEAR -> 365
            }
            blocks.sumOf { it.durationMinutes } / days
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val productiveStats: StateFlow<ProductiveStat> = combine(
        _timeBlocks,
        _templates
    ) { blocks, templates ->
        calculateProductiveStats(blocks, templates)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductiveStat(0, 0, 0f, TimeNature.PRODUCTIVE))

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _categories.value = categories
            }
        }

        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { templates ->
                // Create a map of template name to template for lookup
                _templates.value = templates.associateBy { it.name }
            }
        }

        viewModelScope.launch {
            _selectedRange.collect { range ->
                loadTimeBlocks(range)
            }
        }
    }

    private suspend fun loadTimeBlocks(range: DateRange) {
        val endDate = LocalDate.now()
        val startDate = when (range) {
            DateRange.WEEK -> endDate.minusWeeks(1)
            DateRange.MONTH -> endDate.minusMonths(1)
            DateRange.YEAR -> endDate.minusYears(1)
        }
        
        timeBlockRepository.getTimeBlocksBetweenDates(startDate, endDate).collect { blocks ->
            _timeBlocks.value = blocks
        }
    }

    fun setDateRange(range: DateRange) {
        _selectedRange.value = range
    }

    private fun calculateColorStats(
        blocks: List<TimeBlock>,
        templates: Map<String, com.timetracker.app.data.model.Template>
    ): List<ColorStat> {
        if (blocks.isEmpty()) return emptyList()

        val totalMinutes = blocks.sumOf { it.durationMinutes }
        val colorMinutes = blocks.groupBy { it.color }
            .mapValues { (_, blocks) -> blocks.sumOf { it.durationMinutes } }

        return colorMinutes.mapNotNull { (color, minutes) ->
            if (minutes > 0) {
                // Find template name for this color
                val templateName = templates.values.find { it.color == color }?.name ?: "未分类"
                ColorStat(
                    color = color,
                    colorName = templateName,
                    totalMinutes = minutes,
                    percentage = minutes.toFloat() / totalMinutes * 100
                )
            } else null
        }.sortedByDescending { it.totalMinutes }
    }

    private fun calculateDailyStats(blocks: List<TimeBlock>): List<DailyStat> {
        return blocks.groupBy { it.date }
            .map { (date, dayBlocks) ->
                DailyStat(
                    date = date,
                    totalMinutes = dayBlocks.sumOf { it.durationMinutes },
                    colorBreakdown = dayBlocks.groupBy { it.color }
                        .mapValues { (_, bs) -> bs.sumOf { it.durationMinutes } }
                )
            }.sortedBy { it.date }
    }

    private fun calculateProductiveStats(
        blocks: List<TimeBlock>,
        templates: Map<String, com.timetracker.app.data.model.Template>
    ): ProductiveStat {
        if (blocks.isEmpty()) return ProductiveStat(0, 0, 0f, TimeNature.PRODUCTIVE)

        var productiveMinutes = 0L
        var unproductiveMinutes = 0L
        var neutralMinutes = 0L

        blocks.forEach { block ->
            // 只统计 PRODUCTIVE 和 UNPRODUCTIVE，不统计 NEUTRAL
            when (block.timeNature) {
                TimeNature.PRODUCTIVE -> productiveMinutes += block.durationMinutes
                TimeNature.UNPRODUCTIVE -> unproductiveMinutes += block.durationMinutes
                TimeNature.NEUTRAL -> neutralMinutes += block.durationMinutes // 中性不计入效率分析
            }
        }

        val totalMinutes = productiveMinutes + unproductiveMinutes
        val productivePercentage = if (totalMinutes > 0) {
            productiveMinutes.toFloat() / totalMinutes * 100
        } else 0f

        // 判断主导性质（元气满满 vs 摸鱼时光）
        val dominantNature = if (productiveMinutes >= unproductiveMinutes) {
            TimeNature.PRODUCTIVE
        } else {
            TimeNature.UNPRODUCTIVE
        }

        return ProductiveStat(productiveMinutes, unproductiveMinutes, productivePercentage, dominantNature)
    }

    enum class DateRange {
        WEEK, MONTH, YEAR
    }
}
