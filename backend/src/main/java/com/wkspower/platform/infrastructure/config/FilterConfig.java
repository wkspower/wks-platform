package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.audit.CorrelationIdFilter;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers cross-cutting servlet filters with explicit ordering.
 *
 * <p>{@link CorrelationIdFilter} runs first so every subsequent log line (including Spring
 * Security's authentication log) carries the {@code correlationId} MDC entry.
 *
 * <p>{@link JwtAuthenticationFilter} is a {@code @Component} which Spring Boot would otherwise
 * auto-register as a top-level servlet filter. Because the filter is added into the Spring Security
 * chain explicitly via {@code http.addFilterBefore(...)}, we disable the auto-registered copy here
 * to prevent it from running twice per request.
 */
@Configuration
public class FilterConfig {

  @Bean
  public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
      CorrelationIdFilter filter) {
    FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setOrder(1);
    registration.addUrlPatterns("/*");
    return registration;
  }

  @Bean
  public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
      JwtAuthenticationFilter filter) {
    FilterRegistrationBean<JwtAuthenticationFilter> registration =
        new FilterRegistrationBean<>(filter);
    registration.setEnabled(false); // managed by Spring Security's chain
    return registration;
  }
}
