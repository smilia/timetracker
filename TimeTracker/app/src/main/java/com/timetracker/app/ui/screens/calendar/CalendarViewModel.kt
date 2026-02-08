package com.timetracker.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.model.Category
import com.timetracker.app.data.model.Template
import com.timetracker.app.data.model.TimeBlock
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.data.repository.CategoryRepository
import com.timetracker.app.data.repository.TemplateRepository
import com.timetracker.app.data.repository.TimeBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val timeBlockRepository: TimeBlockRepository,
    private val categoryRepository: CategoryRepository,
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _templates = MutableStateFlow<List<Template>>(emptyList())
    val templates: StateFlow<List<Template>> = _templates.asStateFlow()

    val timeBlocks: StateFlow<List<TimeBlock>> = _selectedDate
        .flatMapLatest { date ->
            timeBlockRepository.getTimeBlocksByDate(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.initializeDefaultCategories()
            categoryRepository.getAllCategories().collect { categories ->
                _categories.value = categories
            }
        }
        viewModelScope.launch {
            // Initialize default templates
            templateRepository.initializeDefaultTemplates()
        }
        viewModelScope.launch {
            // Use getAllTemplatesByUsage to sort templates by usage count
            templateRepository.getAllTemplatesByUsage().collect { templates ->
                _templates.value = templates
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addTimeBlock(
        title: String,
        color: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        note: String? = null,
        timeNature: TimeNature = TimeNature.PRODUCTIVE
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val timeBlock = TimeBlock(
                    color = color,
                    title = title,
                    startTime = startTime,
                    endTime = endTime,
                    date = _selectedDate.value,
                    note = note,
                    timeNature = timeNature
                )
                
                val hasOverlap = timeBlockRepository.hasOverlappingBlocks(
                    startTime, endTime, _selectedDate.value
                )
                
                if (hasOverlap) {
                    _errorMessage.value = "该时间段与其他时间块重叠"
                    return@launch
                }
                
                timeBlockRepository.insertTimeBlock(timeBlock)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTimeBlock(timeBlock: TimeBlock) {
        viewModelScope.launch {
            try {
                val hasOverlap = timeBlockRepository.hasOverlappingBlocks(
                    timeBlock.startTime,
                    timeBlock.endTime,
                    timeBlock.date,
                    excludeId = timeBlock.id
                )
                
                if (hasOverlap) {
                    _errorMessage.value = "该时间段与其他时间块重叠"
                    return@launch
                }
                
                timeBlockRepository.updateTimeBlock(timeBlock)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteTimeBlock(timeBlock: TimeBlock) {
        viewModelScope.launch {
            try {
                timeBlockRepository.deleteTimeBlock(timeBlock)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun moveTimeBlock(timeBlock: TimeBlock, newStartTime: LocalDateTime) {
        viewModelScope.launch {
            try {
                val duration = java.time.Duration.between(timeBlock.startTime, timeBlock.endTime)
                val newEndTime = newStartTime.plus(duration)
                
                val hasOverlap = timeBlockRepository.hasOverlappingBlocks(
                    newStartTime, newEndTime, timeBlock.date, excludeId = timeBlock.id
                )
                
                if (hasOverlap) {
                    _errorMessage.value = "该时间段与其他时间块重叠"
                    return@launch
                }
                
                val updatedBlock = timeBlock.copy(
                    startTime = newStartTime,
                    endTime = newEndTime
                )
                timeBlockRepository.updateTimeBlock(updatedBlock)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun resizeTimeBlock(timeBlock: TimeBlock, newEndTime: LocalDateTime) {
        viewModelScope.launch {
            try {
                if (newEndTime.isBefore(timeBlock.startTime.plusMinutes(15))) {
                    _errorMessage.value = "时间块最少15分钟"
                    return@launch
                }
                
                val hasOverlap = timeBlockRepository.hasOverlappingBlocks(
                    timeBlock.startTime, newEndTime, timeBlock.date, excludeId = timeBlock.id
                )
                
                if (hasOverlap) {
                    _errorMessage.value = "该时间段与其他时间块重叠"
                    return@launch
                }
                
                val updatedBlock = timeBlock.copy(endTime = newEndTime)
                timeBlockRepository.updateTimeBlock(updatedBlock)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getCategoryById(id: Long?): Category? {
        return _categories.value.find { it.id == id }
    }

    fun addTimeBlockFromTemplate(template: Template, startTime: LocalDateTime, endTime: LocalDateTime) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val timeBlock = TimeBlock(
                    color = template.color,
                    title = template.name,
                    startTime = startTime,
                    endTime = endTime,
                    date = _selectedDate.value,
                    timeNature = template.timeNature
                )

                val hasOverlap = timeBlockRepository.hasOverlappingBlocks(
                    startTime, endTime, _selectedDate.value
                )

                if (hasOverlap) {
                    _errorMessage.value = "该时间段与其他时间块重叠"
                    return@launch
                }

                timeBlockRepository.insertTimeBlock(timeBlock)

                // Increment template usage count
                templateRepository.incrementTemplateUsage(template.id)

                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPomodoroBlocks(
        title: String,
        color: String,
        startTime: LocalDateTime,
        cycles: Int,
        note: String? = null,
        timeNature: TimeNature = TimeNature.PRODUCTIVE
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var currentStartTime = startTime
                
                for (i in 1..cycles) {
                    // Work block (25 minutes)
                    val workEndTime = currentStartTime.plusMinutes(25)
                    val workBlock = TimeBlock(
                        color = color,
                        title = "$title 🍅",
                        startTime = currentStartTime,
                        endTime = workEndTime,
                        date = _selectedDate.value,
                        note = if (i == 1 && note != null) "$note (番茄 $i/$cycles)" else "番茄 $i/$cycles",
                        timeNature = timeNature
                    )
                    
                    // Check overlap for work block
                    val hasWorkOverlap = timeBlockRepository.hasOverlappingBlocks(
                        currentStartTime, workEndTime, _selectedDate.value
                    )
                    
                    if (hasWorkOverlap) {
                        _errorMessage.value = "第${i}个番茄工作时间段与其他时间块重叠"
                        return@launch
                    }
                    
                    timeBlockRepository.insertTimeBlock(workBlock)
                    
                    // Break block (5 minutes) - only if not the last cycle
                    if (i < cycles) {
                        val breakStartTime = workEndTime
                        val breakEndTime = breakStartTime.plusMinutes(5)
                        val breakBlock = TimeBlock(
                            color = "#FF9800", // Orange for break
                            title = "☕ 休息",
                            startTime = breakStartTime,
                            endTime = breakEndTime,
                            date = _selectedDate.value,
                            note = "番茄 $i 后的休息",
                            timeNature = TimeNature.NEUTRAL // Break is neutral
                        )
                        
                        // Check overlap for break block
                        val hasBreakOverlap = timeBlockRepository.hasOverlappingBlocks(
                            breakStartTime, breakEndTime, _selectedDate.value
                        )
                        
                        if (hasBreakOverlap) {
                            _errorMessage.value = "第${i}个番茄休息时间段与其他时间块重叠"
                            return@launch
                        }
                        
                        timeBlockRepository.insertTimeBlock(breakBlock)
                        currentStartTime = breakEndTime
                    } else {
                        currentStartTime = workEndTime
                    }
                }
                
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
