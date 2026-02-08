package com.timetracker.app.service.notification;

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
public final class LockScreenNotificationService_MembersInjector implements MembersInjector<LockScreenNotificationService> {
  private final Provider<TimeTrackerDatabase> databaseProvider;

  public LockScreenNotificationService_MembersInjector(
      Provider<TimeTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  public static MembersInjector<LockScreenNotificationService> create(
      Provider<TimeTrackerDatabase> databaseProvider) {
    return new LockScreenNotificationService_MembersInjector(databaseProvider);
  }

  @Override
  public void injectMembers(LockScreenNotificationService instance) {
    injectDatabase(instance, databaseProvider.get());
  }

  @InjectedFieldSignature("com.timetracker.app.service.notification.LockScreenNotificationService.database")
  public static void injectDatabase(LockScreenNotificationService instance,
      TimeTrackerDatabase database) {
    instance.database = database;
  }
}
