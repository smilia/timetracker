package com.timetracker.app.data.repository

import com.timetracker.app.data.local.dao.TimeBlockDao
import com.timetracker.app.data.model.TimeBlock
import com.timetracker.app.data.model.toEntity
import com.timetracker.app.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeBlockRepository @Inject constructor(
    private val timeBlockDao: TimeBlockDao
) {
    fun getTimeBlocksByDate(date: LocalDate): Flow<List<TimeBlock>> {
        val dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return timeBlockDao.getTimeBlocksByDate(dateMillis).map { list ->
            list.map { it.toModel() }
        }
    }
    
    fun getTimeBlocksBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<TimeBlock>> {
        val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return timeBlockDao.getTimeBlocksBetweenDates(startMillis, endMillis).map { list ->
            list.map { it.toModel() }
        }
    }
    
    suspend fun getTimeBlockById(id: Long): TimeBlock? {
        return timeBlockDao.getTimeBlockById(id)?.toModel()
    }
    
    suspend fun getCurrentTimeBlock(): TimeBlock? {
        val currentTime = System.currentTimeMillis()
        return timeBlockDao.getCurrentTimeBlock(currentTime)?.toModel()
    }
    
    suspend fun insertTimeBlock(timeBlock: TimeBlock): Long {
        return timeBlockDao.insertTimeBlock(timeBlock.toEntity())
    }
    
    suspend fun updateTimeBlock(timeBlock: TimeBlock) {
        timeBlockDao.updateTimeBlock(timeBlock.toEntity())
    }
    
    suspend fun deleteTimeBlock(timeBlock: TimeBlock) {
        timeBlockDao.deleteTimeBlock(timeBlock.toEntity())
    }
    
    suspend fun deleteTimeBlockById(id: Long) {
        timeBlockDao.deleteTimeBlockById(id)
    }
    
    suspend fun hasOverlappingBlocks(
        startTime: java.time.LocalDateTime,
        endTime: java.time.LocalDateTime,
        date: LocalDate,
        excludeId: Long? = null
    ): Boolean {
        val startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val overlapping = timeBlockDao.getOverlappingBlocks(startMillis, endMillis, dateMillis)
        return if (excludeId != null) {
            overlapping.any { it.id != excludeId }
        } else {
            overlapping.isNotEmpty()
        }
    }
}
