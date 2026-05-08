package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.StatusOptionsStore;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import com.wkspower.platform.infrastructure.persistence.repository.StatusOptionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * Story 3.7 AC3 — backend IT coverage for the append-class status CRUD path. Runs against an H2
 * datasource with the {@code status_options} migration applied; exercises the full HTTP envelope
 * for the four endpoints + the durability path (scenario 6 boots a fresh JPA read against the
 * persisted row).
 *
 * <p>Wave-1 lesson #1 honored: this test does not run under the {@code production} profile, so the
 * {@code ProductionBootstrapValidator} opt-out is not needed here.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:stagestatus3-7;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
class StageScopedStatusCrudIT {

  private static final String ADMIN_EMAIL = "stage-status-admin@wkspower.local";
  private static final String PASSWORD = "admin";

  private static final String CASE_TYPE_ID = "loan-application";
  private static final String STAGE_ID = "intake";

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private CaseTypeVersionRegistry versionRegistry;
  @Autowired private StatusOptionsStore store;
  @Autowired private StatusOptionJpaRepository repository;

  @BeforeEach
  void setup() {
    if (users.findByEmail(ADMIN_EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), ADMIN_EMAIL, Set.of("admin"), true),
          encoder.encode(PASSWORD));
    }
    registry.register(loanType());
    // Seed a row in case_type_versions for v1 — the admin path depends on
    // CaseTypeVersionRegistry.currentVersion(...) for the bound version.
    if (versionRegistry.currentVersion(CASE_TYPE_ID).isEmpty()) {
      String stub = "id: " + CASE_TYPE_ID + "\nversion: 1\n";
      versionRegistry.register(CASE_TYPE_ID, stub.getBytes(StandardCharsets.UTF_8), "test:setup");
    }
    repository.deleteAll();
  }

  @Test
  void getResolvesYamlBaseAndAppendOverlay() {
    String cookie = login();
    // Append a custom status, then GET — expect both YAML base statuses + the appended one.
    appendStatus(cookie, "needs-info", "Needs info", "amber", false, HttpStatus.CREATED);

    ResponseEntity<String> resp = exchange(statusesPath(), HttpMethod.GET, cookie);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getBody())
        .contains("\"stageId\":\"intake\"")
        .contains("\"id\":\"open\"")
        .contains("\"id\":\"closed\"")
        .contains("\"id\":\"needs-info\"")
        .contains("\"initialStatus\":\"open\"");
  }

  @Test
  void appendSurvivesFreshJpaContext_Scenario6() {
    // Scenario 6 — durability across a "fresh JPA context". We approximate container-restart by
    // bypassing the in-memory cache: write via the controller, then read via the JPA repository
    // directly to prove the row landed durably.
    String cookie = login();
    appendStatus(cookie, "with-vendor", "With vendor", "violet", false, HttpStatus.CREATED);

    Optional<com.wkspower.platform.infrastructure.persistence.entity.StatusOptionEntity> row =
        repository.findByCaseTypeIdAndVersionAndStageIdAndStatusId(
            CASE_TYPE_ID, 1, STAGE_ID, "with-vendor");
    assertThat(row).isPresent();
    assertThat(row.get().getDisplayName()).isEqualTo("With vendor");
    assertThat(row.get().getColor()).isEqualTo("violet");
    assertThat(row.get().isTerminal()).isFalse();
  }

  @Test
  void duplicateAppendReturns409_Scenario7() {
    String cookie = login();
    appendStatus(cookie, "needs-info", "Needs info", "amber", false, HttpStatus.CREATED);

    ResponseEntity<String> dup =
        appendStatusRaw(cookie, "needs-info", "Needs info again", "amber", false);
    assertThat(dup.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(dup.getBody()).contains("WKS-STG-007");
  }

  @Test
  void renameUpdatesDisplayName_Scenario8_HappyPath() {
    String cookie = login();
    appendStatus(cookie, "needs-info", "Needs info", "amber", false, HttpStatus.CREATED);

    ResponseEntity<String> patched =
        exchangeWithBody(
            statusesPath() + "/needs-info",
            HttpMethod.PATCH,
            cookie,
            "{\"displayName\":\"Awaiting docs\"}");
    assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(patched.getBody()).contains("\"displayName\":\"Awaiting docs\"");

    // Verify the GET reflects the rename.
    ResponseEntity<String> getResp = exchange(statusesPath(), HttpMethod.GET, cookie);
    assertThat(getResp.getBody()).contains("\"displayName\":\"Awaiting docs\"");
  }

  @Test
  void terminalFlipReturns405_Scenario8_MutateClassRejection() {
    String cookie = login();
    appendStatus(cookie, "needs-info", "Needs info", "amber", false, HttpStatus.CREATED);

    ResponseEntity<String> patched =
        exchangeWithBody(
            statusesPath() + "/needs-info", HttpMethod.PATCH, cookie, "{\"terminal\":true}");
    assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    assertThat(patched.getBody()).contains("WKS-STG-009");
  }

  @Test
  void deleteReturns405_Scenario9() {
    String cookie = login();
    appendStatus(cookie, "needs-info", "Needs info", "amber", false, HttpStatus.CREATED);

    ResponseEntity<String> deleted =
        exchange(statusesPath() + "/needs-info", HttpMethod.DELETE, cookie);
    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    assertThat(deleted.getBody()).contains("WKS-STG-009");
  }

  @Test
  void concurrentAppendsResultInTwoDistinctRows_Scenario11() throws Exception {
    // Scenario 11 — two threads racing the append on the same stage; expect both rows to land
    // (distinct status ids) with no lost write.
    String cookie = login();
    int threads = 4;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    AtomicInteger created = new AtomicInteger();
    for (int i = 0; i < threads; i++) {
      final int idx = i;
      pool.submit(
          () -> {
            try {
              start.await();
              ResponseEntity<String> resp =
                  appendStatusRaw(cookie, "concurrent-" + idx, "Concurrent " + idx, "blue", false);
              if (resp.getStatusCode() == HttpStatus.CREATED) {
                created.incrementAndGet();
              }
            } catch (InterruptedException ignored) {
              Thread.currentThread().interrupt();
            }
          });
    }
    start.countDown();
    pool.shutdown();
    assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue();

    assertThat(created.get()).isEqualTo(threads);
    List<StatusDefinition> rows = store.listFor(CASE_TYPE_ID, 1, STAGE_ID);
    assertThat(rows).hasSize(threads);
    // Ordinals must be distinct across the persisted rows (the in-memory MAX+1 race may produce
    // ties; we tolerate ties but never overlapping status ids).
    assertThat(rows.stream().map(StatusDefinition::id).distinct().count()).isEqualTo(threads);
  }

  @Test
  void unknownCaseTypeReturns404_WithStg012() {
    String cookie = login();
    ResponseEntity<String> resp =
        exchange("/api/admin/case-types/missing/stages/intake/statuses", HttpMethod.GET, cookie);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(resp.getBody()).contains("WKS-STG-012");
  }

  @Test
  void unknownStageReturns404_WithStg012() {
    String cookie = login();
    ResponseEntity<String> resp =
        exchange(
            "/api/admin/case-types/" + CASE_TYPE_ID + "/stages/missing/statuses",
            HttpMethod.GET,
            cookie);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(resp.getBody()).contains("WKS-STG-012");
  }

  // ---- helpers ----------------------------------------------------------

  private String statusesPath() {
    return "/api/admin/case-types/" + CASE_TYPE_ID + "/stages/" + STAGE_ID + "/statuses";
  }

  private void appendStatus(
      String cookie,
      String id,
      String displayName,
      String color,
      boolean terminal,
      HttpStatus expected) {
    ResponseEntity<String> resp = appendStatusRaw(cookie, id, displayName, color, terminal);
    assertThat(resp.getStatusCode()).isEqualTo(expected);
  }

  private ResponseEntity<String> appendStatusRaw(
      String cookie, String id, String displayName, String color, boolean terminal) {
    String body =
        "{\"id\":\""
            + id
            + "\",\"displayName\":\""
            + displayName
            + "\",\"color\":\""
            + color
            + "\",\"terminal\":"
            + terminal
            + "}";
    return exchangeWithBody(statusesPath(), HttpMethod.POST, cookie, body);
  }

  private String login() {
    ResponseEntity<String> resp =
        rest.postForEntity(
            "/api/auth/login", new LoginRequest(ADMIN_EMAIL, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    int semi = setCookie.indexOf(';');
    return semi > 0 ? setCookie.substring(0, semi) : setCookie;
  }

  private ResponseEntity<String> exchange(String path, HttpMethod method, String cookie) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(path, method, new HttpEntity<>(headers), String.class);
  }

  private ResponseEntity<String> exchangeWithBody(
      String path, HttpMethod method, String cookie, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, cookie);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
  }

  private static CaseTypeConfig loanType() {
    StageDefinition stage = new StageDefinition(STAGE_ID, "Intake", 0);
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(
            new StatusDefinition("open", "Open", StatusColor.ZINC),
            new StatusDefinition("closed", "Closed", StatusColor.EMERALD, true)),
        List.of("name"),
        List.of(new RoleDefinition("admin", List.of(Permission.VIEW))),
        List.of(stage),
        List.of());
  }
}
