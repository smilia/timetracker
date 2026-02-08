package com.timetracker.app.ui.screens.settings

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetracker.app.data.local.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val dataStore = application.dataStore

    // Preference Keys
    companion object {
        val POMODORO_NOTIFICATION_ENABLED = booleanPreferencesKey("pomodoro_notification_enabled")
        val POMODORO_COMPLETION_ALERT = booleanPreferencesKey("pomodoro_completion_alert")
        val BLOCK_START_REMINDER = booleanPreferencesKey("block_start_reminder")
        val BLOCK_END_REMINDER = booleanPreferencesKey("block_end_reminder")
        val ENHANCED_NOTIFICATION_ENABLED = booleanPreferencesKey("enhanced_notification_enabled")
        val ATOMIC_NOTIFICATION_ENABLED = booleanPreferencesKey("atomic_notification_enabled")
        val INTERVAL_REMINDER_ENABLED = booleanPreferencesKey("interval_reminder_enabled")
        val INTERVAL_REMINDER_MINUTES = stringPreferencesKey("interval_reminder_minutes")
        
        // 默认提醒时间点（每小时的分钟数）
        val DEFAULT_REMINDER_MINUTES = listOf(15, 30, 45, 0) // 0 表示整点（60分）
        val DO_NOT_DISTURB_ENABLED = booleanPreferencesKey("do_not_disturb_enabled")
        val DO_NOT_DISTURB_START_HOUR = intPreferencesKey("do_not_disturb_start_hour")
        val DO_NOT_DISTURB_START_MINUTE = intPreferencesKey("do_not_disturb_start_minute")
        val DO_NOT_DISTURB_END_HOUR = intPreferencesKey("do_not_disturb_end_hour")
        val DO_NOT_DISTURB_END_MINUTE = intPreferencesKey("do_not_disturb_end_minute")
    }

    // Flows for settings
    val pomodoroNotificationEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[POMODORO_NOTIFICATION_ENABLED] ?: true
        }

    val pomodoroCompletionAlert: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[POMODORO_COMPLETION_ALERT] ?: true
        }

    val blockStartReminder: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[BLOCK_START_REMINDER] ?: false
        }

    val blockEndReminder: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[BLOCK_END_REMINDER] ?: false
        }

    val enhancedNotificationEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[ENHANCED_NOTIFICATION_ENABLED] ?: false
        }

    val atomicNotificationEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[ATOMIC_NOTIFICATION_ENABLED] ?: false
        }

    val intervalReminderEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[INTERVAL_REMINDER_ENABLED] ?: false
        }

    val intervalReminderMinutes: Flow<List<Int>> = dataStore.data
        .map { preferences ->
            val minutesString = preferences[INTERVAL_REMINDER_MINUTES]
            if (minutesString.isNullOrEmpty()) {
                DEFAULT_REMINDER_MINUTES
            } else {
                minutesString.split(",").mapNotNull { it.toIntOrNull() }
            }
        }

    // 免打扰设置 Flows
    val doNotDisturbEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[DO_NOT_DISTURB_ENABLED] ?: false
        }

    val doNotDisturbStartHour: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[DO_NOT_DISTURB_START_HOUR] ?: 22
        }

    val doNotDisturbStartMinute: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[DO_NOT_DISTURB_START_MINUTE] ?: 0
        }

    val doNotDisturbEndHour: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[DO_NOT_DISTURB_END_HOUR] ?: 8
        }

    val doNotDisturbEndMinute: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[DO_NOT_DISTURB_END_MINUTE] ?: 0
        }

    // StateFlows for UI
    private val _pomodoroNotificationEnabled = MutableStateFlow(true)
    val pomodoroNotificationEnabledState: StateFlow<Boolean> = _pomodoroNotificationEnabled

    private val _pomodoroCompletionAlert = MutableStateFlow(true)
    val pomodoroCompletionAlertState: StateFlow<Boolean> = _pomodoroCompletionAlert

    private val _blockStartReminder = MutableStateFlow(false)
    val blockStartReminderState: StateFlow<Boolean> = _blockStartReminder

    private val _blockEndReminder = MutableStateFlow(false)
    val blockEndReminderState: StateFlow<Boolean> = _blockEndReminder

    init {
        viewModelScope.launch {
            pomodoroNotificationEnabled.collect { _pomodoroNotificationEnabled.value = it }
        }
        viewModelScope.launch {
            pomodoroCompletionAlert.collect { _pomodoroCompletionAlert.value = it }
        }
        viewModelScope.launch {
            blockStartReminder.collect { _blockStartReminder.value = it }
        }
        viewModelScope.launch {
            blockEndReminder.collect { _blockEndReminder.value = it }
        }
    }

    fun setPomodoroNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[POMODORO_NOTIFICATION_ENABLED] = enabled
            }
            _pomodoroNotificationEnabled.value = enabled
        }
    }

    fun setPomodoroCompletionAlert(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[POMODORO_COMPLETION_ALERT] = enabled
            }
            _pomodoroCompletionAlert.value = enabled
        }
    }

    fun setBlockStartReminder(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[BLOCK_START_REMINDER] = enabled
            }
            _blockStartReminder.value = enabled
        }
    }

    fun setBlockEndReminder(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[BLOCK_END_REMINDER] = enabled
            }
            _blockEndReminder.value = enabled
        }
    }

    fun setEnhancedNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[ENHANCED_NOTIFICATION_ENABLED] = enabled
            }
        }
    }

    fun setAtomicNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[ATOMIC_NOTIFICATION_ENABLED] = enabled
            }
        }
    }

    fun setIntervalReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[INTERVAL_REMINDER_ENABLED] = enabled
            }
        }
    }

    fun setIntervalReminderMinutes(minutes: List<Int>) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[INTERVAL_REMINDER_MINUTES] = minutes.joinToString(",")
            }
        }
    }
    
    // 切换某个时间点的选中状态
    fun toggleReminderMinute(minute: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val currentString = preferences[INTERVAL_REMINDER_MINUTES]
                val currentMinutes = if (currentString.isNullOrEmpty()) {
                    DEFAULT_REMINDER_MINUTES.toMutableList()
                } else {
                    currentString.split(",").mapNotNull { it.toIntOrNull() }.toMutableList()
                }
                
                if (currentMinutes.contains(minute)) {
                    currentMinutes.remove(minute)
                } else {
                    currentMinutes.add(minute)
                    currentMinutes.sort()
                }
                
                preferences[INTERVAL_REMINDER_MINUTES] = currentMinutes.joinToString(",")
            }
        }
    }

    // 免打扰设置方法
    fun setDoNotDisturbEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DO_NOT_DISTURB_ENABLED] = enabled
            }
        }
    }

    fun setDoNotDisturbStartTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DO_NOT_DISTURB_START_HOUR] = hour
                preferences[DO_NOT_DISTURB_START_MINUTE] = minute
            }
        }
    }

    fun setDoNotDisturbEndTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DO_NOT_DISTURB_END_HOUR] = hour
                preferences[DO_NOT_DISTURB_END_MINUTE] = minute
            }
        }
    }

    // 检查当前是否在免打扰时间段内
    fun isInDoNotDisturbPeriod(): Boolean {
        val now = java.time.LocalTime.now()
        val startHour = doNotDisturbStartHour.toString().toIntOrNull() ?: 22
        val startMinute = doNotDisturbStartMinute.toString().toIntOrNull() ?: 0
        val endHour = doNotDisturbEndHour.toString().toIntOrNull() ?: 8
        val endMinute = doNotDisturbEndMinute.toString().toIntOrNull() ?: 0

        val startTime = java.time.LocalTime.of(startHour, startMinute)
        val endTime = java.time.LocalTime.of(endHour, endMinute)

        return if (startTime.isBefore(endTime)) {
            now.isAfter(startTime) && now.isBefore(endTime)
        } else {
            // 跨天的情况（如 22:00 - 08:00）
            now.isAfter(startTime) || now.isBefore(endTime)
        }
    }
}
