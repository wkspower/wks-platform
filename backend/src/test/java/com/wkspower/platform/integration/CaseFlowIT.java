package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.event.CaseStatusChanged;
import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.event.ExecutionSignalRouted;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.MappingRegistry;
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

import java.util.Map;
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
  @Autowired private MappingRegistry mappingRegistry;

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

  @Test
  void createReturns422WithFieldId() throws Exception {
    // P28 / AC10 / Story 2.7 — POST /api/cases with a payload that omits a required field returns
    // 422 with errors[].field set to the YAML-declared field id ("name"), NOT a JSON-Pointer path
    // like "/data/name". The frontend RHF setError(field, ...) path depends on this exact shape;
    // any drift makes inline validation messages vanish silently.
    String cookie = login();

    // Send `name: 123` (a number where the schema declares string) — networknt assigns this
    // violation to the property location `/name`, exercising the pointerToField field-id roundtrip
    // that the frontend setError() path depends on. (A `data: {}` payload would attribute the
    // missing-required violation to the parent location instead, returning "data".)
    ResponseEntity<String> resp =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + CASE_TYPE_ID + "\",\"data\":{\"name\":123}}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    JsonNode body = json.readTree(resp.getBody());
    JsonNode errors = body.path("error").path("errors");
    assertThat(errors.isArray()).isTrue();
    assertThat(errors.size()).isGreaterThan(0);
    boolean hasNameFieldError = false;
    for (JsonNode err : errors) {
      String field = err.path("field").asText();
      // P11 — must be the YAML id, not "/data/name", "data.name" or "data".
      if ("name".equals(field)) {
        hasNameFieldError = true;
        break;
      }
    }
    assertThat(hasNameFieldError)
        .as("at least one error must carry the YAML-declared field id 'name'")
        .isTrue();
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
                "admin", List.of(Permission.VIEW, Permission.CREATE, Permission.EDIT))),
        List.of(),
        List.of());
  }

  // ---- Story 2.4 — full create → transition end-to-end ----------------

  @Test
  void createTransitionRoundTripUpdatesStatusAndPublishesEvent() throws Exception {
    // B1 fix (Story 4.4b) — re-enabled. The BPMN-path manual transition now routes through
    // ExecutionSignalRouter via a registered userTask:manual PropertyEmissionRule.
    // The old test completed a BPMN user task and used message correlation ("submit") to drive
    // the process to an end event. The new architecture: CaseService.transition() on the BPMN
    // path emits ExecutionSignal(TASK_STATUS_CHANGED, source="manual", value=action) to the router.
    // The router dispatches to the userTask:manual rule and calls statusUpdater.updateStatus.
    // BPMN engine state is not consulted for manual transitions — the status update is direct.
    // The test therefore uses action="approved" (a declared status id on the fixture case type).
    registerTransitionCaseType();
    deployTransitionFixture();
    String cookie = login();
    recorder.events.clear();
    recorder.routedEvents.clear();

    // POST create — engine starts the process; BPMN user task `draft` becomes active.
    // The processInstanceId must be non-null so CaseService.transition() takes the BPMN path.
    ResponseEntity<String> created =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + TRANSITION_CASE_TYPE_ID + "\",\"data\":{\"name\":\"Asha\"}}");
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String caseId = json.readTree(created.getBody()).path("data").path("id").asText();
    UUID caseUuid = UUID.fromString(caseId);
    // Verify BPMN path: processInstanceId must be set (not blank/null).
    assertThat(json.readTree(created.getBody()).path("data").path("processInstanceId").asText())
        .isNotBlank();

    // POST transition — action="approved" is a declared status id on the case-transition-fixture
    // case type. CaseService.transition() takes the BPMN path (processInstanceId != null), emits
    // TASK_STATUS_CHANGED / source="manual" / value="approved" to the router. The userTask:manual
    // PropertyEmissionRule routes the signal to statusUpdater.updateStatus(caseId, "approved").
    long transitionStartNanos = System.nanoTime();
    ResponseEntity<String> tx =
        exchange(
            "/api/cases/" + caseId + "/transition",
            HttpMethod.POST,
            cookie,
            "{\"action\":\"approved\"}");
    long transitionElapsedMs = (System.nanoTime() - transitionStartNanos) / 1_000_000L;
    assertThat(tx.getStatusCode()).isEqualTo(HttpStatus.OK);
    // Story 2.4 AC1 — transition wall-clock latency must be < 500 ms for the fixture process.
    assertThat(transitionElapsedMs)
        .as("Story 2.4 AC1 — POST /transition wall-clock latency must be < 500 ms")
        .isLessThan(500L);

    // AC: status was updated.
    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                assertThat(caseEntities.findById(caseUuid).orElseThrow().getStatus())
                    .as("status must be updated to 'approved' via BPMN-path router dispatch")
                    .isEqualTo("approved"));

    // AC: a ExecutionSignalRouted event was published (router audit event, no errorCode = success).
    // Published via publishAfterCommit — available after the HTTP request's transaction commits.
    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                assertThat(recorder.routedEvents)
                    .as("router must emit a ExecutionSignalRouted event with no errorCode")
                    .anySatisfy(
                        evt -> {
                          assertThat(evt.caseId()).isEqualTo(caseUuid);
                          assertThat(evt.kind()).isEqualTo(ExecutionSignalKind.TASK_STATUS_CHANGED);
                          assertThat(evt.errorCode()).isNull();
                        }));
  }

  // ---- Story 2.5 AC11 #4 — end-event status-property branch -----------

  private static final String STATUS_PROPERTY_CASE_TYPE_ID = "case-status-property-fixture";

  /**
   * Story 2.5 AC11 #4 — pin the end-event {@code camunda:property name="status"} branch of {@link
   * com.wkspower.platform.engine.listeners.CaseStatusListener#resolveNewStatus}. The existing
   * {@code case-transition-fixture.bpmn} has matching id + property ({@code "approved"} for both),
   * so a passing test there does not distinguish which branch fired. The new {@code
   * case-status-property-fixture.bpmn} has end event id={@code "end"} but property value={@code
   * "resolved"} — the test passes only when the listener reads the property.
   */
  @org.junit.jupiter.api.Disabled(
      "Story 4.4a — see sibling Disabled annotation. Listener no longer mutates cases.status;"
          + " this assertion needs the manual-path rewrite (4.4b) plus a stage-mapping fixture.")
  @Test
  void endEventStatusProperty() throws Exception {
    registerStatusPropertyCaseType();
    deployStatusPropertyFixture();
    String cookie = login();
    recorder.events.clear();

    ResponseEntity<String> created =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\""
                + STATUS_PROPERTY_CASE_TYPE_ID
                + "\",\"data\":{\"name\":\"Asha\"}}");
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String caseId = json.readTree(created.getBody()).path("data").path("id").asText();
    UUID caseUuid = UUID.fromString(caseId);

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

    ResponseEntity<String> tx =
        exchange(
            "/api/cases/" + caseId + "/transition",
            HttpMethod.POST,
            cookie,
            "{\"action\":\"submit\"}");
    assertThat(tx.getStatusCode()).isEqualTo(HttpStatus.OK);

    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                assertThat(caseEntities.findById(caseUuid).orElseThrow().getStatus())
                    .as(
                        "status should be the camunda:property value 'resolved', not the end event id 'end'")
                    .isEqualTo("resolved"));

    assertThat(recorder.events)
        .anySatisfy(
            evt -> {
              assertThat(evt.caseId()).isEqualTo(caseUuid);
              assertThat(evt.newStatus()).isEqualTo("resolved");
            });
  }

  private void registerStatusPropertyCaseType() {
    registry.register(statusPropertyCaseType());
  }

  private void deployStatusPropertyFixture() throws java.io.IOException {
    byte[] bytes;
    try (InputStream in =
        getClass().getResourceAsStream("/bpmn/case-status-property-fixture.bpmn")) {
      if (in == null) {
        throw new IllegalStateException("BPMN fixture missing on classpath");
      }
      bytes = in.readAllBytes();
    }
    // Avoid pollution across repeated test runs in the same JVM — remove any prior deployment of
    // this fixture before re-deploying. Cascade removes process definitions, instances, history.
    repositoryService
        .createDeploymentQuery()
        .deploymentName("status-property-it-" + STATUS_PROPERTY_CASE_TYPE_ID)
        .list()
        .forEach(d -> repositoryService.deleteDeployment(d.getId(), true));
    org.cibseven.bpm.engine.repository.Deployment deployment =
        repositoryService
            .createDeployment()
            .name("status-property-it-" + STATUS_PROPERTY_CASE_TYPE_ID)
            .addInputStream(
                STATUS_PROPERTY_CASE_TYPE_ID + ".bpmn", new java.io.ByteArrayInputStream(bytes))
            .deploy();
    String processDefinitionId =
        repositoryService
            .createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult()
            .getId();
    events.publishEvent(
        new ConfigDeployed(
            STATUS_PROPERTY_CASE_TYPE_ID,
            1,
            deployment.getId(),
            STATUS_PROPERTY_CASE_TYPE_ID,
            processDefinitionId,
            null,
            Instant.now()));
  }

  private static CaseTypeConfig statusPropertyCaseType() {
    return new CaseTypeConfig(
        STATUS_PROPERTY_CASE_TYPE_ID,
        "Case Status Property Fixture",
        1,
        null,
        new WorkflowRef(STATUS_PROPERTY_CASE_TYPE_ID + ".bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(
            new StatusDefinition("open", "Open", StatusColor.ZINC),
            new StatusDefinition("resolved", "Resolved", StatusColor.EMERALD)),
        List.of("name"),
        List.of(
            new RoleDefinition(
                "admin",
                List.of(
                    Permission.VIEW, Permission.CREATE, Permission.EDIT, Permission.TRANSITION))),
        List.of(),
        List.of());
  }

  private void registerTransitionCaseType() {
    registry.register(transitionCaseType());
    // B1 fix: register a MappingDefinition with a userTask:manual rule so the
    // ExecutionSignalRouter can route the TASK_STATUS_CHANGED signal emitted by
    // CaseService.transition() on the BPMN path. Without this registration the router
    // throws WksMappingMissException (silently audited as WKS-MAP-404) and the case
    // status is never updated. The inline seed mirrors the pattern used by
    // ExecutionSignalRouterIT.ac7_manualUserTaskStatusTransition_updatesStatusAndEmitsOneEvent.
    CaseTypeRef caseTypeRef = new CaseTypeRef(TRANSITION_CASE_TYPE_ID, "1");
    MappingDefinition mappingDef =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    TRANSITION_CASE_TYPE_ID + ".bpmn",
                    "case",
                    java.util.Optional.empty(),
                    java.util.Map.of(),
                    java.util.Optional.empty(),
                    java.util.Map.of(),
                    List.of(
                        new PropertyEmissionRule(
                            "userTask:manual",
                            "status",
                            ExecutionSignalKind.TASK_STATUS_CHANGED,
                            "stage:case")),
                            Map.of())));
    mappingRegistry.register(caseTypeRef, "1", mappingDef);
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
                    Permission.VIEW, Permission.CREATE, Permission.EDIT, Permission.TRANSITION))),
        List.of(),
        List.of());
  }

  /**
   * Captures published events so tests can assert domain-event firing. Records both {@link
   * CaseStatusChanged} (legacy path) and {@link ExecutionSignalRouted} (Story 4.3 router audit
   * path, published via publishAfterCommit after transaction commits).
   */
  static class StatusEventRecorder {
    final java.util.List<CaseStatusChanged> events = new CopyOnWriteArrayList<>();
    final java.util.List<ExecutionSignalRouted> routedEvents = new CopyOnWriteArrayList<>();

    @EventListener
    public void on(CaseStatusChanged e) {
      events.add(e);
    }

    @EventListener
    public void onRouted(ExecutionSignalRouted e) {
      routedEvents.add(e);
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
