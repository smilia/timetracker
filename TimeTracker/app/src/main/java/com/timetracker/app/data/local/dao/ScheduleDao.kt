package com.timetracker.app.data.local.dao

import androidx.room.*
import com.timetracker.app.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    
    @Query("SELECT * FROM schedules WHERE startTime >= :startTime ORDER BY startTime ASC")
    fun getUpcomingSchedules(startTime: Long): Flow<List<ScheduleEntity>>
    
    @Query("SELECT * FROM schedules WHERE startTime BETWEEN :startTime AND :endTime ORDER BY startTime ASC")
    fun getSchedulesBetween(startTime: Long, endTime: Long): Flow<List<ScheduleEntity>>
    
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long
    
    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)
    
    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
    
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)
}
