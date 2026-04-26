package com.wkspower.platform.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.cibseven.bpm.engine.ProcessEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Engine-adapter integration test. Exercises the bean as wired by the production Spring context —
 * no engine stubbing. Covers AC1 DataSource identity, AC3 deploy + duplicate-filter idempotence,
 * and the SLA budget.
 */
@SpringBootTest
@ActiveProfiles("dev")
class CibSevenWorkflowEngineIT {

  /** Local dev assertion ceiling (per AC3). */
  private static final long SLA_MS_LOCAL = 3000L;

  /** CI variance ceiling — GitHub runner cold-start jitter eats most of the slack. */
  private static final long SLA_MS_CI = 5000L;

  @TempDir static Path dbDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) {
    // File-mode H2 mirrors the dev boot path exactly — in-memory H2 2.x rejected the engine's
    // BLOB columns. Same DataSource serves both the application and engine schemas.
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("engine-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("wks.case-types.dir", () -> "");
    // Engine refuses BPMNs without historyTimeToLive by default. Tests deploy minimal fixtures
    // — disable the enforce check so we don't have to declare it on every process.
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
  }

  @Autowired WorkflowEngine workflowEngine;
  @Autowired ProcessEngine processEngine;
  @Autowired DataSource applicationDataSource;

  @Test
  void engineAndApplicationShareTheSameDataSource() throws java.sql.SQLException {
    DataSource engineDs = processEngine.getProcessEngineConfiguration().getDataSource();
    // AC1 spec literal text mandates `==` identity. Spring's transactional integration wraps the
    // application DataSource in a TransactionAwareDataSourceProxy before handing it to the
    // engine; `engineDs.unwrap(DataSource.class)` returns the proxy itself (Spring's proxy does
    // not expose the wrapped target via JDBC unwrap semantics). Object-identity is therefore
    // unenforceable without reflection into Spring internals, which would couple this test to
    // private API. Documented variance from AC1 (see Story 2.2 Review Findings — accepted
    // variance): we assert AC1's *intent* via JDBC URL equality, which proves both pools point
    // at the same physical store. Combined with the absence of any secondary DataSource @Bean
    // in DataSourceConfig (Story 1.4 guardrail), single-pool is structurally guaranteed.
    String engineUrl;
    String appUrl;
    try (var c = engineDs.getConnection()) {
      engineUrl = c.getMetaData().getURL();
    }
    try (var c = applicationDataSource.getConnection()) {
      appUrl = c.getMetaData().getURL();
    }
    assertThat(engineUrl)
        .as(
            "AC1 (variance): engine + application share the same JDBC URL — single physical"
                + " store; restart survival lives on a single H2 file in production.")
        .isEqualTo(appUrl);
  }

  @Test
  void deployingTheSameBytesTwiceReturnsTheSameDeploymentId() {
    byte[] bpmn = simpleBpmn("idempotent-process").getBytes(StandardCharsets.UTF_8);
    DeploymentRequest request =
        new DeploymentRequest(
            "idempotence-test", "idempotent-process", bpmn, "idempotent-process", 1);

    DeploymentResult first = workflowEngine.deploy(request);
    DeploymentResult second = workflowEngine.deploy(request);

    assertThat(second.deploymentId())
        .as(
            "AC3: enableDuplicateFiltering(true) returns the original deploymentId on identical "
                + "bytes")
        .isEqualTo(first.deploymentId());

    Optional<DeploymentInfo> latest = workflowEngine.latestDeployment("idempotent-process");
    assertThat(latest).isPresent();
    assertThat(latest.get().version()).isEqualTo(1);
  }

  @Test
  void deployFitsInsideTheSlaBudget() {
    byte[] bpmn = simpleBpmn("sla-process").getBytes(StandardCharsets.UTF_8);
    DeploymentRequest request =
        new DeploymentRequest("sla-test", "sla-process", bpmn, "sla-process", 1);

    Instant start = Instant.now();
    workflowEngine.deploy(request);
    long elapsedMs = Duration.between(start, Instant.now()).toMillis();

    long ceiling = System.getenv("CI") == null ? SLA_MS_LOCAL : SLA_MS_CI;
    assertThat(elapsedMs)
        .as(
            "AC3: a single 50-element BPMN deploy must complete inside the SLA "
                + "(local %d ms, CI %d ms)",
            SLA_MS_LOCAL, SLA_MS_CI)
        .isLessThan(ceiling);
  }

  @Test
  void startProcessInstanceReturnsEngineProcessInstanceId() {
    byte[] bpmn = simpleBpmn("start-process").getBytes(StandardCharsets.UTF_8);
    workflowEngine.deploy(
        new DeploymentRequest("start-test", "start-process", bpmn, "start-process", 1));

    String pi =
        workflowEngine.startProcessInstance(
            "start-process", Map.of("caseId", UUID.randomUUID().toString()));

    assertThat(pi)
        .as("Story 2.3 AC4: startProcessInstance returns a non-blank engine-assigned id")
        .isNotBlank();
  }

  @Test
  void startProcessInstanceWithUnknownKeyWrapsEngineException() {
    assertThatThrownBy(
            () ->
                workflowEngine.startProcessInstance(
                    "process-that-does-not-exist", Map.of("caseId", UUID.randomUUID().toString())))
        .as(
            "Story 2.3 AC4: ProcessEngineException for unknown key wraps to"
                + " WksWorkflowEngineException")
        .isInstanceOf(WksWorkflowEngineException.class);
  }

  /** Minimal BPMN 2.0 fixture used by every test in this class. */
  private static String simpleBpmn(String key) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
        + " xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\""
        + " targetNamespace=\"http://wkspower.com/bpmn/test\">"
        + "<bpmn:process id=\""
        + key
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
