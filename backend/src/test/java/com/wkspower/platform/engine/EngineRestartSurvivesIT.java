package com.wkspower.platform.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.WksPlatformApplication;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * AC9: a deployment written by one application instance must survive a clean shutdown and a fresh
 * boot against the same H2 file. This is the smoke test that catches a Flyway-vs-MyBatis collision
 * before it ships.
 *
 * <p>We boot {@link WksPlatformApplication} twice via {@link SpringApplication#run(Class,
 * String...)} — NOT {@code @SpringBootTest} — to avoid poisoning the context cache.
 */
class EngineRestartSurvivesIT {

  private static final String PROCESS_KEY = "restart-survival-process";

  @Test
  void deploymentSurvivesAcrossBoots(@TempDir Path dbDir) {
    String jdbcUrl = "jdbc:h2:file:" + dbDir.resolve("restart-it") + ";DB_CLOSE_DELAY=-1";
    String[] args = baseArgs(jdbcUrl);

    String firstBootProcessDefinitionId;
    String firstBootDeploymentId;
    Instant firstBootDeployedAt;
    int firstBootVersion;
    try (ConfigurableApplicationContext ctx =
        SpringApplication.run(WksPlatformApplication.class, args)) {
      WorkflowEngine engine = ctx.getBean(WorkflowEngine.class);
      DeploymentRequest request =
          new DeploymentRequest(
              "restart-survival",
              PROCESS_KEY,
              simpleBpmn().getBytes(StandardCharsets.UTF_8),
              PROCESS_KEY,
              1);
      DeploymentResult deployment = engine.deploy(request);
      firstBootProcessDefinitionId = deployment.processDefinitionId();
      firstBootDeploymentId = deployment.deploymentId();
      firstBootDeployedAt = deployment.deployedAt();
      firstBootVersion = deployment.version();
      assertThat(firstBootProcessDefinitionId).isNotBlank();
    }

    // Second boot — same DB file, fresh JVM/Spring context.
    try (ConfigurableApplicationContext ctx =
        SpringApplication.run(WksPlatformApplication.class, args)) {
      WorkflowEngine engine = ctx.getBean(WorkflowEngine.class);
      Optional<DeploymentInfo> latest = engine.latestDeployment(PROCESS_KEY);
      assertThat(latest)
          .as("AC9: deployment from the previous boot must be visible after restart")
          .isPresent();
      DeploymentInfo info = latest.get();
      assertThat(info.processDefinitionId())
          .as("AC9: latest deployment id must match what was written before shutdown")
          .isEqualTo(firstBootProcessDefinitionId);
      // The next assertions catch silent destructive DDL: if the engine recreated ACT_RE_*
      // tables on the second boot (e.g. due to a future schema-update regression that drops +
      // re-creates rather than evolves in place), the deployment row would be re-minted with
      // fresh ids/timestamps and these checks would fire.
      assertThat(info.deploymentId())
          .as(
              "AC9: deploymentId must be byte-stable across restart — a different id implies the"
                  + " engine recreated the deployment row, indicating destructive DDL on boot 2")
          .isEqualTo(firstBootDeploymentId);
      assertThat(info.version())
          .as("AC9: version must match the pre-restart deployment exactly")
          .isEqualTo(firstBootVersion);
      assertThat(info.deployedAt())
          .as(
              "AC9: deployedAt must match the pre-restart timestamp — a re-minted row would carry"
                  + " a newer Instant")
          .isEqualTo(firstBootDeployedAt);
    }
  }

  private static String[] baseArgs(String jdbcUrl) {
    return new String[] {
      "--spring.profiles.active=dev",
      "--server.port=0",
      "--spring.datasource.url=" + jdbcUrl,
      "--wks.case-types.dir=",
      "--camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive=false"
    };
  }

  private static String simpleBpmn() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
        + " xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\""
        + " targetNamespace=\"http://wkspower.com/bpmn/test\">"
        + "<bpmn:process id=\""
        + PROCESS_KEY
        + "\" isExecutable=\"true\" camunda:historyTimeToLive=\"30\">"
        + "<bpmn:startEvent id=\"start\"/>"
        + "<bpmn:userTask id=\"draft\">"
        + "<bpmn:extensionElements>"
        + "<camunda:properties>"
        + "<camunda:property name=\"archetype\" value=\"submit_for_processing\"/>"
        + "</camunda:properties>"
        + "</bpmn:extensionElements>"
        + "</bpmn:userTask>"
        + "<bpmn:endEvent id=\"end\"/>"
        + "<bpmn:sequenceFlow id=\"f1\" sourceRef=\"start\" targetRef=\"draft\"/>"
        + "<bpmn:sequenceFlow id=\"f2\" sourceRef=\"draft\" targetRef=\"end\"/>"
        + "</bpmn:process>"
        + "</bpmn:definitions>";
  }
}
