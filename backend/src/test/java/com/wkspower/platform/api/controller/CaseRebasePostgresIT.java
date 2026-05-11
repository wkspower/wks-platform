package com.wkspower.platform.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.CaseRebaseService;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Story 3.9 AC5 — Postgres-IT exercising end-to-end rebase: dry-run preview, apply success,
 * irreconcilable rejection, version-arg rejection.
 *
 * <p>Exercises:
 *
 * <ol>
 *   <li>Scenario 1 — dry-run additive (v1 → v2): irreconcilable=[], no DB mutation
 *   <li>Scenario 2 — apply success (v1 → v2): cases.case_type_version mutated to 2
 *   <li>Scenario 3 — irreconcilable rejection (legacyField removed with data): 422 + WKS-CFG-034,
 *       version unchanged
 *   <li>Scenario 4 — reverse rebase: 422 + WKS-API-007
 *   <li>Scenario 5 — non-existent toVersion: 422 + WKS-API-007
 * </ol>
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md}: production-validation disabled.
 * Memory {@code project_postgres_it_parity_gap.md}: Postgres-IT mandatory for BYTEA reads +
 * cases.case_type_version writes.
 */
@SpringBootTest(properties = "wks.bootstrap.production-validation.enabled=false")
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
  @Autowired private CaseRebaseService caseRebaseService;
  @Autowired private CaseRepository caseRepository;
  @Autowired private CaseTypeVersionRegistry versionRegistry;
  @Autowired private CaseEntityRepository caseEntityRepo;
  @Autowired private CaseTypeVersionJpaRepository ctVersionRepo;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager em;

  private static final String CT_PREFIX = "it-rebase-";

  /** Resolved from the seeded admin user at {@code @BeforeEach}. */
  private UUID adminUserId;

  @BeforeEach
  void resolveAdminUser() {
    adminUserId =
        userRepository
            .findByEmail("admin@wkspower.local")
            .orElseThrow(() -> new IllegalStateException("Admin user not seeded"))
            .id();
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

  /**
   * v2 (registry version after bump): mutate-class change — removes legacy-field which has data.
   */
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

  /** v1 with legacy-field — used for irreconcilable scenario. */
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
    return caseRepository.save(kase);
  }

  @AfterEach
  void wipe() {
    caseEntityRepo.deleteAll();
    ctVersionRepo.deleteAll();
  }

  // ---- Scenario 1: dry-run additive — irreconcilable=[], no DB mutation ----

  @Test
  @Transactional
  void scenario1_dryRun_additive_noMutation() {
    String ctId = CT_PREFIX + "s1";

    // Deploy v1
    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s1");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();

    // Deploy v2 (additive — bumpVersion required for v2 registration after v1)
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2Yaml(ctId), "test:s1", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    // Create case at v1
    int v1Num = versionRegistry.currentVersion(ctId).orElseThrow() - 1; // v1 is one before current
    // Re-query: v2 is current so v1 is 1 less
    int currentV = versionRegistry.currentVersion(ctId).orElseThrow();
    assertThat(currentV).isEqualTo(2);

    Case kase = createCase(ctId, 1, Map.of("name", "Alice"));

    // Dry-run v1 → v2
    CaseRebaseReport report = caseRebaseService.dryRun(ctId, kase.id(), 2);

    assertThat(report.fromVersion()).isEqualTo(1);
    assertThat(report.toVersion()).isEqualTo(2);
    assertThat(report.applied()).isFalse();
    assertThat(report.irreconcilable()).isEmpty();

    // No DB mutation — case still at v1
    int actualVersion = readCaseVersion(kase.id());
    assertThat(actualVersion).as("dry-run must NOT mutate cases.case_type_version").isEqualTo(1);
  }

  // ---- Scenario 2: apply success — cases.case_type_version mutated ----

  @Test
  @Transactional
  void scenario2_apply_success_mutatesVersion() {
    String ctId = CT_PREFIX + "s2";

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s2");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2Yaml(ctId), "test:s2", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCase(ctId, 1, Map.of("name", "Bob"));

    // Apply v1 → v2
    CaseRebaseReport report = caseRebaseService.apply(ctId, kase.id(), 2);

    assertThat(report.applied()).isTrue();
    assertThat(report.fromVersion()).isEqualTo(1);
    assertThat(report.toVersion()).isEqualTo(2);
    assertThat(report.irreconcilable()).isEmpty();

    // DB mutated — case now at v2
    em.flush();
    em.clear();
    int actualVersion = readCaseVersion(kase.id());
    assertThat(actualVersion).as("apply must mutate cases.case_type_version to 2").isEqualTo(2);
  }

  // ---- Scenario 3: irreconcilable rejection (legacyField removed with data) ----

  @Test
  @Transactional
  void scenario3_apply_irreconcilable_rejects422_versionUnchanged() {
    String ctId = CT_PREFIX + "s3";

    // Deploy v1 with legacyField
    ValidationResult v1 =
        configService.validateAndRegister("api.yaml", v1WithLegacyFieldYaml(ctId), "test:s3");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();

    // Deploy v2 (legacy-field removed) — needs bumpVersion=true since it's mutate-class.
    // force=false: bumpVersion=true alone is sufficient for the registry gate.
    ValidationResult v2 =
        configService.validateAndRegister(
            "api.yaml", v2RemovesLegacyFieldYaml(ctId), "test:s3", true, false);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    // Create case at v1 with legacy-field data
    Case kase = createCase(ctId, 1, Map.of("name", "Charlie", "legacy-field", "some-data"));

    // After deploying v1 then bumping to v2, registry current is 2
    int targetVersion = versionRegistry.currentVersion(ctId).orElseThrow();
    assertThat(targetVersion).isEqualTo(2);

    // The case has legacyField data but v2 removes legacyField → irreconcilable
    assertThatThrownBy(() -> caseRebaseService.apply(ctId, kase.id(), targetVersion))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors()).hasSize(1);
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_CFG_034.wire());
            });

    // Version unchanged
    int actualVersion = readCaseVersion(kase.id());
    assertThat(actualVersion)
        .as("rejected apply must not mutate cases.case_type_version")
        .isEqualTo(1);
  }

  // ---- Scenario 4: reverse rebase — 422 + WKS-API-007 ----

  @Test
  @Transactional
  void scenario4_reverseRebase_rejects_api007() {
    String ctId = CT_PREFIX + "s4";

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s4");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();
    ValidationResult v2 =
        configService.validateAndRegister("api.yaml", v2Yaml(ctId), "test:s4", true);
    assertThat(v2.isInvalid()).as("v2 deploy: " + v2.errors()).isFalse();

    Case kase = createCase(ctId, 2, Map.of("name", "Dan"));

    assertThatThrownBy(() -> caseRebaseService.dryRun(ctId, kase.id(), 1))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_API_007.wire());
              assertThat(wce.getErrors().get(0).message()).contains("strictly greater");
            });
  }

  // ---- Scenario 5: non-existent toVersion — 422 + WKS-API-007 ----

  @Test
  @Transactional
  void scenario5_nonExistentToVersion_rejects_api007() {
    String ctId = CT_PREFIX + "s5";

    ValidationResult v1 = configService.validateAndRegister("api.yaml", v1Yaml(ctId), "test:s5");
    assertThat(v1.isInvalid()).as("v1 deploy: " + v1.errors()).isFalse();

    Case kase = createCase(ctId, 1, Map.of("name", "Eve"));

    assertThatThrownBy(() -> caseRebaseService.dryRun(ctId, kase.id(), 99))
        .isInstanceOf(WksConfigException.class)
        .satisfies(
            ex -> {
              WksConfigException wce = (WksConfigException) ex;
              assertThat(wce.getErrors().get(0).code()).isEqualTo(ErrorCode.WKS_API_007.wire());
              assertThat(wce.getErrors().get(0).message())
                  .contains("not found in case_type_versions");
            });
  }

  // ---- Helpers ----

  private int readCaseVersion(UUID caseId) {
    return caseEntityRepo
        .findById(caseId)
        .orElseThrow(() -> new AssertionError("case not found: " + caseId))
        .getCaseTypeVersion();
  }
}
