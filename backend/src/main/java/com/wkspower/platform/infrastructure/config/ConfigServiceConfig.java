package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.service.ConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the pure-Java {@link ConfigService} (domain) with its infrastructure ports so Spring can
 * inject it into the startup loader and (future) admin controller.
 */
@Configuration
public class ConfigServiceConfig {

  @Bean
  public ConfigService configService(CaseTypeSource source, CaseTypeRegistrar registrar) {
    return new ConfigService(source, registrar);
  }
}
