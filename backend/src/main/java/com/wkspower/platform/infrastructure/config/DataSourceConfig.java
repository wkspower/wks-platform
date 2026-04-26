package com.wkspower.platform.infrastructure.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Explicit {@code @Primary} DataSource bean.
 *
 * <p>Only {@code dataSource()} carries {@code @Primary}. {@code dataSourceProperties()} does not —
 * adding {@code @Primary} there would conflict with the BPMN engine starter's own {@code
 * DataSourceProperties} bean when the engine activates in Story 2.2.
 *
 * <p>The BPMN engine registers its own DataSource when activated (Story 2.2). Without a
 * {@code @Primary} marker on ours, Spring would fail to pick between the two. Establishing the
 * pattern now prevents regression later.
 */
@Configuration
public class DataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  public DataSource dataSource(@Qualifier("dataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }
}
