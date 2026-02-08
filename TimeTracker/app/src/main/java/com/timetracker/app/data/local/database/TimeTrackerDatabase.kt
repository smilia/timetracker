package com.timetracker.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.timetracker.app.data.local.dao.*
import com.timetracker.app.data.local.entity.*

@Database(
    entities = [
        CategoryEntity::class,
        TimeBlockEntity::class,
        TemplateEntity::class,
        ScheduleEntity::class,
        ReminderSettingsEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class TimeTrackerDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun timeBlockDao(): TimeBlockDao
    abstract fun templateDao(): TemplateDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: TimeTrackerDatabase? = null

        // Migration from version 2 to 3: Add isReminderEnabled and isPomodoro columns
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add isReminderEnabled column with default value false
                database.execSQL("ALTER TABLE time_blocks ADD COLUMN isReminderEnabled INTEGER NOT NULL DEFAULT 0")
                // Add isPomodoro column with default value false
                database.execSQL("ALTER TABLE time_blocks ADD COLUMN isPomodoro INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 3 to 4: Add lockScreenNotificationEnabled column
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add lockScreenNotificationEnabled column with default value false
                database.execSQL("ALTER TABLE reminder_settings ADD COLUMN lockScreenNotificationEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 4 to 5: Add usageCount column to templates
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add usageCount column with default value 0
                database.execSQL("ALTER TABLE templates ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 5 to 6: Replace categoryId with color and isProductive
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new time_blocks table with updated schema
                database.execSQL("""
                    CREATE TABLE time_blocks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        color TEXT NOT NULL DEFAULT '#5B9BD5',
                        title TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        isReminderEnabled INTEGER NOT NULL DEFAULT 0,
                        isPomodoro INTEGER NOT NULL DEFAULT 0,
                        isProductive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // Migrate data from old table to new table
                database.execSQL("""
                    INSERT INTO time_blocks_new (
                        id, color, title, startTime, endTime, date, note,
                        isCompleted, isReminderEnabled, isPomodoro, isProductive,
                        createdAt, updatedAt
                    )
                    SELECT 
                        id, 
                        CASE 
                            WHEN categoryId = 1 THEN '#5B9BD5'
                            WHEN categoryId = 2 THEN '#70AD47'
                            WHEN categoryId = 3 THEN '#ED7D31'
                            WHEN categoryId = 4 THEN '#E85D75'
                            WHEN categoryId = 5 THEN '#9F6DD3'
                            ELSE '#5B9BD5'
                        END as color,
                        title, startTime, endTime, date, note,
                        isCompleted, isReminderEnabled, isPomodoro, 1 as isProductive,
                        createdAt, updatedAt
                    FROM time_blocks
                """)

                // Drop old table
                database.execSQL("DROP TABLE time_blocks")

                // Rename new table to old name
                database.execSQL("ALTER TABLE time_blocks_new RENAME TO time_blocks")

                // Create indices
                database.execSQL("CREATE INDEX index_time_blocks_startTime ON time_blocks(startTime)")
                database.execSQL("CREATE INDEX index_time_blocks_date ON time_blocks(date)")
            }
        }

        // Migration from version 6 to 7: Replace isProductive with timeNature
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migrate time_blocks table
                database.execSQL("""
                    CREATE TABLE time_blocks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        color TEXT NOT NULL,
                        title TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        note TEXT,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        isReminderEnabled INTEGER NOT NULL DEFAULT 0,
                        isPomodoro INTEGER NOT NULL DEFAULT 0,
                        timeNature TEXT NOT NULL DEFAULT 'PRODUCTIVE',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                """)

                database.execSQL("""
                    INSERT INTO time_blocks_new (
                        id, color, title, startTime, endTime, date, note,
                        isCompleted, isReminderEnabled, isPomodoro, timeNature,
                        createdAt, updatedAt
                    )
                    SELECT 
                        id, color, title, startTime, endTime, date, note,
                        isCompleted, isReminderEnabled, isPomodoro,
                        CASE 
                            WHEN isProductive = 1 THEN 'PRODUCTIVE'
                            ELSE 'UNPRODUCTIVE'
                        END as timeNature,
                        createdAt, updatedAt
                    FROM time_blocks
                """)

                database.execSQL("DROP TABLE time_blocks")
                database.execSQL("ALTER TABLE time_blocks_new RENAME TO time_blocks")
                database.execSQL("CREATE INDEX index_time_blocks_startTime ON time_blocks(startTime)")
                database.execSQL("CREATE INDEX index_time_blocks_date ON time_blocks(date)")

                // Migrate templates table
                database.execSQL("""
                    CREATE TABLE templates_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        color TEXT NOT NULL,
                        name TEXT NOT NULL,
                        defaultDuration INTEGER NOT NULL,
                        isFrequent INTEGER NOT NULL DEFAULT 0,
                        timeNature TEXT NOT NULL DEFAULT 'PRODUCTIVE',
                        usageCount INTEGER NOT NULL DEFAULT 0
                    )
                """)

                database.execSQL("""
                    INSERT INTO templates_new (
                        id, color, name, defaultDuration, isFrequent, timeNature, usageCount
                    )
                    SELECT 
                        id, color, name, defaultDuration, isFrequent,
                        CASE 
                            WHEN isProductive = 1 THEN 'PRODUCTIVE'
                            ELSE 'UNPRODUCTIVE'
                        END as timeNature,
                        usageCount
                    FROM templates
                """)

                database.execSQL("DROP TABLE templates")
                database.execSQL("ALTER TABLE templates_new RENAME TO templates")
            }
        }

        fun getInstance(context: Context): TimeTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TimeTrackerDatabase::class.java,
                    "timetracker_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
