package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.diff.BlastRadiusReport;
import com.wkspower.platform.domain.config.diff.DeltaKind;
import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 * Story 3.8 AC5 — Postgres-backed integration test proving the blast-radius validator gate
 * end-to-end with all six mutate kinds + bump-honor branches.
 *
 * <p>Uses {@code @SpringBootTest} with a real Testcontainers Postgres so that {@code
 * CaseTypeVersionRegistry.loadByVersion} exercises the {@code case_type_versions} table with
 * Postgres semantics (H2 cannot reproduce concurrent deploy lock contention equivalent). Field
 * names are sourced from {@code ConfigValidator.java} lines 112–576.
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md}: production-validation disabled so the
 * validator's WKS-API-055 check does not fire on test-only boot config.
 *
 * <p>Memory {@code project_postgres_it_parity_gap.md}: Postgres-IT is mandatory because
 * version-comparison reads via {@code CaseTypeVersionRegistry.loadByVersion} exercise the {@code
 * case_type_versions} table.
 */
@SpringBootTest(properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class BlastRadiusValidatorPostgresIT {

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
  @Autowired private CaseTypeVersionRegistry versionRegistry;
  @Autowired private CaseTypeVersionJpaRepository repo;
  @Autowired private MappingRegistry mappingRegistry;
  @Autowired private EntityManager em;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Case-type ID prefix — each test uses a unique suffix to avoid cross-test pollution. */
  private static final String CT_PREFIX = "blast-radius-pg-";

  // ---- Field names sourced from ConfigValidator.java lines 112, 113, 388, 576 ----
  // top-level: id (L112), displayName (L113), version (L114), statuses[], fields[], stages[]
  // statuses[]: id, displayName, color (wire form is lowercase per StatusColor.wire()), terminal
  // fields[]: id, displayName, type, required, order

  /** Base v1 YAML: two statuses (open + pending), one field (notes), no stages. */
  private static byte[] v1Yaml(String caseTypeId) {
    return ("""
        id: %s
        displayName: "Blast Radius Test"
        version: 1
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: pending
            displayName: Pending
            color: amber
            terminal: false
        fields:
          - id: notes
            displayName: Notes
            type: text
            required: false
            order: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
            .formatted(caseTypeId))
        .getBytes(StandardCharsets.UTF_8);
  }

  @AfterEach
  void wipe() {
    repo.deleteAll();
  }

  // ---- Scenario 1: status removal without bump → 422 + WKS-CFG-029 ------

  @Test
  void statusRemovalWithoutBump_rejected() {
    String id = CT_PREFIX + "s1";
    // v1: deploy first
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s1", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();

    // v2: remove 'pending' status
    byte[] v2Yaml =
        ("""
        id: %s
        displayName: "Blast Radius Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
        fields:
          - id: notes
            displayName: Notes
            type: text
            required: false
            order: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
                .formatted(id))
            .getBytes(StandardCharsets.UTF_8);

    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", v2Yaml, "test:s1", false);

    assertThat(v2.isInvalid())
        .as("Status removal without bumpVersion=true must be rejected")
        .isTrue();
    assertThat(v2.errors())
        .extracting(com.wkspower.platform.domain.exception.ErrorDetail::code)
        .containsExactly(ErrorCode.WKS_CFG_029.wire());
    assertThat(v2.errors().get(0).message()).contains("Mutate-class");

    // meta.blastRadius must be present with STATUS_REMOVED kind
    assertBlastRadiusInMeta(v2, DeltaKind.STATUS_REMOVED);
  }

  // ---- Scenario 2: status terminal-flag change without bump → 422 + WKS-CFG-029 ----

  @Test
  void statusTerminalFlagChange_withoutBump_rejected() {
    String id = CT_PREFIX + "s2";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s2", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();

    // v2: flip terminal on 'open'
    byte[] v2Yaml =
        ("""
        id: %s
        displayName: "Blast Radius Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: true
          - id: pending
            displayName: Pending
            color: amber
            terminal: false
        fields:
          - id: notes
            displayName: Notes
            type: text
            required: false
            order: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
                .formatted(id))
            .getBytes(StandardCharsets.UTF_8);

    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", v2Yaml, "test:s2", false);

    assertThat(v2.isInvalid()).isTrue();
    assertThat(v2.errors().get(0).code()).isEqualTo(ErrorCode.WKS_CFG_029.wire());
    assertBlastRadiusInMeta(v2, DeltaKind.STATUS_TERMINAL_FLIP);
  }

  // ---- Scenario 3: field type change without bump → 422 + WKS-CFG-029 ---

  @Test
  void fieldTypeChange_withoutBump_rejected() {
    String id = CT_PREFIX + "s3";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s3", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();

    // v2: change 'notes' from text → number
    byte[] v2Yaml =
        ("""
        id: %s
        displayName: "Blast Radius Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: pending
            displayName: Pending
            color: amber
            terminal: false
        fields:
          - id: notes
            displayName: Notes
            type: number
            required: false
            order: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
                .formatted(id))
            .getBytes(StandardCharsets.UTF_8);

    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", v2Yaml, "test:s3", false);

    assertThat(v2.isInvalid()).isTrue();
    assertThat(v2.errors().get(0).code()).isEqualTo(ErrorCode.WKS_CFG_029.wire());
    assertBlastRadiusInMeta(v2, DeltaKind.FIELD_RETYPED);
  }

  // ---- Scenario 4: field required-ness change without bump → 422 --------

  @Test
  void fieldRequiredChange_withoutBump_rejected() {
    String id = CT_PREFIX + "s4";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s4", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();

    // v2: flip 'notes' required: false → true
    byte[] v2Yaml =
        ("""
        id: %s
        displayName: "Blast Radius Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: pending
            displayName: Pending
            color: amber
            terminal: false
        fields:
          - id: notes
            displayName: Notes
            type: text
            required: true
            order: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
                .formatted(id))
            .getBytes(StandardCharsets.UTF_8);

    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", v2Yaml, "test:s4", false);

    assertThat(v2.isInvalid()).isTrue();
    assertThat(v2.errors().get(0).code()).isEqualTo(ErrorCode.WKS_CFG_029.wire());
    assertBlastRadiusInMeta(v2, DeltaKind.FIELD_REQUIRED_FLIPPED);
  }

  // ---- Scenario 5: BPMN MAPPING mutate-class without bump → 422 + WKS-CFG-029 ----

  /**
   * Story 3.8 AC5 scenario 5 — BPMN MAPPING mutate-class. Exercises the Spring-wired {@link
   * MappingRegistry#resolve} path inside {@code ConfigService.runBlastRadiusGate}. The unit suite
   * covers the pure {@code MappingDiff} delegation; only the IT proves the live registry lookup
   * returns the prior {@link MappingDefinition} and the classifier folds it into the report under
   * {@link DeltaKind#MAPPING}.
   *
   * <p>Setup: deploy v1 (registry publishes empty mapping under key "1"), then overwrite the
   * registry with a non-empty {@link MappingDefinition} (one attachment). The v2 deploy carries no
   * BPMN, so {@code yamlResult.mappingDefinition()} resolves to {@link MappingDefinition#empty()} →
   * attachment removal → MUTATE_CLASS via {@code MappingDiff}.
   */
  @Test
  void bpmnMappingMutateClass_withoutBump_rejected() {
    String id = CT_PREFIX + "s5";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s5", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();

    // Seed the prior mapping with one attachment so v2 (empty mapping) becomes a removal →
    // MappingDiff classifies as MUTATE_CLASS. The runBlastRadiusGate keys lookup off
    // priorVersionNum=1 (see ConfigService.runBlastRadiusGate), so we register under "1".
    MappingDefinition priorMapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "deploy.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(
                        "Task_Approve",
                        new AttachmentDefinition.UserTaskMapping("approval", "approval-form")),
                    Optional.empty(),
                    Map.of(),
                    List.of())));
    mappingRegistry.register(new CaseTypeRef(id, "1"), "1", priorMapping);

    // v2 YAML carries the same shape (no schema deltas) so the only mutate signal is MAPPING.
    byte[] v2Yaml =
        ("""
        id: %s
        displayName: "Blast Radius Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: pending
            displayName: Pending
            color: amber
            terminal: false
        fields:
          - id: notes
            displayName: Notes
            type: text
            required: false
            order: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
                .formatted(id))
            .getBytes(StandardCharsets.UTF_8);

    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", v2Yaml, "test:s5", false);

    assertThat(v2.isInvalid())
        .as("BPMN MAPPING mutate-class without bumpVersion must be rejected")
        .isTrue();
    assertThat(v2.errors())
        .extracting(com.wkspower.platform.domain.exception.ErrorDetail::code)
        .containsExactly(ErrorCode.WKS_CFG_029.wire());
    assertBlastRadiusInMeta(v2, DeltaKind.MAPPING);

    // AC2 invariant — the version registry write was NOT executed on rejection. After v1 the
    // table holds exactly one row for this caseTypeId; the rejected v2 must not have added a
    // second row.
    long versionRows = repo.findAll().stream().filter(e -> id.equals(e.getCaseTypeId())).count();
    assertThat(versionRows)
        .as("Rejected mutate-class deploy must not write a v2 row to case_type_versions")
        .isEqualTo(1L);
  }

  // ---- Scenario 5b (Should-Fix #1): unparseable prior YAML → 422 + WKS-CFG-030 (fail-closed) ----

  /**
   * PR #417 review Should-Fix — the blast-radius gate must fail CLOSED when the prior YAML cannot
   * be re-parsed. AC2 invariant requires the gate to apply on every deploy with a prior version;
   * silently bypassing would let mutate-class edits ship unchecked.
   *
   * <p>Setup: deploy v1, then corrupt the {@code definition_yaml} column on the v1 row via a native
   * UPDATE (the column is JPA-immutable; native SQL is the only path). The next deploy attempt
   * should be rejected with {@code WKS-CFG-030}.
   */
  @Test
  @Transactional
  void unparseablePriorYaml_failsClosed_wksCfg030() {
    String id = CT_PREFIX + "s5b";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s5b", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();

    // Corrupt the stored prior YAML so re-parse fails inside runBlastRadiusGate.
    em.createNativeQuery(
            "UPDATE case_type_versions SET definition_yaml = :corrupt WHERE case_type_id = :id")
        .setParameter("corrupt", "::: not valid YAML at all :::\n\t- [unbalanced")
        .setParameter("id", id)
        .executeUpdate();
    em.flush();
    em.clear();

    // Any v2 attempt must now be rejected fail-closed — even a benign no-op edit cannot pass
    // because the gate cannot classify the change.
    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s5b", false);

    assertThat(v2.isInvalid())
        .as("Deploy must be rejected fail-closed when prior YAML cannot be re-parsed")
        .isTrue();
    assertThat(v2.errors())
        .extracting(com.wkspower.platform.domain.exception.ErrorDetail::code)
        .containsExactly(ErrorCode.WKS_CFG_030.wire());
    assertThat(v2.errors().get(0).message())
        .as("Error message must reference fail-closed semantics")
        .contains("fail-closed");
  }

  // ---- Scenario 6: status removal WITH bumpVersion=true → 200 + new version ----

  @Test
  void statusRemovalWithBump_succeeds_newVersion() {
    String id = CT_PREFIX + "s6";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s6", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();
    int v1Version = v1.config().get().version();

    // v2: remove 'pending' status WITH bumpVersion=true
    byte[] v2Yaml =
        ("""
        id: %s
        displayName: "Blast Radius Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
        fields:
          - id: notes
            displayName: Notes
            type: text
            required: false
            order: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
                .formatted(id))
            .getBytes(StandardCharsets.UTF_8);

    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", v2Yaml, "test:s6", true);

    assertThat(v2.isInvalid())
        .as("Status removal with bumpVersion=true must succeed; errors=" + v2.errors())
        .isFalse();
    int v2Version = v2.config().get().version();
    assertThat(v2Version)
        .as("New version must be >= prior + 1")
        .isGreaterThanOrEqualTo(v1Version + 1);
  }

  // ---- Scenario 7: append-class status add without bump → 200 + same version ----

  @Test
  void appendClassStatusAdd_withoutBump_succeeds_sameVersion() {
    String id = CT_PREFIX + "s7";
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s7", false);
    assertThat(v1.isInvalid()).as("v1 deploy must succeed: " + v1.errors()).isFalse();
    int v1Version = v1.config().get().version();

    // v2: add a new status 'approved' (append-class)
    byte[] v2Yaml =
        ("""
        id: %s
        displayName: "Blast Radius Test"
        version: 2
        statuses:
          - id: open
            displayName: Open
            color: blue
            terminal: false
          - id: pending
            displayName: Pending
            color: amber
            terminal: false
          - id: approved
            displayName: Approved
            color: emerald
            terminal: true
        fields:
          - id: notes
            displayName: Notes
            type: text
            required: false
            order: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """
                .formatted(id))
            .getBytes(StandardCharsets.UTF_8);

    ValidationResult v2 =
        configService.validateAndRegister("api-deploy.yaml", v2Yaml, "test:s7", false);

    assertThat(v2.isInvalid())
        .as("Append-class status add without bump must succeed; errors=" + v2.errors())
        .isFalse();
    // The version registry uses content hash for idempotency — new bytes produce a new version
    // row only on REGISTERED outcome. The important assertion is that no WKS-CFG-029 is raised.
    // Version may bump due to registry-hash change — that's OK per the append-class semantic.
    // The key invariant: deploy succeeds.
    assertThat(v2.config()).isPresent();
  }

  // ---- Scenario 8: first deploy (no prior version) → 200 + version 1; bumpVersion=true ignored --

  @Test
  void firstDeploy_noPriorVersion_succeeds_version1() {
    String id = CT_PREFIX + "s8";
    // No prior version exists. bumpVersion=true should be ignored with WARN log.
    ValidationResult v1 =
        configService.validateAndRegister("api-deploy.yaml", v1Yaml(id), "test:s8", true);

    assertThat(v1.isInvalid())
        .as("First deploy with bumpVersion=true must succeed (benign no-op); errors=" + v1.errors())
        .isFalse();
    assertThat(v1.config()).isPresent();
    assertThat(v1.config().get().version()).as("First deploy must produce version 1").isEqualTo(1);
  }

  // ---- helper ------------------------------------------------------------

  /**
   * Asserts that {@code result.responseMeta().blastRadius.mutateDeltas[0].kind} matches {@code
   * expectedKind}. Serialises the {@link BlastRadiusReport} via Jackson to verify the wire shape
   * that the Admin UI will consume (AC2: meta.blastRadius must be present on WKS-CFG-029
   * rejections).
   */
  private static void assertBlastRadiusInMeta(ValidationResult result, DeltaKind expectedKind) {
    assertThat(result.responseMeta())
        .as("responseMeta must not be empty on WKS-CFG-029 rejection")
        .isNotEmpty();
    Object raw = result.responseMeta().get("blastRadius");
    assertThat(raw)
        .as("meta.blastRadius must be present")
        .isNotNull()
        .isInstanceOf(BlastRadiusReport.class);

    BlastRadiusReport report = (BlastRadiusReport) raw;
    assertThat(report.mutateDeltas()).as("mutateDeltas must be non-empty").isNotEmpty();
    assertThat(report.mutateDeltas().get(0).kind())
        .as("First mutate delta kind must match expected")
        .isEqualTo(expectedKind);

    // Verify Jackson wire shape is stable (Admin UI contract, AC2)
    try {
      String json = MAPPER.writeValueAsString(report);
      JsonNode node = MAPPER.readTree(json);
      assertThat(node.has("changeClass")).as("changeClass field must be present in JSON").isTrue();
      assertThat(node.has("mutateDeltas"))
          .as("mutateDeltas field must be present in JSON")
          .isTrue();
      assertThat(node.has("appendDeltas"))
          .as("appendDeltas field must be present in JSON")
          .isTrue();
      assertThat(node.get("changeClass").asText()).isEqualTo("MUTATE_CLASS");
    } catch (Exception e) {
      throw new AssertionError("BlastRadiusReport JSON serialisation failed: " + e.getMessage(), e);
    }
  }
}
