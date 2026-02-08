package com.timetracker.app.data.local.dao

import androidx.room.*
import com.timetracker.app.data.local.entity.TimeBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeBlockDao {
    
    @Query("SELECT * FROM time_blocks WHERE date = :date ORDER BY startTime ASC")
    fun getTimeBlocksByDate(date: Long): Flow<List<TimeBlockEntity>>
    
    @Query("SELECT * FROM time_blocks WHERE date BETWEEN :startDate AND :endDate ORDER BY startTime ASC")
    fun getTimeBlocksBetweenDates(startDate: Long, endDate: Long): Flow<List<TimeBlockEntity>>
    
    @Query("SELECT * FROM time_blocks WHERE id = :id")
    suspend fun getTimeBlockById(id: Long): TimeBlockEntity?
    
    @Query("SELECT * FROM time_blocks WHERE startTime <= :time AND endTime > :time LIMIT 1")
    suspend fun getCurrentTimeBlock(time: Long): TimeBlockEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeBlock(timeBlock: TimeBlockEntity): Long
    
    @Update
    suspend fun updateTimeBlock(timeBlock: TimeBlockEntity)
    
    @Delete
    suspend fun deleteTimeBlock(timeBlock: TimeBlockEntity)
    
    @Query("DELETE FROM time_blocks WHERE id = :id")
    suspend fun deleteTimeBlockById(id: Long)
    
    @Query("SELECT * FROM time_blocks WHERE startTime < :endTime AND endTime > :startTime AND date = :date")
    suspend fun getOverlappingBlocks(startTime: Long, endTime: Long, date: Long): List<TimeBlockEntity>

    // For widget - synchronous queries
    @Query("SELECT * FROM time_blocks WHERE date >= :startDate AND date < :endDate ORDER BY startTime ASC")
    suspend fun getTimeBlocksForWidget(startDate: Long, endDate: Long): List<TimeBlockEntity>
}
