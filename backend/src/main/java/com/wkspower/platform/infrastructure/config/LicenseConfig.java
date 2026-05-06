package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.infrastructure.license.LicenseServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires {@link LicenseService} into the Spring context (Story 7.1).
 *
 * <p>Follows the {@code ConfigServiceConfig} pattern: explicit {@code @Bean} factory methods,
 * constructor injection, no {@code @Component} on the implementation class itself.
 *
 * <p>{@code @EnableScheduling} lives on {@code WksPlatformApplication} (the canonical Spring Boot
 * location) to ensure {@code TaskSchedulingAutoConfiguration} initialises in a predictable order.
 */
@Configuration
public class LicenseConfig {

  @Bean
  public LicenseService licenseService(@Value("${wks.license.file:}") String licenseFilePath) {
    return new LicenseServiceImpl(licenseFilePath, LicenseServiceImpl.loadBundledPublicKey());
  }
}
