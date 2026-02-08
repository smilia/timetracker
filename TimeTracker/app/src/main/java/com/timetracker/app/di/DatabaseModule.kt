package com.timetracker.app.di

import android.content.Context
import androidx.room.Room
import com.timetracker.app.data.local.database.TimeTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TimeTrackerDatabase {
        return Room.databaseBuilder(
            context,
            TimeTrackerDatabase::class.java,
            "timetracker.db"
        ).build()
    }
    
    @Provides
    fun provideCategoryDao(database: TimeTrackerDatabase) = database.categoryDao()
    
    @Provides
    fun provideTimeBlockDao(database: TimeTrackerDatabase) = database.timeBlockDao()
    
    @Provides
    fun provideTemplateDao(database: TimeTrackerDatabase) = database.templateDao()
    
    @Provides
    fun provideScheduleDao(database: TimeTrackerDatabase) = database.scheduleDao()
    
    @Provides
    fun provideReminderSettingsDao(database: TimeTrackerDatabase) = database.reminderSettingsDao()
}
