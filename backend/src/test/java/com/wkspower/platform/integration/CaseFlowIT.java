package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.event.CaseStatusChanged;
import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.awaitility.Awaitility;
import org.cibseven.bpm.engine.ProcessEngine;
import org.cibseven.bpm.engine.RepositoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Story 2.3 D5b — end-to-end case flow over HTTP. Boots the full Spring context, registers a
 * loan-application case type + deploys a minimal BPMN through the engine, then drives {@code POST →
 * GET → PUT} (happy path) and a second {@code PUT} that races on the optimistic-lock version (409).
 * Uses cookie-session auth so the real {@code @AuthenticationPrincipal +
 * CaseTypePermissionEvaluator} chain runs end-to-end.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.ActiveProfiles("dev")
@Import(CaseFlowIT.RecorderConfig.class)
class CaseFlowIT {

  // Uses the seeded `admin` role (V202604170001) — the only role that exists out of the box. The
  // case-type below grants admin every verb so the full happy + conflict paths can run end-to-end.
  private static final String EMAIL = "caseflow-admin@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "loan-application";
  private static final String PROCESS_KEY = "loan-application-flow";
  private static final String TRANSITION_CASE_TYPE_ID = "case-transition-fixture";

  @TempDir static java.nio.file.Path dbDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) {
    // File-mode H2 mirrors CibSevenWorkflowEngineIT — engine BLOB columns dislike in-memory H2.
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("caseflow-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("wks.case-types.dir", () -> "");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
    reg.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
  }

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private RepositoryService repositoryService;
  @Autowired private ProcessEngine processEngine;
  @Autowired private CaseEntityRepository caseEntities;
  @Autowired private ApplicationEventPublisher events;
  @Autowired private ObjectMapper json;
  @Autowired private StatusEventRecorder recorder;

  @BeforeEach
  void setup() {
    if (users.findByEmail(EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
    registry.register(loanType());
    org.cibseven.bpm.engine.repository.Deployment deployment =
        repositoryService
            .createDeployment()
            .name("flow-it-" + PROCESS_KEY)
            .addInputStream(
                PROCESS_KEY + ".bpmn",
                new java.io.ByteArrayInputStream(
                    simpleBpmn(PROCESS_KEY).getBytes(StandardCharsets.UTF_8)))
            .deploy();
    String processDefinitionId =
        repositoryService
            .createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult()
            .getId();
    events.publishEvent(
        new ConfigDeployed(
            CASE_TYPE_ID,
            1,
            deployment.getId(),
            PROCESS_KEY,
            processDefinitionId,
            null,
            Instant.now()));
  }

  @Test
  void postGetPutFlowRoundTripsThroughEngineAndRepository() throws Exception {
    String cookie = login();

    // POST → 201 with id
    ResponseEntity<String> created =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + CASE_TYPE_ID + "\",\"data\":{\"name\":\"Alice\"}}");
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode body = json.readTree(created.getBody());
    String id = body.path("data").path("id").asText();
    assertThat(id).isNotEmpty();
    assertThat(body.path("data").path("processInstanceId").asText()).isNotEmpty();

    // GET → 200, version starts at 1 after JPA bumps from 0 on insert
    ResponseEntity<String> got = exchange("/api/cases/" + id, HttpMethod.GET, cookie, null);
    assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
    long version = json.readTree(got.getBody()).path("data").path("version").asLong();
    assertThat(version).isGreaterThanOrEqualTo(0L);

    // PUT → 200 happy path
    ResponseEntity<String> updated =
        exchange(
            "/api/cases/" + id,
            HttpMethod.PUT,
            cookie,
            "{\"data\":{\"name\":\"Bob\"},\"version\":" + version + "}");
    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Second PUT with the now-stale version → 409 WKS-RTM-409 (optimistic-lock)
    ResponseEntity<String> conflict =
        exchange(
            "/api/cases/" + id,
            HttpMethod.PUT,
            cookie,
            "{\"data\":{\"name\":\"Carol\"},\"version\":" + version + "}");
    assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(conflict.getBody()).contains("WKS-RTM-409");
  }

  private String login() {
    ResponseEntity<String> resp =
        rest.postForEntity("/api/auth/login", new LoginRequest(EMAIL, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookie).isNotNull();
    int semi = setCookie.indexOf(';');
    return semi > 0 ? setCookie.substring(0, semi) : setCookie;
  }

  private ResponseEntity<String> exchange(
      String path, HttpMethod method, String cookie, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
  }

  private static CaseTypeConfig loanType() {
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Loan Application",
        1,
        null,
        new WorkflowRef(PROCESS_KEY + ".bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(
            new RoleDefinition(
                "admin", List.of(Permission.VIEW, Permission.CREATE, Permission.EDIT))));
  }

  // ---- Story 2.4 — full create → transition end-to-end ----------------

  @Test
  void createTransitionRoundTripUpdatesStatusAndPublishesEvent() throws Exception {
    registerTransitionCaseType();
    deployTransitionFixture();
    String cookie = login();
    recorder.events.clear();

    // POST create — engine starts the process; user task `draft` becomes active.
    ResponseEntity<String> created =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + TRANSITION_CASE_TYPE_ID + "\",\"data\":{\"name\":\"Asha\"}}");
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String caseId = json.readTree(created.getBody()).path("data").path("id").asText();
    UUID caseUuid = UUID.fromString(caseId);

    // The current user task is `draft`. Complete it through the public API so the listener fires.
    String taskId =
        processEngine
            .getTaskService()
            .createTaskQuery()
            .processInstanceId(caseEntities.findById(caseUuid).orElseThrow().getProcessInstanceId())
            .singleResult()
            .getId();
    ResponseEntity<String> completed =
        exchange("/api/tasks/" + taskId + "/complete", HttpMethod.POST, cookie, "{}");
    assertThat(completed.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(completed.getBody()).contains("\"archetype\":\"draft_section\"");

    // POST transition correlates the `submit` message → process advances to the `approved` end
    // event → CaseStatusListener writes status="approved" and publishes CaseStatusChanged.
    long transitionStartNanos = System.nanoTime();
    ResponseEntity<String> tx =
        exchange(
            "/api/cases/" + caseId + "/transition",
            HttpMethod.POST,
            cookie,
            "{\"action\":\"submit\"}");
    long transitionElapsedMs = (System.nanoTime() - transitionStartNanos) / 1_000_000L;
    assertThat(tx.getStatusCode()).isEqualTo(HttpStatus.OK);
    // Story 2.4 AC1 — transition wall-clock latency must be < 500 ms for the fixture process.
    assertThat(transitionElapsedMs)
        .as("Story 2.4 AC1 — POST /transition wall-clock latency must be < 500 ms")
        .isLessThan(500L);

    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                assertThat(caseEntities.findById(caseUuid).orElseThrow().getStatus())
                    .isEqualTo("approved"));

    assertThat(recorder.events)
        .anySatisfy(
            evt -> {
              assertThat(evt.caseId()).isEqualTo(caseUuid);
              assertThat(evt.newStatus()).isEqualTo("approved");
            });
  }

  private void registerTransitionCaseType() {
    registry.register(transitionCaseType());
  }

  private void deployTransitionFixture() throws java.io.IOException {
    byte[] bytes;
    try (InputStream in = getClass().getResourceAsStream("/bpmn/case-transition-fixture.bpmn")) {
      if (in == null) {
        throw new IllegalStateException("BPMN fixture missing on classpath");
      }
      bytes = in.readAllBytes();
    }
    org.cibseven.bpm.engine.repository.Deployment deployment =
        repositoryService
            .createDeployment()
            .name("transition-it-" + TRANSITION_CASE_TYPE_ID)
            .addInputStream(
                TRANSITION_CASE_TYPE_ID + ".bpmn", new java.io.ByteArrayInputStream(bytes))
            .deploy();
    String processDefinitionId =
        repositoryService
            .createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult()
            .getId();
    events.publishEvent(
        new ConfigDeployed(
            TRANSITION_CASE_TYPE_ID,
            1,
            deployment.getId(),
            TRANSITION_CASE_TYPE_ID,
            processDefinitionId,
            null,
            Instant.now()));
  }

  private static CaseTypeConfig transitionCaseType() {
    return new CaseTypeConfig(
        TRANSITION_CASE_TYPE_ID,
        "Case Transition Fixture",
        1,
        null,
        new WorkflowRef(TRANSITION_CASE_TYPE_ID + ".bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(
            new StatusDefinition("open", "Open", StatusColor.ZINC),
            new StatusDefinition("approved", "Approved", StatusColor.EMERALD)),
        List.of("name"),
        List.of(
            new RoleDefinition(
                "admin",
                List.of(
                    Permission.VIEW, Permission.CREATE, Permission.EDIT, Permission.TRANSITION))));
  }

  /** Captures published CaseStatusChanged events so the test can assert listener firing. */
  static class StatusEventRecorder {
    final java.util.List<CaseStatusChanged> events = new CopyOnWriteArrayList<>();

    @EventListener
    public void on(CaseStatusChanged e) {
      events.add(e);
    }
  }

  @TestConfiguration
  static class RecorderConfig {
    @Bean
    StatusEventRecorder statusEventRecorder() {
      return new StatusEventRecorder();
    }
  }

  private static String simpleBpmn(String key) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
        + " xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\""
        + " targetNamespace=\"http://wkspower.com/bpmn/test\">"
        + "<bpmn:process id=\""
        + key
        + "\" isExecutable=\"true\" camunda:historyTimeToLive=\"30\">"
        + "<bpmn:startEvent id=\"start\"/>"
        + "<bpmn:endEvent id=\"end\"/>"
        + "<bpmn:sequenceFlow id=\"f1\" sourceRef=\"start\" targetRef=\"end\"/>"
        + "</bpmn:process>"
        + "</bpmn:definitions>";
  }
}
