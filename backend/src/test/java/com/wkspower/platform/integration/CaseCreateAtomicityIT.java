package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.event.StageEntered;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseStageHistoryJpaRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Story 3.1 code review B1 (2026-05-05) — atomicity proof for {@code POST /api/cases} when the
 * case-type declares stages. {@code CaseController.create} carries {@code @Transactional}, so a
 * forced failure inside {@code WksStageAdvancer.bootstrap → StageRepository.appendTransition} must
 * roll back the case insert AND the {@code case_stage_history} PENDING rows AND must NOT publish
 * the {@code StageEntered} bootstrap event (which is registered with {@code afterCommit}).
 *
 * <p>The {@code CaseCreated} event uses the eager {@code publish} path and is permitted to surface;
 * the test pins ONLY the after-commit stage event suppression because that is the post-pivot wire
 * contract for stage lifecycle (Story 3.1 AC11).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Import(CaseCreateAtomicityIT.RecorderConfig.class)
class CaseCreateAtomicityIT {

  private static final String EMAIL = "atomicity-admin@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "atomicity-staged";
  private static final String PROCESS_KEY = "atomicity-staged-flow";

  @TempDir static java.nio.file.Path dbDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) {
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("atomicity-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("wks.case-types.dir", () -> "");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
    reg.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
  }

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private RepositoryService repositoryService;
  @Autowired private CaseEntityRepository caseEntities;
  @Autowired private CaseStageHistoryJpaRepository historyRepo;
  @Autowired private ApplicationEventPublisher events;
  @Autowired private ObjectMapper json;
  @Autowired private StageEnteredRecorder recorder;

  @MockitoSpyBean private StageRepository stageRepository;

  @BeforeEach
  void setup() throws Exception {
    if (users.findByEmail(EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
    registry.register(stagedCaseType());
    var deployment =
        repositoryService
            .createDeployment()
            .name("atomicity-it-" + PROCESS_KEY)
            .addInputStream(
                PROCESS_KEY + ".bpmn",
                new java.io.ByteArrayInputStream(simpleBpmn().getBytes(StandardCharsets.UTF_8)))
            .deploy();
    String processDefinitionId =
        repositoryService
            .createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .singleResult()
            .getId();
    events.publishEvent(
        new com.wkspower.platform.domain.event.ConfigDeployed(
            CASE_TYPE_ID,
            1,
            deployment.getId(),
            PROCESS_KEY,
            processDefinitionId,
            null,
            Instant.now()));
    recorder.events.clear();
  }

  @Test
  void caseCreateRollsBackWhenStageBootstrapFails() throws Exception {
    String cookie = login();

    // Force WksStageAdvancer.bootstrap → appendTransition to throw mid-create. The controller's
    // @Transactional must roll back the case_stage_history materialise rows AND the cases insert.
    long caseRowsBefore = caseEntities.count();
    long historyRowsBefore = historyRepo.count();

    doThrow(new RuntimeException("forced bootstrap failure (atomicity B1)"))
        .when(stageRepository)
        .appendTransition(any());

    ResponseEntity<String> resp =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + CASE_TYPE_ID + "\",\"data\":{\"name\":\"Asha\"}}");

    // Forced unchecked exception — handler maps via WKS-RTM-500 (default). The exact status is
    // less important than the post-condition: rollback must have happened.
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    // 1) cases table — no new row.
    assertThat(caseEntities.count()).as("case row must be rolled back").isEqualTo(caseRowsBefore);
    // 2) case_stage_history — no PENDING rows materialised either.
    assertThat(historyRepo.count())
        .as("PENDING stage rows must be rolled back")
        .isEqualTo(historyRowsBefore);
    // 3) The after-commit StageEntered bootstrap event MUST NOT have fired (rollback ⇒ no commit
    //    ⇒ no afterCommit synchronization). This is the SpringEventPublisher.publishAfterCommit
    //    contract — the test would catch a future regression that publishes stage events eagerly.
    assertThat(recorder.events).as("bootstrap StageEntered must not publish on rollback").isEmpty();
  }

  // ---- helpers ----------------------------------------------------------

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

  private static CaseTypeConfig stagedCaseType() {
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Atomicity Staged Fixture",
        1,
        null,
        new WorkflowRef(PROCESS_KEY + ".bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(
            new RoleDefinition(
                "admin",
                List.of(
                    Permission.VIEW, Permission.CREATE, Permission.EDIT, Permission.TRANSITION))),
        List.of(
            new StageDefinition("intake", "Intake", 0), new StageDefinition("review", "Review", 1)),
        List.of());
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
        + "<bpmn:endEvent id=\"end\"/>"
        + "<bpmn:sequenceFlow id=\"f1\" sourceRef=\"start\" targetRef=\"end\"/>"
        + "</bpmn:process>"
        + "</bpmn:definitions>";
  }

  /** Captures the bootstrap StageEntered event so we can assert it did NOT fire on rollback. */
  static class StageEnteredRecorder {
    final List<StageEntered> events = new CopyOnWriteArrayList<>();

    @EventListener
    public void on(StageEntered e) {
      events.add(e);
    }
  }

  @TestConfiguration
  static class RecorderConfig {
    @Bean
    StageEnteredRecorder stageEnteredRecorder() {
      return new StageEnteredRecorder();
    }
  }
}
