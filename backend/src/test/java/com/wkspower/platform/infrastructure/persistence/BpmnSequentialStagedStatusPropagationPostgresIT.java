package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.OutcomeMapping;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.domain.service.BpmnSequentialStagedStatusPropagationIT;
import com.wkspower.platform.domain.service.ExecutionSignalRouter;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Story 6.2 AC7 — Postgres-IT parity for gap-10 fix (b): {@code resetStatusForAdvancedStage}
 * propagates the next stage's {@code initialStatus} into {@code case.status} on a real Postgres
 * instance.
 *
 * <p>Per Sprint 9 CF#2 enforcement: any new IT touching persistence MUST ship a Postgres-IT variant
 * in the same PR. This test discharges that requirement for the gap-10 fix-b surface ({@code
 * statusUpdater.updateStatus} within the stage-advance transaction).
 *
 * <p>Covers the same lifecycle walk as {@link BpmnSequentialStagedStatusPropagationIT} but on a
 * real Postgres 16 container:
 *
 * <ol>
 *   <li>Create case → assert {@code case.status = "drafting"} (AC6 gap-10 fix-a).
 *   <li>Dispatch OUTCOME signal → stage advances → assert {@code case.status = "in-review"} (AC7).
 *   <li>Dispatch second OUTCOME signal → stage advances → assert {@code case.status = "approved"}.
 * </ol>
 *
 * <p>Skipped automatically when Docker is unavailable.
 *
 * <p>Slice test: invokes ExecutionSignalRouter.onSignal directly under TransactionTemplate against
 * the production PlatformTransactionManager. This mirrors the CIB seven
 * SpringTransactionInterceptor txn boundary that CaseStatusAdapter.updateStatus
 * (Propagation.MANDATORY) joins in production. Known divergence: the engine's ACT_* tables are not
 * exercised — a router that swallowed a rollback signal without re-throwing would not be caught
 * here. Current router propagates WksMappingMissException so the divergence is theoretical.
 */
@SpringBootTest
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class BpmnSequentialStagedStatusPropagationPostgresIT {

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
    // ProductionBootstrapValidator opt-out — this test does not exercise the validator.
    registry.add("wks.bootstrap.production-validation.enabled", () -> "false");
    registry.add(
        "camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
  }

  @Autowired private CaseRepository caseRepository;
  @Autowired private CaseStatusUpdater caseStatusUpdater;
  @Autowired private StageRepository stageRepository;
  @Autowired private ExecutionSignalRouter backendSignalRouter;
  @Autowired private MappingRegistry mappingRegistry;
  @Autowired private CaseTypeRegistry caseTypeRegistry;
  @Autowired private PlatformTransactionManager transactionManager;

  /**
   * Wraps {@code onSignal} dispatches in a Spring transaction so that {@code
   * CaseStatusAdapter.updateStatus} (Propagation.MANDATORY) can join an active transaction. In
   * production the containing transaction is opened by {@code CaseStatusListener} during BPMN
   * engine execution; this template mirrors that boundary for the IT.
   */
  private void inTx(Runnable work) {
    new TransactionTemplate(transactionManager).executeWithoutResult(s -> work.run());
  }

  private static final Instant NOW = Instant.parse("2026-05-11T10:00:00Z");
  // Must match BpmnSequentialStagedStatusPropagationIT.CASE_TYPE_ID — buildCaseTypeConfig()
  // bakes that id into the returned CaseTypeConfig, and the router resolves status-reset via
  // caseTypeReader.findVersion(caseTypeId, version). A mismatch silently logs and skips the
  // status reset, leaving the case at its initial status. Each Postgres-IT runs in its own
  // Spring context so there is no in-memory CaseTypeRegistry contention with the H2 sibling.
  private static final String CASE_TYPE_ID = "bpmn-seq-staged-it";
  private static final String VERSION = "1";

  /**
   * Full lifecycle walk on real Postgres: create → drafting → approve → in-review → approve →
   * approved.
   *
   * <p>Discharges Postgres-IT parity for gap-10 fix-b (Story 6.2 AC7).
   */
  @Test
  void statusTracksStageInitialStatusOnRealPostgres() {
    UUID actorId = bootstrapActorId();
    UUID caseId = UUID.randomUUID();

    // Create case with gap-10 fix-a initial status = "drafting".
    Case toSave =
        new Case(
            caseId,
            CASE_TYPE_ID,
            1,
            "drafting", // gap-10 fix-a: first stage's initialStatus
            actorId,
            Map.of(),
            null,
            NOW,
            actorId,
            NOW,
            0L);
    caseRepository.save(toSave);
    stageRepository.materialiseStages(caseId, BpmnSequentialStagedStatusPropagationIT.STAGES, NOW);
    stageRepository.appendTransition(
        new StageRepository.Transition(
            caseId, null, null, "intake", 0, List.of(), "wks-auto-rule", "case-create", NOW));

    // Register the staged CaseTypeConfig with the real CaseTypeReader so that
    // resetStatusForAdvancedStage can resolve the next stage's initialStatus.
    caseTypeRegistry.register(BpmnSequentialStagedStatusPropagationIT.buildCaseTypeConfig());

    // Assert AC6: initial status is "drafting" on real Postgres.
    Case created = caseRepository.findById(caseId).orElseThrow();
    assertThat(created.status())
        .as(
            "AC6 gap-10 fix-a on Postgres: case.status should be 'drafting' from intake stage initialStatus")
        .isEqualTo("drafting");
    assertThat(created.currentStageId()).isEqualTo("intake");

    // Register multi-outcome mapping for intake → review transition.
    CaseTypeRef caseTypeRef = new CaseTypeRef(CASE_TYPE_ID, VERSION);
    MappingDefinition mapping1 =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    CASE_TYPE_ID + ".bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of("approve", new OutcomeMapping("intake -> review")))));
    mappingRegistry.register(caseTypeRef, VERSION, mapping1);

    // Dispatch OUTCOME signal: approve on intake stage → advance to review.
    inTx(
        () ->
            backendSignalRouter.onSignal(
                new ExecutionSignal(
                    ExecutionSignalKind.OUTCOME,
                    "formOutcome",
                    new com.wkspower.platform.domain.port.CaseInstanceRef(caseId, caseTypeRef),
                    "intake-triage",
                    Map.of("outcome", "approve"))));

    Case afterFirstAdvance = caseRepository.findById(caseId).orElseThrow();
    assertThat(afterFirstAdvance.currentStageId())
        .as("stage should advance from intake to review on Postgres")
        .isEqualTo("review");
    assertThat(afterFirstAdvance.status())
        .as("AC7 gap-10 fix-b on Postgres: case.status = 'in-review' after intake → review advance")
        .isEqualTo("in-review");

    // Dispatch OUTCOME signal: approve on review stage → advance to decision.
    MappingDefinition mapping2 =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    CASE_TYPE_ID + ".bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of("approve", new OutcomeMapping("review -> decision")))));
    mappingRegistry.register(caseTypeRef, VERSION, mapping2);

    inTx(
        () ->
            backendSignalRouter.onSignal(
                new ExecutionSignal(
                    ExecutionSignalKind.OUTCOME,
                    "formOutcome",
                    new com.wkspower.platform.domain.port.CaseInstanceRef(caseId, caseTypeRef),
                    "review-assess",
                    Map.of("outcome", "approve"))));

    Case afterSecondAdvance = caseRepository.findById(caseId).orElseThrow();
    assertThat(afterSecondAdvance.currentStageId())
        .as("stage should advance from review to decision on Postgres")
        .isEqualTo("decision");
    assertThat(afterSecondAdvance.status())
        .as(
            "AC7 gap-10 fix-b on Postgres: case.status = 'approved' after review → decision advance"
                + " (JOURNEYS F4 step 6)")
        .isEqualTo("approved");
  }

  @Autowired private javax.sql.DataSource dataSource;

  private UUID bootstrapActorId() {
    try (var conn = dataSource.getConnection();
        var stmt = conn.createStatement();
        var rs = stmt.executeQuery("SELECT id FROM users LIMIT 1")) {
      assertThat(rs.next()).as("at least one user must exist for created_by FK").isTrue();
      return UUID.fromString(rs.getString(1));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
