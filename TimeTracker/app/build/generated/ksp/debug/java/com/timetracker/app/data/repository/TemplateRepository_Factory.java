package com.timetracker.app.data.repository;

import com.timetracker.app.data.local.dao.TemplateDao;
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
public final class TemplateRepository_Factory implements Factory<TemplateRepository> {
  private final Provider<TemplateDao> templateDaoProvider;

  public TemplateRepository_Factory(Provider<TemplateDao> templateDaoProvider) {
    this.templateDaoProvider = templateDaoProvider;
  }

  @Override
  public TemplateRepository get() {
    return newInstance(templateDaoProvider.get());
  }

  public static TemplateRepository_Factory create(Provider<TemplateDao> templateDaoProvider) {
    return new TemplateRepository_Factory(templateDaoProvider);
  }

  public static TemplateRepository newInstance(TemplateDao templateDao) {
    return new TemplateRepository(templateDao);
  }
}
