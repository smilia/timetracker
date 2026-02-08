package com.timetracker.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// 统一的 DataStore 定义
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
