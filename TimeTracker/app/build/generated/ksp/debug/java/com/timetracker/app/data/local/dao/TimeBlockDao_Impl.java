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
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.timetracker.app.data.local.entity.TimeBlockEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TimeBlockDao_Impl implements TimeBlockDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TimeBlockEntity> __insertionAdapterOfTimeBlockEntity;

  private final EntityDeletionOrUpdateAdapter<TimeBlockEntity> __deletionAdapterOfTimeBlockEntity;

  private final EntityDeletionOrUpdateAdapter<TimeBlockEntity> __updateAdapterOfTimeBlockEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTimeBlockById;

  public TimeBlockDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTimeBlockEntity = new EntityInsertionAdapter<TimeBlockEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `time_blocks` (`id`,`color`,`title`,`startTime`,`endTime`,`date`,`note`,`isCompleted`,`isReminderEnabled`,`isPomodoro`,`timeNature`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimeBlockEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getColor());
        statement.bindString(3, entity.getTitle());
        statement.bindLong(4, entity.getStartTime());
        statement.bindLong(5, entity.getEndTime());
        statement.bindLong(6, entity.getDate());
        if (entity.getNote() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getNote());
        }
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.isReminderEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        final int _tmp_2 = entity.isPomodoro() ? 1 : 0;
        statement.bindLong(10, _tmp_2);
        statement.bindString(11, entity.getTimeNature());
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindLong(13, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfTimeBlockEntity = new EntityDeletionOrUpdateAdapter<TimeBlockEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `time_blocks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimeBlockEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTimeBlockEntity = new EntityDeletionOrUpdateAdapter<TimeBlockEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `time_blocks` SET `id` = ?,`color` = ?,`title` = ?,`startTime` = ?,`endTime` = ?,`date` = ?,`note` = ?,`isCompleted` = ?,`isReminderEnabled` = ?,`isPomodoro` = ?,`timeNature` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimeBlockEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getColor());
        statement.bindString(3, entity.getTitle());
        statement.bindLong(4, entity.getStartTime());
        statement.bindLong(5, entity.getEndTime());
        statement.bindLong(6, entity.getDate());
        if (entity.getNote() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getNote());
        }
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp);
        final int _tmp_1 = entity.isReminderEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp_1);
        final int _tmp_2 = entity.isPomodoro() ? 1 : 0;
        statement.bindLong(10, _tmp_2);
        statement.bindString(11, entity.getTimeNature());
        statement.bindLong(12, entity.getCreatedAt());
        statement.bindLong(13, entity.getUpdatedAt());
        statement.bindLong(14, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteTimeBlockById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM time_blocks WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTimeBlock(final TimeBlockEntity timeBlock,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTimeBlockEntity.insertAndReturnId(timeBlock);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTimeBlock(final TimeBlockEntity timeBlock,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTimeBlockEntity.handle(timeBlock);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTimeBlock(final TimeBlockEntity timeBlock,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTimeBlockEntity.handle(timeBlock);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTimeBlockById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTimeBlockById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteTimeBlockById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TimeBlockEntity>> getTimeBlocksByDate(final long date) {
    final String _sql = "SELECT * FROM time_blocks WHERE date = ? ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, date);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"time_blocks"}, new Callable<List<TimeBlockEntity>>() {
      @Override
      @NonNull
      public List<TimeBlockEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isReminderEnabled");
          final int _cursorIndexOfIsPomodoro = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoro");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<TimeBlockEntity> _result = new ArrayList<TimeBlockEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeBlockEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsReminderEnabled);
            _tmpIsReminderEnabled = _tmp_1 != 0;
            final boolean _tmpIsPomodoro;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPomodoro);
            _tmpIsPomodoro = _tmp_2 != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new TimeBlockEntity(_tmpId,_tmpColor,_tmpTitle,_tmpStartTime,_tmpEndTime,_tmpDate,_tmpNote,_tmpIsCompleted,_tmpIsReminderEnabled,_tmpIsPomodoro,_tmpTimeNature,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
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
  public Flow<List<TimeBlockEntity>> getTimeBlocksBetweenDates(final long startDate,
      final long endDate) {
    final String _sql = "SELECT * FROM time_blocks WHERE date BETWEEN ? AND ? ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"time_blocks"}, new Callable<List<TimeBlockEntity>>() {
      @Override
      @NonNull
      public List<TimeBlockEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isReminderEnabled");
          final int _cursorIndexOfIsPomodoro = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoro");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<TimeBlockEntity> _result = new ArrayList<TimeBlockEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeBlockEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsReminderEnabled);
            _tmpIsReminderEnabled = _tmp_1 != 0;
            final boolean _tmpIsPomodoro;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPomodoro);
            _tmpIsPomodoro = _tmp_2 != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new TimeBlockEntity(_tmpId,_tmpColor,_tmpTitle,_tmpStartTime,_tmpEndTime,_tmpDate,_tmpNote,_tmpIsCompleted,_tmpIsReminderEnabled,_tmpIsPomodoro,_tmpTimeNature,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
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
  public Object getTimeBlockById(final long id,
      final Continuation<? super TimeBlockEntity> $completion) {
    final String _sql = "SELECT * FROM time_blocks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TimeBlockEntity>() {
      @Override
      @Nullable
      public TimeBlockEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isReminderEnabled");
          final int _cursorIndexOfIsPomodoro = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoro");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final TimeBlockEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsReminderEnabled);
            _tmpIsReminderEnabled = _tmp_1 != 0;
            final boolean _tmpIsPomodoro;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPomodoro);
            _tmpIsPomodoro = _tmp_2 != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new TimeBlockEntity(_tmpId,_tmpColor,_tmpTitle,_tmpStartTime,_tmpEndTime,_tmpDate,_tmpNote,_tmpIsCompleted,_tmpIsReminderEnabled,_tmpIsPomodoro,_tmpTimeNature,_tmpCreatedAt,_tmpUpdatedAt);
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

  @Override
  public Object getCurrentTimeBlock(final long time,
      final Continuation<? super TimeBlockEntity> $completion) {
    final String _sql = "SELECT * FROM time_blocks WHERE startTime <= ? AND endTime > ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, time);
    _argIndex = 2;
    _statement.bindLong(_argIndex, time);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TimeBlockEntity>() {
      @Override
      @Nullable
      public TimeBlockEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isReminderEnabled");
          final int _cursorIndexOfIsPomodoro = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoro");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final TimeBlockEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsReminderEnabled);
            _tmpIsReminderEnabled = _tmp_1 != 0;
            final boolean _tmpIsPomodoro;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPomodoro);
            _tmpIsPomodoro = _tmp_2 != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new TimeBlockEntity(_tmpId,_tmpColor,_tmpTitle,_tmpStartTime,_tmpEndTime,_tmpDate,_tmpNote,_tmpIsCompleted,_tmpIsReminderEnabled,_tmpIsPomodoro,_tmpTimeNature,_tmpCreatedAt,_tmpUpdatedAt);
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

  @Override
  public Object getOverlappingBlocks(final long startTime, final long endTime, final long date,
      final Continuation<? super List<TimeBlockEntity>> $completion) {
    final String _sql = "SELECT * FROM time_blocks WHERE startTime < ? AND endTime > ? AND date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, endTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 3;
    _statement.bindLong(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TimeBlockEntity>>() {
      @Override
      @NonNull
      public List<TimeBlockEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isReminderEnabled");
          final int _cursorIndexOfIsPomodoro = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoro");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<TimeBlockEntity> _result = new ArrayList<TimeBlockEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeBlockEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsReminderEnabled);
            _tmpIsReminderEnabled = _tmp_1 != 0;
            final boolean _tmpIsPomodoro;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPomodoro);
            _tmpIsPomodoro = _tmp_2 != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new TimeBlockEntity(_tmpId,_tmpColor,_tmpTitle,_tmpStartTime,_tmpEndTime,_tmpDate,_tmpNote,_tmpIsCompleted,_tmpIsReminderEnabled,_tmpIsPomodoro,_tmpTimeNature,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTimeBlocksForWidget(final long startDate, final long endDate,
      final Continuation<? super List<TimeBlockEntity>> $completion) {
    final String _sql = "SELECT * FROM time_blocks WHERE date >= ? AND date < ? ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TimeBlockEntity>>() {
      @Override
      @NonNull
      public List<TimeBlockEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsReminderEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isReminderEnabled");
          final int _cursorIndexOfIsPomodoro = CursorUtil.getColumnIndexOrThrow(_cursor, "isPomodoro");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<TimeBlockEntity> _result = new ArrayList<TimeBlockEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeBlockEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsReminderEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsReminderEnabled);
            _tmpIsReminderEnabled = _tmp_1 != 0;
            final boolean _tmpIsPomodoro;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsPomodoro);
            _tmpIsPomodoro = _tmp_2 != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new TimeBlockEntity(_tmpId,_tmpColor,_tmpTitle,_tmpStartTime,_tmpEndTime,_tmpDate,_tmpNote,_tmpIsCompleted,_tmpIsReminderEnabled,_tmpIsPomodoro,_tmpTimeNature,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
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
