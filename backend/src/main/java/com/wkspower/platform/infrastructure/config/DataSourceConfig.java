package com.wkspower.platform.infrastructure.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Explicit {@code @Primary} DataSource bean.
 *
 * <p>CIB seven registers its own DataSource when the engine activates (Story 2.2). Without a
 * {@code @Primary} marker on ours, Spring would fail to pick between the two. Establishing the
 * pattern now prevents regression later.
 */
@Configuration
public class DataSourceConfig {

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  public DataSource dataSource(DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }
}
