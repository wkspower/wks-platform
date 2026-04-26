package com.wkspower.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point.
 *
 * <p>The embedded BPMN engine is active (Story 2.2). Activation is configuration-only — see {@code
 * camunda.bpm.enabled: true} in {@code application.yml} and the {@link
 * com.wkspower.platform.engine} adapter package.
 *
 * <p>The bootstrap deliberately stays free of any engine-SDK import so the ArchUnit {@code
 * workflowEngineImportsLiveOnlyInEnginePackage} rule keeps its no-exceptions stance.
 */
@SpringBootApplication
public class WksPlatformApplication {

  public static void main(String[] args) {
    SpringApplication.run(WksPlatformApplication.class, args);
  }
}
