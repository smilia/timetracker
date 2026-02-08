package com.timetracker.app.data.repository

import com.timetracker.app.data.local.dao.TemplateDao
import com.timetracker.app.data.model.Template
import com.timetracker.app.data.model.toEntity
import com.timetracker.app.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val templateDao: TemplateDao
) {
    fun getAllTemplates(): Flow<List<Template>> {
        return templateDao.getAllTemplates().map { list ->
            list.map { it.toModel() }
        }
    }

    fun getAllTemplatesByUsage(): Flow<List<Template>> {
        return templateDao.getAllTemplatesByUsage().map { list ->
            list.map { it.toModel() }
        }
    }

    fun getFrequentTemplates(): Flow<List<Template>> {
        return templateDao.getFrequentTemplates().map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun getTemplateById(id: Long): Template? {
        return templateDao.getTemplateById(id)?.toModel()
    }

    suspend fun insertTemplate(template: Template): Long {
        return templateDao.insertTemplate(template.toEntity())
    }

    suspend fun updateTemplate(template: Template) {
        templateDao.updateTemplate(template.toEntity())
    }

    suspend fun deleteTemplate(template: Template) {
        templateDao.deleteTemplate(template.toEntity())
    }

    suspend fun incrementTemplateUsage(templateId: Long) {
        templateDao.incrementUsageCount(templateId)
    }
    
    suspend fun initializeDefaultTemplates() {
        // Only initialize if no templates exist
        val existingCount = templateDao.getTemplateCount()
        if (existingCount > 0) return
        
        val defaultTemplates = listOf(
            Template(color = "#5B9BD5", name = "深度工作", defaultDuration = 15, isFrequent = true),
            Template(color = "#5B9BD5", name = "常规工作", defaultDuration = 15),
            Template(color = "#70AD47", name = "学习", defaultDuration = 15, isFrequent = true),
            Template(color = "#70AD47", name = "阅读", defaultDuration = 15),
            Template(color = "#ED7D31", name = "休息", defaultDuration = 15, isFrequent = true),
            Template(color = "#ED7D31", name = "午休", defaultDuration = 15),
            Template(color = "#E85D75", name = "运动", defaultDuration = 15, isFrequent = true),
            Template(color = "#9F6DD3", name = "会议", defaultDuration = 15, isFrequent = true),
            Template(color = "#9F6DD3", name = "短会", defaultDuration = 15)
        )
        
        defaultTemplates.forEach { templateDao.insertTemplate(it.toEntity()) }
    }
}
