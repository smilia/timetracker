package com.timetracker.app.service.overlay;

import com.timetracker.app.data.local.database.TimeTrackerDatabase;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AtomicNotificationService_MembersInjector implements MembersInjector<AtomicNotificationService> {
  private final Provider<TimeTrackerDatabase> databaseProvider;

  public AtomicNotificationService_MembersInjector(Provider<TimeTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  public static MembersInjector<AtomicNotificationService> create(
      Provider<TimeTrackerDatabase> databaseProvider) {
    return new AtomicNotificationService_MembersInjector(databaseProvider);
  }

  @Override
  public void injectMembers(AtomicNotificationService instance) {
    injectDatabase(instance, databaseProvider.get());
  }

  @InjectedFieldSignature("com.timetracker.app.service.overlay.AtomicNotificationService.database")
  public static void injectDatabase(AtomicNotificationService instance,
      TimeTrackerDatabase database) {
    instance.database = database;
  }
}
