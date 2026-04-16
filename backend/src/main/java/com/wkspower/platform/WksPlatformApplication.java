package com.wkspower.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point.
 *
 * <p>CIB seven auto-configuration is excluded in Story 1.1; the engine is activated in Story 2.2
 * once schema and case-type configuration exist. See also {@code camunda.bpm.enabled: false} in
 * application.yml (belt-and-suspenders).
 *
 * <p>The auto-config is referenced by <b>string name</b> rather than class literal so this
 * bootstrap file does not import {@code org.cibseven.*} — that keeps the ArchUnit rule
 * ({@code onlyEngineImportsCibSeven}) strict without exceptions.
 */
@SpringBootApplication(
    excludeName = {"org.cibseven.bpm.spring.boot.starter.CamundaBpmAutoConfiguration"})
public class WksPlatformApplication {

  public static void main(String[] args) {
    SpringApplication.run(WksPlatformApplication.class, args);
  }
}
