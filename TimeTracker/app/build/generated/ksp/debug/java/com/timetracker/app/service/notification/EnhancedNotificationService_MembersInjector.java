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
public final class EnhancedNotificationService_MembersInjector implements MembersInjector<EnhancedNotificationService> {
  private final Provider<TimeTrackerDatabase> databaseProvider;

  public EnhancedNotificationService_MembersInjector(
      Provider<TimeTrackerDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  public static MembersInjector<EnhancedNotificationService> create(
      Provider<TimeTrackerDatabase> databaseProvider) {
    return new EnhancedNotificationService_MembersInjector(databaseProvider);
  }

  @Override
  public void injectMembers(EnhancedNotificationService instance) {
    injectDatabase(instance, databaseProvider.get());
  }

  @InjectedFieldSignature("com.timetracker.app.service.notification.EnhancedNotificationService.database")
  public static void injectDatabase(EnhancedNotificationService instance,
      TimeTrackerDatabase database) {
    instance.database = database;
  }
}
