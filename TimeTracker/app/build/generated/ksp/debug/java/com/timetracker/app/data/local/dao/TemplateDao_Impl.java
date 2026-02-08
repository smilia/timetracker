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
import com.timetracker.app.data.local.entity.TemplateEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class TemplateDao_Impl implements TemplateDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TemplateEntity> __insertionAdapterOfTemplateEntity;

  private final EntityDeletionOrUpdateAdapter<TemplateEntity> __deletionAdapterOfTemplateEntity;

  private final EntityDeletionOrUpdateAdapter<TemplateEntity> __updateAdapterOfTemplateEntity;

  private final SharedSQLiteStatement __preparedStmtOfIncrementUsageCount;

  public TemplateDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTemplateEntity = new EntityInsertionAdapter<TemplateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `templates` (`id`,`color`,`name`,`defaultDuration`,`isFrequent`,`timeNature`,`usageCount`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TemplateEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getColor());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getDefaultDuration());
        final int _tmp = entity.isFrequent() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindString(6, entity.getTimeNature());
        statement.bindLong(7, entity.getUsageCount());
      }
    };
    this.__deletionAdapterOfTemplateEntity = new EntityDeletionOrUpdateAdapter<TemplateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `templates` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TemplateEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTemplateEntity = new EntityDeletionOrUpdateAdapter<TemplateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `templates` SET `id` = ?,`color` = ?,`name` = ?,`defaultDuration` = ?,`isFrequent` = ?,`timeNature` = ?,`usageCount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TemplateEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getColor());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getDefaultDuration());
        final int _tmp = entity.isFrequent() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindString(6, entity.getTimeNature());
        statement.bindLong(7, entity.getUsageCount());
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfIncrementUsageCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE templates SET usageCount = usageCount + 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTemplate(final TemplateEntity template,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTemplateEntity.insertAndReturnId(template);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTemplate(final TemplateEntity template,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTemplateEntity.handle(template);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTemplate(final TemplateEntity template,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTemplateEntity.handle(template);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementUsageCount(final long templateId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementUsageCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, templateId);
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
          __preparedStmtOfIncrementUsageCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TemplateEntity>> getAllTemplates() {
    final String _sql = "SELECT * FROM templates ORDER BY isFrequent DESC, usageCount DESC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"templates"}, new Callable<List<TemplateEntity>>() {
      @Override
      @NonNull
      public List<TemplateEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDefaultDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultDuration");
          final int _cursorIndexOfIsFrequent = CursorUtil.getColumnIndexOrThrow(_cursor, "isFrequent");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfUsageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "usageCount");
          final List<TemplateEntity> _result = new ArrayList<TemplateEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TemplateEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpDefaultDuration;
            _tmpDefaultDuration = _cursor.getInt(_cursorIndexOfDefaultDuration);
            final boolean _tmpIsFrequent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFrequent);
            _tmpIsFrequent = _tmp != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final int _tmpUsageCount;
            _tmpUsageCount = _cursor.getInt(_cursorIndexOfUsageCount);
            _item = new TemplateEntity(_tmpId,_tmpColor,_tmpName,_tmpDefaultDuration,_tmpIsFrequent,_tmpTimeNature,_tmpUsageCount);
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
  public Flow<List<TemplateEntity>> getAllTemplatesByUsage() {
    final String _sql = "SELECT * FROM templates ORDER BY usageCount DESC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"templates"}, new Callable<List<TemplateEntity>>() {
      @Override
      @NonNull
      public List<TemplateEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDefaultDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultDuration");
          final int _cursorIndexOfIsFrequent = CursorUtil.getColumnIndexOrThrow(_cursor, "isFrequent");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfUsageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "usageCount");
          final List<TemplateEntity> _result = new ArrayList<TemplateEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TemplateEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpDefaultDuration;
            _tmpDefaultDuration = _cursor.getInt(_cursorIndexOfDefaultDuration);
            final boolean _tmpIsFrequent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFrequent);
            _tmpIsFrequent = _tmp != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final int _tmpUsageCount;
            _tmpUsageCount = _cursor.getInt(_cursorIndexOfUsageCount);
            _item = new TemplateEntity(_tmpId,_tmpColor,_tmpName,_tmpDefaultDuration,_tmpIsFrequent,_tmpTimeNature,_tmpUsageCount);
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
  public Flow<List<TemplateEntity>> getFrequentTemplates() {
    final String _sql = "SELECT * FROM templates WHERE isFrequent = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"templates"}, new Callable<List<TemplateEntity>>() {
      @Override
      @NonNull
      public List<TemplateEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDefaultDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultDuration");
          final int _cursorIndexOfIsFrequent = CursorUtil.getColumnIndexOrThrow(_cursor, "isFrequent");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfUsageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "usageCount");
          final List<TemplateEntity> _result = new ArrayList<TemplateEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TemplateEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpDefaultDuration;
            _tmpDefaultDuration = _cursor.getInt(_cursorIndexOfDefaultDuration);
            final boolean _tmpIsFrequent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFrequent);
            _tmpIsFrequent = _tmp != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final int _tmpUsageCount;
            _tmpUsageCount = _cursor.getInt(_cursorIndexOfUsageCount);
            _item = new TemplateEntity(_tmpId,_tmpColor,_tmpName,_tmpDefaultDuration,_tmpIsFrequent,_tmpTimeNature,_tmpUsageCount);
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
  public Object getTemplateById(final long id,
      final Continuation<? super TemplateEntity> $completion) {
    final String _sql = "SELECT * FROM templates WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TemplateEntity>() {
      @Override
      @Nullable
      public TemplateEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDefaultDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultDuration");
          final int _cursorIndexOfIsFrequent = CursorUtil.getColumnIndexOrThrow(_cursor, "isFrequent");
          final int _cursorIndexOfTimeNature = CursorUtil.getColumnIndexOrThrow(_cursor, "timeNature");
          final int _cursorIndexOfUsageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "usageCount");
          final TemplateEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpDefaultDuration;
            _tmpDefaultDuration = _cursor.getInt(_cursorIndexOfDefaultDuration);
            final boolean _tmpIsFrequent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFrequent);
            _tmpIsFrequent = _tmp != 0;
            final String _tmpTimeNature;
            _tmpTimeNature = _cursor.getString(_cursorIndexOfTimeNature);
            final int _tmpUsageCount;
            _tmpUsageCount = _cursor.getInt(_cursorIndexOfUsageCount);
            _result = new TemplateEntity(_tmpId,_tmpColor,_tmpName,_tmpDefaultDuration,_tmpIsFrequent,_tmpTimeNature,_tmpUsageCount);
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
  public Object getTemplateCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM templates";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
