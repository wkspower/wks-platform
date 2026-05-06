package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.infrastructure.license.LicenseServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Wires {@link LicenseService} into the Spring context (Story 7.1).
 *
 * <p>Follows the {@code ConfigServiceConfig} pattern: explicit {@code @Bean} factory methods,
 * constructor injection, no {@code @Component} on the implementation class itself.
 *
 * <p>{@link EnableScheduling} activates the {@code @Scheduled} hot-reload polling in {@link
 * LicenseServiceImpl}. This annotation is idempotent — declaring it here rather than on {@code
 * WksPlatformApplication} keeps the concern local to the license subsystem.
 */
@Configuration
@EnableScheduling
public class LicenseConfig {

  @Bean
  public LicenseService licenseService(@Value("${wks.license.file:}") String licenseFilePath) {
    return new LicenseServiceImpl(licenseFilePath, LicenseServiceImpl.loadBundledPublicKey());
  }
}
