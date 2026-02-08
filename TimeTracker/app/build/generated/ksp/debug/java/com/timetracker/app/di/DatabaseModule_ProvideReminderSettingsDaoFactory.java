package com.timetracker.app.di;

import com.timetracker.app.data.local.dao.ReminderSettingsDao;
import com.timetracker.app.data.local.database.TimeTrackerDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class DatabaseModule_ProvideReminderSettingsDaoFactory implements Factory<ReminderSettingsDao> {
  private final Provider<TimeTrackerDatabase> databaseProvider;

  public DatabaseModule_ProvideReminderSettingsDaoFactory(
      Provider<TimeTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ReminderSettingsDao get() {
    return provideReminderSettingsDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideReminderSettingsDaoFactory create(
      Provider<TimeTrackerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideReminderSettingsDaoFactory(databaseProvider);
  }

  public static ReminderSettingsDao provideReminderSettingsDao(TimeTrackerDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideReminderSettingsDao(database));
  }
}
