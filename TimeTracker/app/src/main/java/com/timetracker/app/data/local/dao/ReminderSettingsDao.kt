package com.timetracker.app.data.local.dao

import androidx.room.*
import com.timetracker.app.data.local.entity.ReminderSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderSettingsDao {
    
    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    fun getSettings(): Flow<ReminderSettingsEntity?>
    
    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    suspend fun getSettingsSync(): ReminderSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: ReminderSettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: ReminderSettingsEntity)
}
