package com.timetracker.app.ui.screens.statistics;

import com.timetracker.app.data.repository.CategoryRepository;
import com.timetracker.app.data.repository.TemplateRepository;
import com.timetracker.app.data.repository.TimeBlockRepository;
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
public final class StatisticsViewModel_Factory implements Factory<StatisticsViewModel> {
  private final Provider<TimeBlockRepository> timeBlockRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<TemplateRepository> templateRepositoryProvider;

  public StatisticsViewModel_Factory(Provider<TimeBlockRepository> timeBlockRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<TemplateRepository> templateRepositoryProvider) {
    this.timeBlockRepositoryProvider = timeBlockRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.templateRepositoryProvider = templateRepositoryProvider;
  }

  @Override
  public StatisticsViewModel get() {
    return newInstance(timeBlockRepositoryProvider.get(), categoryRepositoryProvider.get(), templateRepositoryProvider.get());
  }

  public static StatisticsViewModel_Factory create(
      Provider<TimeBlockRepository> timeBlockRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<TemplateRepository> templateRepositoryProvider) {
    return new StatisticsViewModel_Factory(timeBlockRepositoryProvider, categoryRepositoryProvider, templateRepositoryProvider);
  }

  public static StatisticsViewModel newInstance(TimeBlockRepository timeBlockRepository,
      CategoryRepository categoryRepository, TemplateRepository templateRepository) {
    return new StatisticsViewModel(timeBlockRepository, categoryRepository, templateRepository);
  }
}
