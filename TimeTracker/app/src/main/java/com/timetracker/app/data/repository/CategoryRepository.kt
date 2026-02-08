package com.timetracker.app.data.repository

import com.timetracker.app.data.local.dao.CategoryDao
import com.timetracker.app.data.model.Category
import com.timetracker.app.data.model.toEntity
import com.timetracker.app.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.toModel() }
        }
    }
    
    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toModel()
    }
    
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }
    
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }
    
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }
    
    suspend fun initializeDefaultCategories() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaultCategories = listOf(
                Category(name = "工作", color = "#5B9BD5", icon = "work", sortOrder = 0),      // Soft Blue
                Category(name = "学习", color = "#70AD47", icon = "school", sortOrder = 1),   // Soft Green
                Category(name = "休息", color = "#ED7D31", icon = "coffee", sortOrder = 2),   // Soft Orange
                Category(name = "运动", color = "#E85D75", icon = "fitness_center", sortOrder = 3), // Soft Pink
                Category(name = "娱乐", color = "#9F6DD3", icon = "sports_esports", sortOrder = 4), // Soft Purple
                Category(name = "阅读", color = "#4DB3D8", icon = "menu_book", sortOrder = 5), // Soft Teal
                Category(name = "会议", color = "#E15759", icon = "groups", sortOrder = 6),   // Soft Red
                Category(name = "其他", color = "#A5A5A5", icon = "more_horiz", sortOrder = 7) // Soft Gray
            )
            categoryDao.insertCategories(defaultCategories.map { it.toEntity() })
        }
    }
}
