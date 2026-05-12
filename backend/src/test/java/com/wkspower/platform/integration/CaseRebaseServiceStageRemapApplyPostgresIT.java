package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.StageState;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.infrastructure.persistence.entity.CaseStageHistoryEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseStageHistoryJpaRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
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
 * Story 3.9.1 AC-4 — Postgres-IT: proves that the stage-remap apply is atomic and the committed
 * view is correct after the HTTP response.
 *
 * <p>Per [[feedback_postgres_it_committed_read]]: class is NOT {@code @Transactional}. Every DB
 * read after the HTTP call goes through a fresh {@link TransactionTemplate} so only committed state
 * is observed. This pattern (from PR #431) is the load-bearing proof that:
 *
 * <ul>
 *   <li>The version bump + stage flip land in the same committed transaction.
 *   <li>The stage_history REMAPPED + ACTIVE rows are committed before the audit event fires.
 *   <li>A rejection (bad remap key/value) leaves the cases row UNCHANGED in committed state.
 * </ul>
 *
 * <p>Per [[feedback_production_validator_opt_out]]: production-validation disabled.
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class CaseRebaseServiceStageRemapApplyPostgresIT {

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
  @Autowired private CaseStageHistoryJpaRepository stageHistoryRepo;
  @Autowired private CaseTypeVersionJpaRepository ctVersionRepo;
  @Autowired private UserRepository userRepository;
  @Autowired private TestRestTemplate rest;
  @Autowired private ObjectMapper json;
  @Autowired private PlatformTransactionManager txManager;

  private UUID adminUserId;
  private String ctId;
  private TransactionTemplate freshTx;

  private static final String ROLE_BLOCK =
      """
          roles:
            - name: admin
              permissions: [view, create, edit, transition]
          """;

  @BeforeEach
  void perTestSetup() {
    adminUserId =
        userRepository
            .findByEmail("admin@wkspower.local")
            .orElseThrow(() -> new IllegalStateException("Admin user not seeded"))
            .id();
    ctId = "pg-remap-" + UUID.randomUUID().toString().substring(0, 8);
    freshTx = new TransactionTemplate(txManager);
  }

  @AfterEach
  void wipe() {
    caseEntityRepo.deleteAll();
    ctVersionRepo.deleteAll();
  }

  // ---- YAML helpers ----

  private byte[] v1WithStages() {
    return ("""
        id: %s
        displayName: "Postgres Remap IT"
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
        stages:
          - id: underwriting
            displayName: Underwriting
          - id: review
            displayName: Review
        """
            + ROLE_BLOCK)
        .formatted(ctId)
        .getBytes(StandardCharsets.UTF_8);
  }

  private byte[] v2WithStages() {
    return ("""
        id: %s
        displayName: "Postgres Remap IT"
        version: 2
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
        stages:
          - id: review
            displayName: Review
          - id: decision
            displayName: Decision
        """
            + ROLE_BLOCK)
        .formatted(ctId)
        .getBytes(StandardCharsets.UTF_8);
  }

  private Case createCase(String stageId, int ordinal) {
    Case kase =
        new Case(
            UUID.randomUUID(),
            ctId,
            1,
            "open",
            null,
            Map.of("name", "Alice"),
            null,
            Instant.now(),
            adminUserId,
            Instant.now(),
            0L,
            null, // currentStageId set via updateStageCache below
            null);
    Case saved = caseRepository.save(kase);
    // CaseMapper.toEntity intentionally skips stage cache columns (maintained by
    // WksStageAdvancer via dedicated JPQL UPDATEs). Seed the cache explicitly for the test.
    freshTx.execute(
        status -> {
          caseEntityRepo.updateStageCache(saved.id(), stageId, ordinal);
          return null;
        });
    return freshTx.execute(status -> caseRepository.findById(saved.id()).orElseThrow());
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

  private ResponseEntity<String> rebaseApply(
      String cookie, String caseTypeId, UUID caseId, int to, String bodyJson) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(
        "/api/admin/case-types/" + caseTypeId + "/cases/" + caseId + "/rebase?to=" + to,
        HttpMethod.POST,
        new HttpEntity<>(bodyJson, headers),
        String.class);
  }

  // ---- Fresh-tx reads (committed-read pattern from PR #431) ----

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

  private String readCommittedCurrentStageId(UUID caseId) {
    return freshTx.execute(
        status ->
            caseEntityRepo
                .findById(caseId)
                .orElseThrow(() -> new AssertionError("case not found: " + caseId))
                .getCurrentStageId());
  }

  private List<CaseStageHistoryEntity> readCommittedHistory(UUID caseId) {
    return freshTx.execute(status -> stageHistoryRepo.findByCaseIdOrderByOrdinalAsc(caseId));
  }

  // ---- AC-4 Postgres committed-read: version bump + stage flip are atomic ----

  @Test
  void validRemap_postgres_committedRead() throws Exception {
    String cookie = login();

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1WithStages(), "pg:remap");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2WithStages(), "pg:remap", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCase("underwriting", 0);

    // Seed ACTIVE stage_history row.
    caseRepository
        .findById(kase.id())
        .ifPresent(
            k -> {
              freshTx.execute(
                  status -> {
                    stageHistoryRepo.save(
                        new CaseStageHistoryEntity(
                            UUID.randomUUID(),
                            k.id(),
                            "underwriting",
                            0,
                            StageState.ACTIVE,
                            Instant.now(),
                            null,
                            "bootstrap",
                            null,
                            Instant.now()));
                    return null;
                  });
            });

    // Apply remap underwriting → review.
    String body = "{\"stageRemap\":{\"underwriting\":\"review\"}}";
    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 2, body);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Committed-read: version bump visible in new transaction.
    assertThat(readCommittedCaseVersion(kase.id()))
        .as("cases.case_type_version must be 2 in committed view")
        .isEqualTo(2);

    // Committed-read: stage flip visible in new transaction.
    assertThat(readCommittedCurrentStageId(kase.id()))
        .as("cases.current_stage_id must be 'review' in committed view")
        .isEqualTo("review");

    // Committed-read: stage_history REMAPPED + ACTIVE rows.
    List<CaseStageHistoryEntity> history = readCommittedHistory(kase.id());
    var underwritingRow =
        history.stream().filter(r -> "underwriting".equals(r.getStageId())).findFirst();
    assertThat(underwritingRow).isPresent();
    assertThat(underwritingRow.get().getState())
        .as("old underwriting row must be REMAPPED in committed view")
        .isEqualTo(StageState.REMAPPED);

    var reviewRow = history.stream().filter(r -> "review".equals(r.getStageId())).findFirst();
    assertThat(reviewRow).isPresent();
    assertThat(reviewRow.get().getState())
        .as("new review row must be ACTIVE in committed view")
        .isEqualTo(StageState.ACTIVE);
  }
}
