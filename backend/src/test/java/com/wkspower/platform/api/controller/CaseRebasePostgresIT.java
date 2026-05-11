package com.wkspower.platform.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Story 3.9 AC5 — Postgres-IT exercising end-to-end rebase over HTTP. Per the review remediation:
 *
 * <ul>
 *   <li>Class is NOT {@code @Transactional} — every test commits its setup writes; reads after the
 *       HTTP call go through a fresh {@link TransactionTemplate} so we only observe the committed
 *       view (irreconcilable rejection must NOT mutate {@code cases.case_type_version}).
 *   <li>Endpoint exercised via {@link TestRestTemplate} on a random port, hitting the real {@code
 *       AdminController} chain through the security filter — proves the apply path's commit
 *       boundary and audit semantics, not just the service surface.
 *   <li>Scenarios cover dry-run, apply success, irreconcilable rejection, apply-path reverse
 *       ({@code WKS-API-007}), apply-path no-op ({@code WKS-API-008}), and apply-path non-existent
 *       toVersion ({@code WKS-API-007}). All rejection paths assert the {@code cases} row is
 *       UNCHANGED via fresh-tx read.
 *   <li>Per-test UUIDs in {@code @BeforeEach} — no shared CASE_TYPE_ID literal carries state across
 *       methods.
 * </ul>
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md}: production-validation disabled.
 * Memory {@code project_postgres_it_parity_gap.md}: Postgres-IT mandatory for BYTEA reads + {@code
 * cases.case_type_version} writes.
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class CaseRebasePostgresIT {

  @Container
  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("wks")
          .withUsername("wks")
          .withPassword("wks");

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("WKS_DB_URL", POSTGRES::getJdbcUrl);
    registry.add("WKS_DB_USER", POSTGRES::getUsername);
    registry.add("WKS_DB_PASSWORD", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("WKS_ADMIN_EMAIL", () -> "admin@wkspower.local");
    registry.add("WKS_ADMIN_PASSWORD", () -> "admin");
    registry.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
    registry.add("WKS_CORS_ORIGINS", () -> "http://localhost:5173");
    registry.add(
        "camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
  }

  @Autowired private ConfigService configService;
  @Autowired private CaseRepository caseRepository;
  @Autowired private CaseTypeVersionRegistry versionRegistry;
  @Autowired private CaseEntityRepository caseEntityRepo;
  @Autowired private CaseTypeVersionJpaRepository ctVersionRepo;
  @Autowired private UserRepository userRepository;
  @Autowired private TestRestTemplate rest;
  @Autowired private ObjectMapper json;
  @Autowired private PlatformTransactionManager txManager;

  /** Resolved from the seeded admin user at {@code @BeforeEach}. */
  private UUID adminUserId;

  /** Per-test CaseType id — defeats any cross-test state leakage. */
  private String ctId;

  private TransactionTemplate freshTx;

  @BeforeEach
  void perTestSetup() {
    adminUserId =
        userRepository
            .findByEmail("admin@wkspower.local")
            .orElseThrow(() -> new IllegalStateException("Admin user not seeded"))
            .id();
    ctId = "it-rebase-" + UUID.randomUUID().toString().substring(0, 8);
    freshTx = new TransactionTemplate(txManager);
  }

  // ---- YAML helpers ----

  private static final String ROLE_BLOCK =
      """
          roles:
            - name: admin
              permissions: [view, create, edit, transition]
          """;

  /** v1: one field (name), statuses: open + closed. */
  private static byte[] v1Yaml(String ctId) {
    return ("""
        id: %s
        displayName: "Rebase IT Test"
        version: 1
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: closed
            displayName: Closed
            color: emerald
            terminal: true
        fields:
          - id: name
            displayName: Name
            type: text
            required: true
        """
            + ROLE_BLOCK)
        .formatted(ctId)
        .getBytes(StandardCharsets.UTF_8);
  }

  /** v2: additive change — adds email field (append-class, no irreconcilable). */
  private static byte[] v2Yaml(String ctId) {
    return ("""
        id: %s
        displayName: "Rebase IT Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: closed
            displayName: Closed
            color: emerald
            terminal: true
        fields:
          - id: name
            displayName: Name
            type: text
            required: true
          - id: email
            displayName: Email
            type: text
            required: false
        """
            + ROLE_BLOCK)
        .formatted(ctId)
        .getBytes(StandardCharsets.UTF_8);
  }

  /** v2 mutate-class — removes legacy-field which has data on the case. */
  private static byte[] v2RemovesLegacyFieldYaml(String ctId) {
    return ("""
        id: %s
        displayName: "Rebase IT Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: closed
            displayName: Closed
            color: emerald
            terminal: true
        fields:
          - id: name
            displayName: Name
            type: text
            required: true
        """
            + ROLE_BLOCK)
        .formatted(ctId)
        .getBytes(StandardCharsets.UTF_8);
  }

  /** v1 with legacy-field — paired with v2RemovesLegacyFieldYaml for irreconcilable scenario. */
  private static byte[] v1WithLegacyFieldYaml(String ctId) {
    return ("""
        id: %s
        displayName: "Rebase IT Test"
        version: 1
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
        fields:
          - id: name
            displayName: Name
            type: text
            required: true
          - id: legacy-field
            displayName: Legacy
            type: text
            required: false
        """
            + ROLE_BLOCK)
        .formatted(ctId)
        .getBytes(StandardCharsets.UTF_8);
  }

  private Case createCase(String ctId, int version, Map<String, Object> data) {
    Case kase =
        new Case(
            UUID.randomUUID(),
            ctId,
            version,
            "open",
            null,
            data,
            null,
            Instant.now(),
            adminUserId,
            Instant.now(),
            0L,
            null,
            null);
    // Each save commits in its own tx (the IT itself is NOT @Transactional).
    return caseRepository.save(kase);
  }

  @AfterEach
  void wipe() {
    // Each cleanup commits — no class-level @Transactional to roll the world back implicitly.
    caseEntityRepo.deleteAll();
    ctVersionRepo.deleteAll();
  }

  // ---- HTTP helpers ----

  private String login() {
    ResponseEntity<String> resp =
        rest.postForEntity(
            "/api/auth/login", new LoginRequest("admin@wkspower.local", "admin"), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookie).as("login Set-Cookie present").isNotNull();
    int semi = setCookie.indexOf(';');
    return semi > 0 ? setCookie.substring(0, semi) : setCookie;
  }

  private ResponseEntity<String> rebaseDryRun(String cookie, String ctId, UUID caseId, int to) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(
        "/api/admin/case-types/" + ctId + "/cases/" + caseId + "/rebase?to=" + to,
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);
  }

  private ResponseEntity<String> rebaseApply(String cookie, String ctId, UUID caseId, int to) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(
        "/api/admin/case-types/" + ctId + "/cases/" + caseId + "/rebase?to=" + to,
        HttpMethod.POST,
        new HttpEntity<>(headers),
        String.class);
  }

  /** Fresh-tx read — only observes committed state. */
  private int readCommittedCaseVersion(UUID caseId) {
    Integer v =
        freshTx.execute(
            status ->
                caseEntityRepo
                    .findById(caseId)
                    .orElseThrow(() -> new AssertionError("case not found: " + caseId))
                    .getCaseTypeVersion());
    return v == null ? -1 : v;
  }

  private String firstErrorCode(String body) throws Exception {
    JsonNode root = json.readTree(body);
    JsonNode error = root.path("error");
    JsonNode errorsArr = error.path("errors");
    if (errorsArr.isArray() && errorsArr.size() > 0) {
      return errorsArr.get(0).path("code").asText();
    }
    return error.path("code").asText();
  }

  // ---- Scenario 1: dry-run additive — irreconcilable=[], no DB mutation ----

  @Test
  void scenario1_dryRun_additive_noMutation() throws Exception {
    String cookie = login();

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s1");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2Yaml(ctId), "test:s1", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCase(ctId, 1, Map.of("name", "Alice"));

    ResponseEntity<String> resp = rebaseDryRun(cookie, ctId, kase.id(), 2);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode body = json.readTree(resp.getBody());
    JsonNode data = body.path("data");
    assertThat(data.path("fromVersion").asInt()).isEqualTo(1);
    assertThat(data.path("toVersion").asInt()).isEqualTo(2);
    assertThat(data.path("applied").asBoolean()).isFalse();
    assertThat(data.path("irreconcilable")).isEmpty();

    // Fresh-tx read — dry-run must NOT mutate the cases row.
    assertThat(readCommittedCaseVersion(kase.id())).isEqualTo(1);
  }

  // ---- Scenario 2: apply success — cases.case_type_version mutated ----

  @Test
  void scenario2_apply_success_mutatesVersion() throws Exception {
    String cookie = login();

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s2");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2Yaml(ctId), "test:s2", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCase(ctId, 1, Map.of("name", "Bob"));

    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 2);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode body = json.readTree(resp.getBody());
    assertThat(body.path("data").path("applied").asBoolean()).isTrue();
    assertThat(body.path("data").path("toVersion").asInt()).isEqualTo(2);

    // Fresh-tx read — apply commit must be visible to a new transaction.
    assertThat(readCommittedCaseVersion(kase.id())).isEqualTo(2);
  }

  // ---- Scenario 3: irreconcilable rejection — fresh-tx read shows row UNCHANGED ----

  @Test
  void scenario3_apply_irreconcilable_rejects422_versionUnchanged_freshTxRead() throws Exception {
    String cookie = login();

    ValidationResult v1 =
        configService.validateAndRegister("api.yaml", v1WithLegacyFieldYaml(ctId), "test:s3");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister(
            "api.yaml", v2RemovesLegacyFieldYaml(ctId), "test:s3", true, false);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCase(ctId, 1, Map.of("name", "Charlie", "legacy-field", "some-data"));

    int targetVersion = versionRegistry.currentVersion(ctId).orElseThrow();
    assertThat(targetVersion).isEqualTo(2);

    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), targetVersion);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(firstErrorCode(resp.getBody())).isEqualTo(ErrorCode.WKS_CFG_034.wire());

    // Fresh-tx read — proves the rejection did not commit a partial update.
    assertThat(readCommittedCaseVersion(kase.id())).isEqualTo(1);
  }

  // ---- Scenario 4: apply-path reverse rebase — WKS-API-007 + UNCHANGED ----

  @Test
  void scenario4_applyPath_reverseRebase_rejects_api007_unchanged() throws Exception {
    String cookie = login();

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s4");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2Yaml(ctId), "test:s4", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCase(ctId, 2, Map.of("name", "Dan"));

    // POST (apply path) reverse rebase: toVersion < fromVersion → WKS-API-007
    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 1);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(firstErrorCode(resp.getBody())).isEqualTo(ErrorCode.WKS_API_007.wire());

    // Fresh-tx read — row UNCHANGED.
    assertThat(readCommittedCaseVersion(kase.id())).isEqualTo(2);
  }

  // ---- Scenario 5: apply-path non-existent toVersion — WKS-API-007 + UNCHANGED ----

  @Test
  void scenario5_applyPath_nonExistentToVersion_rejects_api007_unchanged() throws Exception {
    String cookie = login();

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s5");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();

    Case kase = createCase(ctId, 1, Map.of("name", "Eve"));

    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 99);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(firstErrorCode(resp.getBody())).isEqualTo(ErrorCode.WKS_API_007.wire());

    assertThat(readCommittedCaseVersion(kase.id())).isEqualTo(1);
  }

  // ---- Scenario 6: apply-path no-op rebase — WKS-API-008 + UNCHANGED ----

  @Test
  void scenario6_applyPath_noOpRebase_rejects_api008_unchanged() throws Exception {
    String cookie = login();

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s6");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();

    Case kase = createCase(ctId, 1, Map.of("name", "Fae"));

    // toVersion == fromVersion → WKS-API-008 (split from WKS-API-007 reverse)
    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 1);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(firstErrorCode(resp.getBody())).isEqualTo(ErrorCode.WKS_API_008.wire());

    assertThat(readCommittedCaseVersion(kase.id())).isEqualTo(1);
  }
}
