package com.timetracker.app.ui.screens.settings;

import android.app.Application;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<Application> applicationProvider;

  public SettingsViewModel_Factory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(applicationProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<Application> applicationProvider) {
    return new SettingsViewModel_Factory(applicationProvider);
  }

  public static SettingsViewModel newInstance(Application application) {
    return new SettingsViewModel(application);
  }
}
