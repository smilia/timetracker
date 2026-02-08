package com.timetracker.app.di;

import com.timetracker.app.data.local.dao.TimeBlockDao;
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
public final class DatabaseModule_ProvideTimeBlockDaoFactory implements Factory<TimeBlockDao> {
  private final Provider<TimeTrackerDatabase> databaseProvider;

  public DatabaseModule_ProvideTimeBlockDaoFactory(Provider<TimeTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public TimeBlockDao get() {
    return provideTimeBlockDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideTimeBlockDaoFactory create(
      Provider<TimeTrackerDatabase> databaseProvider) {
    return new DatabaseModule_ProvideTimeBlockDaoFactory(databaseProvider);
  }

  public static TimeBlockDao provideTimeBlockDao(TimeTrackerDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTimeBlockDao(database));
  }
}
