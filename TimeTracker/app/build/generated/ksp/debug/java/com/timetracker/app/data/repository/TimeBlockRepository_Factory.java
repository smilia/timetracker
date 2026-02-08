package com.timetracker.app.data.repository;

import com.timetracker.app.data.local.dao.TimeBlockDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class TimeBlockRepository_Factory implements Factory<TimeBlockRepository> {
  private final Provider<TimeBlockDao> timeBlockDaoProvider;

  public TimeBlockRepository_Factory(Provider<TimeBlockDao> timeBlockDaoProvider) {
    this.timeBlockDaoProvider = timeBlockDaoProvider;
  }

  @Override
  public TimeBlockRepository get() {
    return newInstance(timeBlockDaoProvider.get());
  }

  public static TimeBlockRepository_Factory create(Provider<TimeBlockDao> timeBlockDaoProvider) {
    return new TimeBlockRepository_Factory(timeBlockDaoProvider);
  }

  public static TimeBlockRepository newInstance(TimeBlockDao timeBlockDao) {
    return new TimeBlockRepository(timeBlockDao);
  }
}
