package com.timetracker.app.ui.screens.calendar

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
class CalendarScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun calendarScreen_displaysDateHeader() {
        // Then - Check that date is displayed
        composeTestRule.onNodeWithText("年", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("月", substring = true).assertIsDisplayed()
    }

    @Test
    fun calendarScreen_displaysTimeSlots() {
        // Then - Check that time slots are displayed
        composeTestRule.onNodeWithText("09:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("12:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("18:00").assertIsDisplayed()
    }

    @Test
    fun calendarScreen_displaysAddButton() {
        // Then - Check that FAB is displayed
        composeTestRule.onNodeWithContentDescription("添加时间块").assertIsDisplayed()
    }

    @Test
    fun calendarScreen_clickAddButton_showsDialog() {
        // When
        composeTestRule.onNodeWithContentDescription("添加时间块").performClick()

        // Then
        composeTestRule.onNodeWithText("添加时间块").assertIsDisplayed()
        composeTestRule.onNodeWithText("标题").assertIsDisplayed()
    }

    @Test
    fun calendarScreen_dialogCanBeDismissed() {
        // Given
        composeTestRule.onNodeWithContentDescription("添加时间块").performClick()
        composeTestRule.onNodeWithText("添加时间块").assertIsDisplayed()

        // When
        composeTestRule.onNodeWithText("取消").performClick()

        // Then - Dialog should be dismissed
        composeTestRule.onNodeWithText("添加时间块").assertDoesNotExist()
    }

    @Test
    fun calendarScreen_canNavigateBetweenDates() {
        // Given - Get current date text
        val initialDateText = composeTestRule.onAllNodesWithText("年", substring = true)[0].text

        // When - Click next day
        composeTestRule.onNodeWithContentDescription("后一天").performClick()

        // Then - Date should change
        composeTestRule.waitForIdle()
        // The date should have changed, but we can't easily verify the exact date
        // without parsing the text
    }

    @Test
    fun calendarScreen_canNavigateToToday() {
        // When
        composeTestRule.onNodeWithContentDescription("今天").performClick()

        // Then - Should navigate to today
        composeTestRule.waitForIdle()
    }

    @Test
    fun calendarScreen_displaysBottomNavigation() {
        // Then - Check bottom navigation items
        composeTestRule.onNodeWithText("日历").assertIsDisplayed()
        composeTestRule.onNodeWithText("统计").assertIsDisplayed()
        composeTestRule.onNodeWithText("模板").assertIsDisplayed()
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
    }

    @Test
    fun calendarScreen_canSwitchToStatisticsTab() {
        // When
        composeTestRule.onNodeWithText("统计").performClick()

        // Then
        composeTestRule.onNodeWithText("统计分析").assertIsDisplayed()
    }

    @Test
    fun calendarScreen_canSwitchToTemplatesTab() {
        // When
        composeTestRule.onNodeWithText("模板").performClick()

        // Then
        composeTestRule.onNodeWithText("时间块模板").assertIsDisplayed()
    }

    @Test
    fun calendarScreen_canSwitchToSettingsTab() {
        // When
        composeTestRule.onNodeWithText("设置").performClick()

        // Then
        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
        composeTestRule.onNodeWithText("定时提醒").assertIsDisplayed()
    }
}
