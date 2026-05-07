package com.wkspower.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point.
 *
 * <p>The embedded BPMN engine is active (Story 2.2). Activation is configuration-only — see {@code
 * camunda.bpm.enabled: true} in {@code application.yml} and the {@link
 * com.wkspower.platform.engine} adapter package.
 *
 * <p>The bootstrap deliberately stays free of any engine-SDK import so the ArchUnit {@code
 * workflowEngineImportsLiveOnlyInEnginePackage} rule keeps its no-exceptions stance.
 *
 * <p>{@link EnableScheduling} is declared here (canonical Spring Boot location) so {@code
 * TaskSchedulingAutoConfiguration} initialises in a predictable order relative to Boot's
 * autoconfiguration chain. Placing it on a subsidiary {@code @Configuration} class can cause {@code
 * ApplicationReadyEvent} listeners to fire asynchronously, breaking production smoke tests.
 */
@SpringBootApplication
@EnableScheduling
public class WksPlatformApplication {

  public static void main(String[] args) {
    SpringApplication.run(WksPlatformApplication.class, args);
  }
}
