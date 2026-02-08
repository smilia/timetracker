package com.timetracker.app.ui.screens.templates

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.timetracker.app.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TemplatesScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
        // Navigate to templates tab
        composeTestRule.onNodeWithText("模板").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun templatesScreen_displaysTitle() {
        // Then
        composeTestRule.onNodeWithText("时间块模板").assertIsDisplayed()
    }

    @Test
    fun templatesScreen_displaysAddButton() {
        // Then
        composeTestRule.onNodeWithContentDescription("添加模板").assertIsDisplayed()
    }

    @Test
    fun templatesScreen_clickAddButton_showsDialog() {
        // When
        composeTestRule.onNodeWithContentDescription("添加模板").performClick()

        // Then
        composeTestRule.onNodeWithText("添加模板").assertIsDisplayed()
        composeTestRule.onNodeWithText("模板名称").assertIsDisplayed()
    }

    @Test
    fun templatesScreen_dialogShowsCategorySelection() {
        // When
        composeTestRule.onNodeWithContentDescription("添加模板").performClick()

        // Then
        composeTestRule.onNodeWithText("分类").assertIsDisplayed()
    }

    @Test
    fun templatesScreen_dialogShowsDurationInput() {
        // When
        composeTestRule.onNodeWithContentDescription("添加模板").performClick()

        // Then
        composeTestRule.onNodeWithText("默认时长（分钟）").assertIsDisplayed()
    }

    @Test
    fun templatesScreen_dialogCanBeCancelled() {
        // Given
        composeTestRule.onNodeWithContentDescription("添加模板").performClick()

        // When
        composeTestRule.onNodeWithText("取消").performClick()

        // Then
        composeTestRule.onNodeWithText("添加模板").assertDoesNotExist()
    }

    @Test
    fun templatesScreen_displaysDefaultTemplates() {
        // Wait for templates to load
        composeTestRule.waitForIdle()

        // Then - Default templates should be displayed
        // Note: This depends on the default templates being initialized
        composeTestRule.onNodeWithText("所有模板").assertIsDisplayed()
    }
}
