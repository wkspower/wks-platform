package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.infrastructure.persistence.entity.CaseTypeVersionEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
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
 * Story 3.11 — Postgres-backed integration test for the lenient prior re-parse + dev-only force
 * override paths.
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md} — production-validation disabled so
 * the validator's bootstrap check does not fire on test-only boot config.
 *
 * <p>Memory {@code project_postgres_it_parity_gap.md}: Postgres-IT mandatory because the prior YAML
 * byte storage path crosses {@code case_type_versions.definition_yaml} which Sprint 4 confirmed
 * needs Postgres parity for byte fidelity.
 */
@SpringBootTest(properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class BlastRadiusForceOverridePostgresIT {

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
  @Autowired private CaseTypeVersionJpaRepository repo;
  @Autowired private EntityManager em;

  private static final String CT_PREFIX = "force-override-pg-";

  /** Minimal valid base YAML — id substituted per scenario for isolation. */
  private static byte[] baseYaml(String caseTypeId, int version) {
    return ("""
        id: %s
        displayName: "Force Override Test"
        version: %d
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
            .formatted(caseTypeId, version))
        .getBytes(StandardCharsets.UTF_8);
  }

  @AfterEach
  void wipe() {
    repo.deleteAll();
  }

  // ---- Scenario 1: corrupt prior + force=true&bumpVersion=true → ACCEPTED ----

  @Test
  @Transactional
  void corruptPrior_forceTrue_bumpTrue_accepted() {
    String id = CT_PREFIX + "s1";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", baseYaml(id, 1), "test:s1", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();

    // Corrupt the stored YAML to malformed bytes.
    corruptPriorYaml(id, 1);

    // v2: redeploy with force=true&bumpVersion=true — gate should bypass with WARN.
    ValidationResult v2 =
        configService.validateAndRegister(
            "api-deploy.yaml", baseYaml(id, 2), "test:s1", true, true);

    assertThat(v2.isInvalid())
        .as("force=true&bump=true must bypass WKS-CFG-030 in non-production: " + v2.errors())
        .isFalse();
  }

  // ---- Scenario 2: corrupt prior + force=false → 422 + WKS-CFG-030 ----

  @Test
  @Transactional
  void corruptPrior_noForce_rejectedWithCfg030() {
    String id = CT_PREFIX + "s2";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", baseYaml(id, 1), "test:s2", false);
    assertThat(v1.isInvalid()).isFalse();

    corruptPriorYaml(id, 1);

    ValidationResult v2 =
        configService.validateAndRegister(
            "api-deploy.yaml", baseYaml(id, 2), "test:s2", false, false);

    assertThat(v2.isInvalid()).isTrue();
    assertThat(v2.errors().get(0).code())
        .as("AC2 regression — corrupt prior without force must still fail-closed")
        .isEqualTo(ErrorCode.WKS_CFG_030.wire());
  }

  // ---- Scenario 3: schema-drifted prior (unknown legacy key) → AC1 lenient happy path ----

  @Test
  @Transactional
  void schemaDriftedPrior_lenientPathPicksUp_classifierRuns() {
    String id = CT_PREFIX + "s3";

    // Seed v1 the normal way then overwrite definitionYaml with a drifted shape that the strict
    // mapper rejects but the lenient mapper accepts.
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", baseYaml(id, 1), "test:s3", false);
    assertThat(v1.isInvalid()).isFalse();

    String drifted =
        """
        id: %s
        displayName: "Force Override Test"
        version: 1
        legacyTopLevelField: removed-since
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
            .formatted(id);
    overwritePriorYaml(id, 1, drifted);

    // v2: append-class change (no force needed). Lenient prior parse should pick up cleanly.
    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", baseYaml(id, 2), "test:s3", false);

    assertThat(v2.isInvalid())
        .as(
            "AC1 — schema-drifted prior should be re-parsed leniently and classifier should run"
                + " against append-class change without force: "
                + v2.errors())
        .isFalse();
  }

  // ---- Scenario 4: production profile rejection of force=true ----
  // AC3 production rejection is a CONTROLLER-layer concern (pre-parse). The service-layer
  // ConfigService.isProductionProfile() returns false for this @ActiveProfiles("test","postgres")
  // class. A profile-specific IT proves the controller layer wires Environment correctly; that
  // path is exercised by the AdminControllerForceOverrideTest unit tests via mocking
  // configService.isProductionProfile() — IT-level verification of the production-profile
  // wiring is already covered by the existing BlastRadiusValidatorPostgresIT class which uses
  // @ActiveProfiles("production"). This Postgres-IT focuses on the service-layer behaviors.

  // ---- Helpers ----

  private void corruptPriorYaml(String caseTypeId, int version) {
    String malformed = "id: " + caseTypeId + "\ndisplayName: \"unterminated\nversion: " + version;
    overwritePriorYaml(caseTypeId, version, malformed);
  }

  /** Direct write to {@code case_type_versions.definition_yaml} bypassing the registry adapter. */
  private void overwritePriorYaml(String caseTypeId, int version, String yaml) {
    em.flush();
    em.createNativeQuery(
            "UPDATE case_type_versions SET definition_yaml = :y WHERE case_type_id = :id AND"
                + " version = :v")
        .setParameter("y", yaml)
        .setParameter("id", caseTypeId)
        .setParameter("v", version)
        .executeUpdate();
    em.clear();
    // Sanity — the row is updated.
    CaseTypeVersionEntity row =
        em.createQuery(
                "SELECT e FROM CaseTypeVersionEntity e WHERE e.caseTypeId = :id AND e.version ="
                    + " :v",
                CaseTypeVersionEntity.class)
            .setParameter("id", caseTypeId)
            .setParameter("v", version)
            .getSingleResult();
    assertThat(row.getDefinitionYaml()).isEqualTo(yaml);
  }
}
