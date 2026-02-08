package com.timetracker.app.data.local.dao

import androidx.room.*
import com.timetracker.app.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Query("SELECT * FROM templates ORDER BY isFrequent DESC, usageCount DESC, name ASC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates ORDER BY usageCount DESC, name ASC")
    fun getAllTemplatesByUsage(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE isFrequent = 1")
    fun getFrequentTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): TemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun getTemplateCount(): Int

    @Query("UPDATE templates SET usageCount = usageCount + 1 WHERE id = :templateId")
    suspend fun incrementUsageCount(templateId: Long)
}
