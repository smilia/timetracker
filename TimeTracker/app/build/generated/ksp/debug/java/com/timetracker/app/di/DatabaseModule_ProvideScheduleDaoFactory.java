package com.timetracker.app.di;

import com.timetracker.app.data.local.dao.ScheduleDao;
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
public final class DatabaseModule_ProvideScheduleDaoFactory implements Factory<ScheduleDao> {
  private final Provider<TimeTrackerDatabase> databaseProvider;

  public DatabaseModule_ProvideScheduleDaoFactory(Provider<TimeTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ScheduleDao get() {
    return provideScheduleDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideScheduleDaoFactory create(
      Provider<TimeTrackerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideScheduleDaoFactory(databaseProvider);
  }

  public static ScheduleDao provideScheduleDao(TimeTrackerDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideScheduleDao(database));
  }
}
