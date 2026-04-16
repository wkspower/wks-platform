package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.HealthDto;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unauthenticated health probe. Returns version (from build-info) and process uptime.
 *
 * <p>No sensitive data is exposed — safe for external load balancers. With no Spring Security on
 * the classpath (Story 1.1), the endpoint is reachable by default; Story 1.2 adds a {@code
 * permitAll} rule for it.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

  private final BuildProperties buildProperties;

  @Autowired(required = false)
  public HealthController(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  @GetMapping("/health")
  public ApiResponse<HealthDto> health() {
    String version = buildProperties != null ? buildProperties.getVersion() : "0.0.0-unknown";
    String uptime = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()).toString();
    return ApiResponse.success(new HealthDto(version, uptime));
  }
}
