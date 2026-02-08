package com.timetracker.app.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.timetracker.app.data.local.dao.CategoryDao;
import com.timetracker.app.data.local.dao.CategoryDao_Impl;
import com.timetracker.app.data.local.dao.ReminderSettingsDao;
import com.timetracker.app.data.local.dao.ReminderSettingsDao_Impl;
import com.timetracker.app.data.local.dao.ScheduleDao;
import com.timetracker.app.data.local.dao.ScheduleDao_Impl;
import com.timetracker.app.data.local.dao.TemplateDao;
import com.timetracker.app.data.local.dao.TemplateDao_Impl;
import com.timetracker.app.data.local.dao.TimeBlockDao;
import com.timetracker.app.data.local.dao.TimeBlockDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TimeTrackerDatabase_Impl extends TimeTrackerDatabase {
  private volatile CategoryDao _categoryDao;

  private volatile TimeBlockDao _timeBlockDao;

  private volatile TemplateDao _templateDao;

  private volatile ScheduleDao _scheduleDao;

  private volatile ReminderSettingsDao _reminderSettingsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(7) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` TEXT NOT NULL, `icon` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `time_blocks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `color` TEXT NOT NULL, `title` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `date` INTEGER NOT NULL, `note` TEXT, `isCompleted` INTEGER NOT NULL, `isReminderEnabled` INTEGER NOT NULL, `isPomodoro` INTEGER NOT NULL, `timeNature` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_blocks_startTime` ON `time_blocks` (`startTime`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_blocks_date` ON `time_blocks` (`date`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `color` TEXT NOT NULL, `name` TEXT NOT NULL, `defaultDuration` INTEGER NOT NULL, `isFrequent` INTEGER NOT NULL, `timeNature` TEXT NOT NULL, `usageCount` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `categoryId` INTEGER, `reminderMinutes` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `recurrenceRule` TEXT, `isCompleted` INTEGER NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedules_categoryId` ON `schedules` (`categoryId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedules_startTime` ON `schedules` (`startTime`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reminder_settings` (`id` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL, `intervalMinutes` INTEGER NOT NULL, `quietHoursStart` TEXT, `quietHoursEnd` TEXT, `vibrationEnabled` INTEGER NOT NULL, `lockScreenNotificationEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4ece533c255b55daf216b5f593f392fe')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `categories`");
        db.execSQL("DROP TABLE IF EXISTS `time_blocks`");
        db.execSQL("DROP TABLE IF EXISTS `templates`");
        db.execSQL("DROP TABLE IF EXISTS `schedules`");
        db.execSQL("DROP TABLE IF EXISTS `reminder_settings`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsCategories = new HashMap<String, TableInfo.Column>(5);
        _columnsCategories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("color", new TableInfo.Column("color", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("icon", new TableInfo.Column("icon", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategories = new TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories);
        final TableInfo _existingCategories = TableInfo.read(db, "categories");
        if (!_infoCategories.equals(_existingCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "categories(com.timetracker.app.data.local.entity.CategoryEntity).\n"
                  + " Expected:\n" + _infoCategories + "\n"
                  + " Found:\n" + _existingCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsTimeBlocks = new HashMap<String, TableInfo.Column>(13);
        _columnsTimeBlocks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("color", new TableInfo.Column("color", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("isReminderEnabled", new TableInfo.Column("isReminderEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("isPomodoro", new TableInfo.Column("isPomodoro", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("timeNature", new TableInfo.Column("timeNature", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTimeBlocks.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTimeBlocks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTimeBlocks = new HashSet<TableInfo.Index>(2);
        _indicesTimeBlocks.add(new TableInfo.Index("index_time_blocks_startTime", false, Arrays.asList("startTime"), Arrays.asList("ASC")));
        _indicesTimeBlocks.add(new TableInfo.Index("index_time_blocks_date", false, Arrays.asList("date"), Arrays.asList("ASC")));
        final TableInfo _infoTimeBlocks = new TableInfo("time_blocks", _columnsTimeBlocks, _foreignKeysTimeBlocks, _indicesTimeBlocks);
        final TableInfo _existingTimeBlocks = TableInfo.read(db, "time_blocks");
        if (!_infoTimeBlocks.equals(_existingTimeBlocks)) {
          return new RoomOpenHelper.ValidationResult(false, "time_blocks(com.timetracker.app.data.local.entity.TimeBlockEntity).\n"
                  + " Expected:\n" + _infoTimeBlocks + "\n"
                  + " Found:\n" + _existingTimeBlocks);
        }
        final HashMap<String, TableInfo.Column> _columnsTemplates = new HashMap<String, TableInfo.Column>(7);
        _columnsTemplates.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTemplates.put("color", new TableInfo.Column("color", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTemplates.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTemplates.put("defaultDuration", new TableInfo.Column("defaultDuration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTemplates.put("isFrequent", new TableInfo.Column("isFrequent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTemplates.put("timeNature", new TableInfo.Column("timeNature", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTemplates.put("usageCount", new TableInfo.Column("usageCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTemplates = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTemplates = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTemplates = new TableInfo("templates", _columnsTemplates, _foreignKeysTemplates, _indicesTemplates);
        final TableInfo _existingTemplates = TableInfo.read(db, "templates");
        if (!_infoTemplates.equals(_existingTemplates)) {
          return new RoomOpenHelper.ValidationResult(false, "templates(com.timetracker.app.data.local.entity.TemplateEntity).\n"
                  + " Expected:\n" + _infoTemplates + "\n"
                  + " Found:\n" + _existingTemplates);
        }
        final HashMap<String, TableInfo.Column> _columnsSchedules = new HashMap<String, TableInfo.Column>(10);
        _columnsSchedules.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("categoryId", new TableInfo.Column("categoryId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("reminderMinutes", new TableInfo.Column("reminderMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("isRecurring", new TableInfo.Column("isRecurring", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("recurrenceRule", new TableInfo.Column("recurrenceRule", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("isCompleted", new TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSchedules = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysSchedules.add(new TableInfo.ForeignKey("categories", "SET NULL", "NO ACTION", Arrays.asList("categoryId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesSchedules = new HashSet<TableInfo.Index>(2);
        _indicesSchedules.add(new TableInfo.Index("index_schedules_categoryId", false, Arrays.asList("categoryId"), Arrays.asList("ASC")));
        _indicesSchedules.add(new TableInfo.Index("index_schedules_startTime", false, Arrays.asList("startTime"), Arrays.asList("ASC")));
        final TableInfo _infoSchedules = new TableInfo("schedules", _columnsSchedules, _foreignKeysSchedules, _indicesSchedules);
        final TableInfo _existingSchedules = TableInfo.read(db, "schedules");
        if (!_infoSchedules.equals(_existingSchedules)) {
          return new RoomOpenHelper.ValidationResult(false, "schedules(com.timetracker.app.data.local.entity.ScheduleEntity).\n"
                  + " Expected:\n" + _infoSchedules + "\n"
                  + " Found:\n" + _existingSchedules);
        }
        final HashMap<String, TableInfo.Column> _columnsReminderSettings = new HashMap<String, TableInfo.Column>(7);
        _columnsReminderSettings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderSettings.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderSettings.put("intervalMinutes", new TableInfo.Column("intervalMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderSettings.put("quietHoursStart", new TableInfo.Column("quietHoursStart", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderSettings.put("quietHoursEnd", new TableInfo.Column("quietHoursEnd", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderSettings.put("vibrationEnabled", new TableInfo.Column("vibrationEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminderSettings.put("lockScreenNotificationEnabled", new TableInfo.Column("lockScreenNotificationEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReminderSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReminderSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReminderSettings = new TableInfo("reminder_settings", _columnsReminderSettings, _foreignKeysReminderSettings, _indicesReminderSettings);
        final TableInfo _existingReminderSettings = TableInfo.read(db, "reminder_settings");
        if (!_infoReminderSettings.equals(_existingReminderSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "reminder_settings(com.timetracker.app.data.local.entity.ReminderSettingsEntity).\n"
                  + " Expected:\n" + _infoReminderSettings + "\n"
                  + " Found:\n" + _existingReminderSettings);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4ece533c255b55daf216b5f593f392fe", "4c02f7e3d634289a9e8c518e941766de");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "categories","time_blocks","templates","schedules","reminder_settings");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `categories`");
      _db.execSQL("DELETE FROM `time_blocks`");
      _db.execSQL("DELETE FROM `templates`");
      _db.execSQL("DELETE FROM `schedules`");
      _db.execSQL("DELETE FROM `reminder_settings`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TimeBlockDao.class, TimeBlockDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TemplateDao.class, TemplateDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ScheduleDao.class, ScheduleDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReminderSettingsDao.class, ReminderSettingsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public TimeBlockDao timeBlockDao() {
    if (_timeBlockDao != null) {
      return _timeBlockDao;
    } else {
      synchronized(this) {
        if(_timeBlockDao == null) {
          _timeBlockDao = new TimeBlockDao_Impl(this);
        }
        return _timeBlockDao;
      }
    }
  }

  @Override
  public TemplateDao templateDao() {
    if (_templateDao != null) {
      return _templateDao;
    } else {
      synchronized(this) {
        if(_templateDao == null) {
          _templateDao = new TemplateDao_Impl(this);
        }
        return _templateDao;
      }
    }
  }

  @Override
  public ScheduleDao scheduleDao() {
    if (_scheduleDao != null) {
      return _scheduleDao;
    } else {
      synchronized(this) {
        if(_scheduleDao == null) {
          _scheduleDao = new ScheduleDao_Impl(this);
        }
        return _scheduleDao;
      }
    }
  }

  @Override
  public ReminderSettingsDao reminderSettingsDao() {
    if (_reminderSettingsDao != null) {
      return _reminderSettingsDao;
    } else {
      synchronized(this) {
        if(_reminderSettingsDao == null) {
          _reminderSettingsDao = new ReminderSettingsDao_Impl(this);
        }
        return _reminderSettingsDao;
      }
    }
  }
}
