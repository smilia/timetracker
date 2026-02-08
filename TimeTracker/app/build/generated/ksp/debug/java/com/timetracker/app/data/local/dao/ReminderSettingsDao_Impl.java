package com.timetracker.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.timetracker.app.data.local.entity.ReminderSettingsEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReminderSettingsDao_Impl implements ReminderSettingsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReminderSettingsEntity> __insertionAdapterOfReminderSettingsEntity;

  private final EntityDeletionOrUpdateAdapter<ReminderSettingsEntity> __updateAdapterOfReminderSettingsEntity;

  public ReminderSettingsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReminderSettingsEntity = new EntityInsertionAdapter<ReminderSettingsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `reminder_settings` (`id`,`isEnabled`,`intervalMinutes`,`quietHoursStart`,`quietHoursEnd`,`vibrationEnabled`,`lockScreenNotificationEnabled`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReminderSettingsEntity entity) {
        statement.bindLong(1, entity.getId());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(2, _tmp);
        statement.bindLong(3, entity.getIntervalMinutes());
        if (entity.getQuietHoursStart() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getQuietHoursStart());
        }
        if (entity.getQuietHoursEnd() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getQuietHoursEnd());
        }
        final int _tmp_1 = entity.getVibrationEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        final int _tmp_2 = entity.getLockScreenNotificationEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp_2);
      }
    };
    this.__updateAdapterOfReminderSettingsEntity = new EntityDeletionOrUpdateAdapter<ReminderSettingsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `reminder_settings` SET `id` = ?,`isEnabled` = ?,`intervalMinutes` = ?,`quietHoursStart` = ?,`quietHoursEnd` = ?,`vibrationEnabled` = ?,`lockScreenNotificationEnabled` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReminderSettingsEntity entity) {
        statement.bindLong(1, entity.getId());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(2, _tmp);
        statement.bindLong(3, entity.getIntervalMinutes());
        if (entity.getQuietHoursStart() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getQuietHoursStart());
        }
        if (entity.getQuietHoursEnd() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getQuietHoursEnd());
        }
        final int _tmp_1 = entity.getVibrationEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        final int _tmp_2 = entity.getLockScreenNotificationEnabled() ? 1 : 0;
        statement.bindLong(7, _tmp_2);
        statement.bindLong(8, entity.getId());
      }
    };
  }

  @Override
  public Object insertSettings(final ReminderSettingsEntity settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReminderSettingsEntity.insert(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSettings(final ReminderSettingsEntity settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfReminderSettingsEntity.handle(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<ReminderSettingsEntity> getSettings() {
    final String _sql = "SELECT * FROM reminder_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"reminder_settings"}, new Callable<ReminderSettingsEntity>() {
      @Override
      @Nullable
      public ReminderSettingsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMinutes");
          final int _cursorIndexOfQuietHoursStart = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursStart");
          final int _cursorIndexOfQuietHoursEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursEnd");
          final int _cursorIndexOfVibrationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "vibrationEnabled");
          final int _cursorIndexOfLockScreenNotificationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "lockScreenNotificationEnabled");
          final ReminderSettingsEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final int _tmpIntervalMinutes;
            _tmpIntervalMinutes = _cursor.getInt(_cursorIndexOfIntervalMinutes);
            final String _tmpQuietHoursStart;
            if (_cursor.isNull(_cursorIndexOfQuietHoursStart)) {
              _tmpQuietHoursStart = null;
            } else {
              _tmpQuietHoursStart = _cursor.getString(_cursorIndexOfQuietHoursStart);
            }
            final String _tmpQuietHoursEnd;
            if (_cursor.isNull(_cursorIndexOfQuietHoursEnd)) {
              _tmpQuietHoursEnd = null;
            } else {
              _tmpQuietHoursEnd = _cursor.getString(_cursorIndexOfQuietHoursEnd);
            }
            final boolean _tmpVibrationEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfVibrationEnabled);
            _tmpVibrationEnabled = _tmp_1 != 0;
            final boolean _tmpLockScreenNotificationEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfLockScreenNotificationEnabled);
            _tmpLockScreenNotificationEnabled = _tmp_2 != 0;
            _result = new ReminderSettingsEntity(_tmpId,_tmpIsEnabled,_tmpIntervalMinutes,_tmpQuietHoursStart,_tmpQuietHoursEnd,_tmpVibrationEnabled,_tmpLockScreenNotificationEnabled);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSettingsSync(final Continuation<? super ReminderSettingsEntity> $completion) {
    final String _sql = "SELECT * FROM reminder_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReminderSettingsEntity>() {
      @Override
      @Nullable
      public ReminderSettingsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMinutes");
          final int _cursorIndexOfQuietHoursStart = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursStart");
          final int _cursorIndexOfQuietHoursEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "quietHoursEnd");
          final int _cursorIndexOfVibrationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "vibrationEnabled");
          final int _cursorIndexOfLockScreenNotificationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "lockScreenNotificationEnabled");
          final ReminderSettingsEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final int _tmpIntervalMinutes;
            _tmpIntervalMinutes = _cursor.getInt(_cursorIndexOfIntervalMinutes);
            final String _tmpQuietHoursStart;
            if (_cursor.isNull(_cursorIndexOfQuietHoursStart)) {
              _tmpQuietHoursStart = null;
            } else {
              _tmpQuietHoursStart = _cursor.getString(_cursorIndexOfQuietHoursStart);
            }
            final String _tmpQuietHoursEnd;
            if (_cursor.isNull(_cursorIndexOfQuietHoursEnd)) {
              _tmpQuietHoursEnd = null;
            } else {
              _tmpQuietHoursEnd = _cursor.getString(_cursorIndexOfQuietHoursEnd);
            }
            final boolean _tmpVibrationEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfVibrationEnabled);
            _tmpVibrationEnabled = _tmp_1 != 0;
            final boolean _tmpLockScreenNotificationEnabled;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfLockScreenNotificationEnabled);
            _tmpLockScreenNotificationEnabled = _tmp_2 != 0;
            _result = new ReminderSettingsEntity(_tmpId,_tmpIsEnabled,_tmpIntervalMinutes,_tmpQuietHoursStart,_tmpQuietHoursEnd,_tmpVibrationEnabled,_tmpLockScreenNotificationEnabled);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
