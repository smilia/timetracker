package com.timetracker.app.ui.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.model.Template
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _templates = MutableStateFlow<List<Template>>(emptyList())
    val templates: StateFlow<List<Template>> = _templates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Custom labels for productive/unproductive
    var productiveLabel: String = "元气满满"
        private set
    var unproductiveLabel: String = "摸鱼时光"
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { templates ->
                _templates.value = templates
                
                // Initialize default templates if none exist
                if (templates.isEmpty()) {
                    templateRepository.initializeDefaultTemplates()
                }
            }
        }
    }

    fun addTemplate(name: String, color: String, defaultDuration: Int, isFrequent: Boolean = false, timeNature: TimeNature = TimeNature.PRODUCTIVE) {
        viewModelScope.launch {
            val template = Template(
                name = name,
                color = color,
                defaultDuration = defaultDuration,
                isFrequent = isFrequent,
                timeNature = timeNature
            )
            templateRepository.insertTemplate(template)
        }
    }

    fun updateTemplate(template: Template) {
        viewModelScope.launch {
            templateRepository.updateTemplate(template)
        }
    }

    fun deleteTemplate(template: Template) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(template)
        }
    }

    fun toggleFrequent(template: Template) {
        viewModelScope.launch {
            templateRepository.updateTemplate(template.copy(isFrequent = !template.isFrequent))
        }
    }

    // Custom labels
    fun setProductiveLabel(label: String) {
        productiveLabel = label
    }

    fun setUnproductiveLabel(label: String) {
        unproductiveLabel = label
    }
}
