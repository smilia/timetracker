package com.timetracker.app.data.repository

import com.timetracker.app.data.local.dao.CategoryDao
import com.timetracker.app.data.local.entity.CategoryEntity
import com.timetracker.app.data.model.Category
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryTest {

    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() {
        categoryDao = mockk(relaxed = true)
        repository = CategoryRepository(categoryDao)
    }

    @Test
    fun `getAllCategories should return all categories`() = runTest {
        // Given
        val entities = listOf(
            CategoryEntity(id = 1, name = "工作", color = "#FF0000", icon = "work"),
            CategoryEntity(id = 2, name = "学习", color = "#00FF00", icon = "school")
        )
        coEvery { categoryDao.getAllCategories() } returns flowOf(entities)

        // When
        val result = repository.getAllCategories().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("工作", result[0].name)
        assertEquals("学习", result[1].name)
    }

    @Test
    fun `getCategoryById should return category when exists`() = runTest {
        // Given
        val entity = CategoryEntity(id = 1, name = "工作", color = "#FF0000", icon = "work")
        coEvery { categoryDao.getCategoryById(1) } returns entity

        // When
        val result = repository.getCategoryById(1)

        // Then
        assertNotNull(result)
        assertEquals("工作", result?.name)
    }

    @Test
    fun `getCategoryById should return null when not exists`() = runTest {
        // Given
        coEvery { categoryDao.getCategoryById(999) } returns null

        // When
        val result = repository.getCategoryById(999)

        // Then
        assertNull(result)
    }

    @Test
    fun `insertCategory should call dao insert`() = runTest {
        // Given
        val category = Category(name = "新分类", color = "#0000FF", icon = "new")
        coEvery { categoryDao.insertCategory(any()) } returns 1L

        // When
        val result = repository.insertCategory(category)

        // Then
        assertEquals(1L, result)
        coVerify { categoryDao.insertCategory(any()) }
    }

    @Test
    fun `updateCategory should call dao update`() = runTest {
        // Given
        val category = Category(id = 1, name = "更新分类", color = "#FF0000", icon = "work")
        coEvery { categoryDao.updateCategory(any()) } just Runs

        // When
        repository.updateCategory(category)

        // Then
        coVerify { categoryDao.updateCategory(any()) }
    }

    @Test
    fun `deleteCategory should call dao delete`() = runTest {
        // Given
        val category = Category(id = 1, name = "删除分类", color = "#FF0000", icon = "work")
        coEvery { categoryDao.deleteCategory(any()) } just Runs

        // When
        repository.deleteCategory(category)

        // Then
        coVerify { categoryDao.deleteCategory(any()) }
    }

    @Test
    fun `initializeDefaultCategories should insert defaults when empty`() = runTest {
        // Given
        coEvery { categoryDao.getCategoryCount() } returns 0
        coEvery { categoryDao.insertCategories(any()) } just Runs

        // When
        repository.initializeDefaultCategories()

        // Then
        coVerify { categoryDao.insertCategories(any()) }
    }

    @Test
    fun `initializeDefaultCategories should not insert when not empty`() = runTest {
        // Given
        coEvery { categoryDao.getCategoryCount() } returns 5

        // When
        repository.initializeDefaultCategories()

        // Then
        coVerify(exactly = 0) { categoryDao.insertCategories(any()) }
    }
}
