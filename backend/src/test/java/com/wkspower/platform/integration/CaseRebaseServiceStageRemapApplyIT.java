package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.exception.ErrorCode;
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
import org.junit.jupiter.api.io.TempDir;
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

/**
 * Story 3.9.1 AC-4 + AC-5 — H2 integration tests for {@link
 * com.wkspower.platform.domain.service.CaseRebaseService} stage-remap apply path.
 *
 * <p>AC-4: validates that a valid stageRemap atomically bumps the case version, flips {@code
 * currentStageId}, closes the old stage_history row with {@code state = REMAPPED}, and opens a new
 * ACTIVE row. The audit event is emitted via AFTER_COMMIT (tested structurally by the fact that the
 * apply endpoint returns 200 — the rollback-suppression assertion is in {@link
 * RebaseAuditListenerCommitFailureTest}).
 *
 * <p>AC-5: validates that historical stage_history rows referencing removed stages do NOT block
 * rebase — only the case's {@code currentStageId} is checked.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class CaseRebaseServiceStageRemapApplyIT {

  @TempDir static java.nio.file.Path dbDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) {
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("remap-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("wks.case-types.dir", () -> "");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
    reg.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
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
  private TransactionTemplate txTemplate;

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
    ctId = "it-remap-" + UUID.randomUUID().toString().substring(0, 8);
    txTemplate = new TransactionTemplate(txManager);
  }

  @AfterEach
  void wipe() {
    caseEntityRepo.deleteAll();
    ctVersionRepo.deleteAll();
  }

  // ---- YAML helpers ----

  /** v1: stages underwriting + review. */
  private byte[] v1WithStages() {
    return ("""
        id: %s
        displayName: "Remap IT Test"
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

  /** v2: stages review + decision (underwriting removed; review retained). */
  private byte[] v2WithStages() {
    return ("""
        id: %s
        displayName: "Remap IT Test"
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

  private Case createCaseAtStage(String stageId, int ordinal) {
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
    // CaseMapper.toEntity intentionally skips stage cache columns (maintained by WksStageAdvancer
    // via dedicated JPQL UPDATEs). Seed the cache inside a transaction — @Modifying requires one.
    txTemplate.execute(
        status -> {
          caseEntityRepo.updateStageCache(saved.id(), stageId, ordinal);
          return null;
        });
    return caseRepository.findById(saved.id()).orElseThrow();
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

  private String firstErrorCode(String body) throws Exception {
    JsonNode root = json.readTree(body);
    JsonNode errorsArr = root.path("error").path("errors");
    if (errorsArr.isArray() && errorsArr.size() > 0) {
      return errorsArr.get(0).path("code").asText();
    }
    return root.path("error").path("code").asText();
  }

  // ---- AC-4: valid remap → atomic version bump + stage flip + history row states ----

  @Test
  void validRemap_atomic_versionBump_stageFlip_auditAfterCommit() throws Exception {
    String cookie = login();

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1WithStages(), "it:remap");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2WithStages(), "it:remap", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    // Create case at "underwriting" stage (v1 stage that is removed in v2).
    Case kase = createCaseAtStage("underwriting", 0);

    // Seed an ACTIVE stage_history row for "underwriting".
    stageHistoryRepo.save(
        new com.wkspower.platform.infrastructure.persistence.entity.CaseStageHistoryEntity(
            UUID.randomUUID(),
            kase.id(),
            "underwriting",
            0,
            StageState.ACTIVE,
            Instant.now(),
            null,
            "bootstrap",
            null,
            Instant.now()));

    // Apply rebase with stageRemap underwriting→review.
    String body = "{\"stageRemap\":{\"underwriting\":\"review\"}}";
    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 2, body);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode data = json.readTree(resp.getBody()).path("data");
    assertThat(data.path("applied").asBoolean()).isTrue();
    assertThat(data.path("toVersion").asInt()).isEqualTo(2);

    // Assert cases row: version bumped + currentStageId flipped.
    var caseEntity = caseEntityRepo.findById(kase.id()).orElseThrow();
    assertThat(caseEntity.getCaseTypeVersion()).isEqualTo(2);
    assertThat(caseEntity.getCurrentStageId()).isEqualTo("review");

    // Assert stage_history: old "underwriting" row is REMAPPED.
    List<CaseStageHistoryEntity> history =
        stageHistoryRepo.findByCaseIdOrderByOrdinalAsc(kase.id());
    var underwirting =
        history.stream().filter(r -> "underwriting".equals(r.getStageId())).findFirst();
    assertThat(underwirting).isPresent();
    assertThat(underwirting.get().getState()).isEqualTo(StageState.REMAPPED);
    assertThat(underwirting.get().getExitedAt()).isNotNull();

    // Assert stage_history: new "review" row is ACTIVE.
    var reviewRow = history.stream().filter(r -> "review".equals(r.getStageId())).findFirst();
    assertThat(reviewRow).isPresent();
    assertThat(reviewRow.get().getState()).isEqualTo(StageState.ACTIVE);
    assertThat(reviewRow.get().getEnteredAt()).isNotNull();
  }

  // ---- AC-5: partial remap — current remapped, historical orphan does NOT block ----

  @Test
  void partialRemap_currentRemapped_historyUnaffected() throws Exception {
    String cookie = login();

    ValidationResult v1 =
        configService.validateAndRegister("api.yaml", v1WithStages(), "it:remap5");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2WithStages(), "it:remap5", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    // Case currently at "underwriting" (removed in v2), and has a COMPLETED "appeals" history row
    // (stage that also doesn't exist in v2 — historical orphan).
    Case kase = createCaseAtStage("underwriting", 0);

    // Seed ACTIVE stage_history row for "underwriting".
    stageHistoryRepo.save(
        new com.wkspower.platform.infrastructure.persistence.entity.CaseStageHistoryEntity(
            UUID.randomUUID(),
            kase.id(),
            "underwriting",
            0,
            StageState.ACTIVE,
            Instant.now(),
            null,
            "bootstrap",
            null,
            Instant.now()));

    // Seed COMPLETED "appeals" row (orphan history — appeals never existed in either v1 or v2).
    stageHistoryRepo.save(
        new com.wkspower.platform.infrastructure.persistence.entity.CaseStageHistoryEntity(
            UUID.randomUUID(),
            kase.id(),
            "appeals",
            99,
            StageState.COMPLETED,
            Instant.now().minusSeconds(3600),
            Instant.now().minusSeconds(1800),
            "old-process",
            null,
            Instant.now()));

    // stageRemap covers only "underwriting" → "review"; "appeals" is historical-only, NOT blocked.
    String body = "{\"stageRemap\":{\"underwriting\":\"review\"}}";
    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 2, body);

    // AC-5: apply SUCCEEDS — historical "appeals" reference does NOT block rebase.
    assertThat(resp.getStatusCode())
        .as("historical orphan stage must not block rebase: " + resp.getBody())
        .isEqualTo(HttpStatus.OK);

    JsonNode data = json.readTree(resp.getBody()).path("data");
    assertThat(data.path("applied").asBoolean()).isTrue();

    // "appeals" history row is still COMPLETED — untouched by remap.
    List<CaseStageHistoryEntity> history =
        stageHistoryRepo.findByCaseIdOrderByOrdinalAsc(kase.id());
    var appealsRow = history.stream().filter(r -> "appeals".equals(r.getStageId())).findFirst();
    assertThat(appealsRow).isPresent();
    assertThat(appealsRow.get().getState()).isEqualTo(StageState.COMPLETED);
  }

  // ---- AC-2 wire: WKS-CFG-036 for bad from-key ----

  @Test
  void badFromKey_returns422_WKS_CFG_036() throws Exception {
    String cookie = login();

    ValidationResult v1 =
        configService.validateAndRegister("api.yaml", v1WithStages(), "it:remap6");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2WithStages(), "it:remap6", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCaseAtStage("underwriting", 0);

    // "bad-stage" not in v1 stages.
    String body = "{\"stageRemap\":{\"bad-stage\":\"review\"}}";
    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 2, body);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(firstErrorCode(resp.getBody())).isEqualTo(ErrorCode.WKS_CFG_036.wire());

    // No mutation.
    var caseEntity = caseEntityRepo.findById(kase.id()).orElseThrow();
    assertThat(caseEntity.getCaseTypeVersion()).isEqualTo(1);
    assertThat(caseEntity.getCurrentStageId()).isEqualTo("underwriting");
  }

  // ---- AC-3 wire: WKS-CFG-037 for bad to-value ----

  @Test
  void badToValue_returns422_WKS_CFG_037() throws Exception {
    String cookie = login();

    ValidationResult v1 =
        configService.validateAndRegister("api.yaml", v1WithStages(), "it:remap7");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2WithStages(), "it:remap7", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCaseAtStage("underwriting", 0);

    // "bad-target" not in v2 stages.
    String body = "{\"stageRemap\":{\"underwriting\":\"bad-target\"}}";
    ResponseEntity<String> resp = rebaseApply(cookie, ctId, kase.id(), 2, body);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(firstErrorCode(resp.getBody())).isEqualTo(ErrorCode.WKS_CFG_037.wire());

    // No mutation.
    var caseEntity = caseEntityRepo.findById(kase.id()).orElseThrow();
    assertThat(caseEntity.getCaseTypeVersion()).isEqualTo(1);
  }
}
