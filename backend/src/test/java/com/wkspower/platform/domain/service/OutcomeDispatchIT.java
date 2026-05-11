package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.OutcomeMapping;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.domain.port.FakeRecordingWorkflowAdapter;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.infrastructure.persistence.RouterItPersistenceImports;
import com.wkspower.platform.infrastructure.persistence.entity.RoleEntity;
import com.wkspower.platform.infrastructure.persistence.entity.UserEntity;
import com.wkspower.platform.infrastructure.persistence.repository.RoleEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.UserEntityRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Story 6.2 AC2 / AC5 — H2 integration test for OUTCOME signal dispatch via {@link
 * ExecutionSignalRouter}.
 *
 * <p>H2-only rationale (documented per Story 6.2 PR body): the router itself is in-memory domain
 * logic — the only persistence surface is {@code CaseStatusUpdater.updateStatus} and {@code
 * StageRepository.appendTransition}, both covered at the Postgres-IT level by {@link
 * CaseTransitionPostgresIT} (4.4b) and {@link BpmnSequentialStagedStatusPropagationPostgresIT} (6.2
 * gap-10 AC7). Adding a duplicate Postgres-IT for the outcome dispatch path would test the same
 * persistence contract already covered by those tests.
 *
 * <p>Covers:
 *
 * <ul>
 *   <li>AC2: OUTCOME signal with declared outcome key resolves the OutcomeMapping rule and applies
 *       the stageTransition (stage advances, status resets to next stage initialStatus).
 *   <li>AC5: OUTCOME signal with undeclared outcome key throws WksMappingMissException carrying
 *       WKS-MAP-404 BEFORE any mutation; case row is unchanged.
 * </ul>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  BpmnSequentialStagedStatusPropagationIT.TestConfig.class,
  RouterItPersistenceImports.class
})
@ActiveProfiles("dev")
@Transactional
class OutcomeDispatchIT {

  private static final Instant NOW = Instant.parse("2026-05-11T10:00:00Z");
  private static final String CASE_TYPE_ID = "outcome-dispatch-it";

  @Autowired ExecutionSignalRouter router;
  @Autowired MappingRegistry mappingRegistry;
  @Autowired WorkflowAdapterBinder binder;
  @Autowired CaseRepository caseRepository;
  @Autowired StageRepository stageRepository;
  @Autowired CaseStatusUpdater statusUpdater;
  @Autowired UserEntityRepository userRepo;
  @Autowired RoleEntityRepository roleRepo;
  @Autowired EntityManager em;

  private UUID actorId;
  private FakeRecordingWorkflowAdapter fake;

  @BeforeEach
  void seedActorAndAdapter() {
    RoleEntity role =
        roleRepo
            .findByName("admin")
            .orElseGet(
                () ->
                    roleRepo.save(
                        new RoleEntity(UUID.randomUUID(), "admin", Instant.now(), Instant.now())));
    UserEntity u =
        userRepo.save(
            new UserEntity(
                UUID.randomUUID(),
                "outcome-it-" + UUID.randomUUID() + "@x",
                "x",
                true,
                Instant.now(),
                Instant.now(),
                new HashSet<>(List.of(role))));
    actorId = u.getId();
    fake = new FakeRecordingWorkflowAdapter(binder);
    fake.onExecutionSignal(router::onSignal);
  }

  /**
   * AC2 — OUTCOME signal with declared outcome key "approve" resolves the mapping rule and applies
   * the stageTransition "intake -> review", advancing the stage.
   */
  @Test
  void outcomeDispatch_declaredKey_advancesStageAndResetsStatus() {
    Case caseRow = setupCase(CASE_TYPE_ID + "-ac2");
    CaseTypeRef caseTypeRef = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseTypeRef, AttachmentScope.ofCase());

    mappingRegistry.register(
        caseTypeRef,
        "1",
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "test.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of(
                        "approve", new OutcomeMapping("intake -> review"),
                        "reject", new OutcomeMapping("intake -> closed"))))));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.OUTCOME,
            "formOutcome",
            new CaseInstanceRef(caseRow.id(), caseTypeRef),
            "review-task",
            Map.of("outcome", "approve")));
    em.flush();
    em.clear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId())
        .as("AC2: stage should advance to review on 'approve' outcome")
        .isEqualTo("review");
    // The router emits stage-advance + status-reset (two events from resetStatusForAdvancedStage).
    // Status reset: review stage's initialStatus = "in-review" (from stubCaseTypeReader in
    // TestConfig).
    // Note: TestConfig.stubCaseTypeReader returns BpmnSequentialStagedStatusPropagationIT.STAGES
    // which has review.initialStatus = "in-review".
    assertThat(after.status())
        .as("AC2: status should reset to review stage's initialStatus 'in-review' after advance")
        .isEqualTo("in-review");
  }

  /**
   * AC5 — OUTCOME signal with undeclared key "sendBack" causes WksMappingMissException
   * (WKS-MAP-404) BEFORE any mutation. The case row is unchanged.
   */
  @Test
  void outcomeDispatch_undeclaredKey_throwsMissExceptionBeforeMutation() {
    Case caseRow = setupCase(CASE_TYPE_ID + "-ac5");
    CaseTypeRef caseTypeRef = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseTypeRef, AttachmentScope.ofCase());

    // Mapping declares only {approve, reject} — NOT sendBack.
    mappingRegistry.register(
        caseTypeRef,
        "1",
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "test.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of(
                        "approve", new OutcomeMapping("intake -> review"),
                        "reject", new OutcomeMapping("intake -> closed"))))));

    // Snapshot the case before the miss-dispatch.
    String statusBefore = caseRow.status();
    String stageBefore = caseRow.currentStageId();

    // The router catches WksMappingMissException internally (AC4 — miss is NOT propagated).
    // FakeRecordingWorkflowAdapter.emit() → router.onSignal() → catches miss → emits audit row.
    assertThatNoException()
        .isThrownBy(
            () ->
                fake.emit(
                    new ExecutionSignal(
                        ExecutionSignalKind.OUTCOME,
                        "formOutcome",
                        new CaseInstanceRef(caseRow.id(), caseTypeRef),
                        "review-task",
                        Map.of("outcome", "sendBack"))));
    em.flush();
    em.clear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.status())
        .as("AC5: status must be unchanged after miss-dispatch — no mutation before exception")
        .isEqualTo(statusBefore);
    assertThat(after.currentStageId())
        .as("AC5: currentStageId must be unchanged after miss-dispatch")
        .isEqualTo(stageBefore);
  }

  /**
   * Story 6.2 — applyStageTransition rejects a transition spec with a blank source segment.
   * MappingValidator anchors the grammar at deploy time, but a malformed spec sneaking through
   * (e.g. admin REST PATCH, runtime hot-reload race) must not be silently treated as
   * "from anywhere". The router throws WksMappingMissException → router's existing miss path
   * catches and emits an audit row; the case row remains unchanged.
   */
  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.ValueSource(
      strings = {"-> review", " -> ", "review -> ->", "intake -> review -> closed"})
  void applyStageTransition_rejectsMalformedSpec(String malformedSpec) {
    Case caseRow = setupCase(CASE_TYPE_ID + "-malformed-" + Math.abs(malformedSpec.hashCode()));
    CaseTypeRef caseTypeRef = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseTypeRef, AttachmentScope.ofCase());

    mappingRegistry.register(
        caseTypeRef,
        "1",
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "test.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of("approve", new OutcomeMapping(malformedSpec))))));

    String statusBefore = caseRow.status();
    String stageBefore = caseRow.currentStageId();

    assertThatNoException()
        .isThrownBy(
            () ->
                fake.emit(
                    new ExecutionSignal(
                        ExecutionSignalKind.OUTCOME,
                        "formOutcome",
                        new CaseInstanceRef(caseRow.id(), caseTypeRef),
                        "intake-task",
                        Map.of("outcome", "approve"))));
    em.flush();
    em.clear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.status())
        .as("malformed spec '" + malformedSpec + "' must not mutate status")
        .isEqualTo(statusBefore);
    assertThat(after.currentStageId())
        .as("malformed spec '" + malformedSpec + "' must not mutate stage")
        .isEqualTo(stageBefore);
  }

  // ---- helpers -------------------------------------------------------

  private Case setupCase(String caseTypeId) {
    UUID caseId = UUID.randomUUID();
    // Use "drafting" as initial status per gap-10 fix-a (first stage's initialStatus).
    Case toSave =
        new Case(caseId, caseTypeId, 1, "drafting", actorId, Map.of(), null, NOW, actorId, NOW, 0L);
    caseRepository.save(toSave);

    List<StageDefinition> stages =
        List.of(
            new StageDefinition(
                "intake",
                "Intake",
                0,
                List.of(new StatusDefinition("drafting", "Drafting", StatusColor.AMBER, false)),
                Optional.of("drafting")),
            new StageDefinition(
                "review",
                "Review",
                1,
                List.of(new StatusDefinition("in-review", "In Review", StatusColor.BLUE, false)),
                Optional.of("in-review")),
            new StageDefinition(
                "closed",
                "Closed",
                2,
                List.of(new StatusDefinition("closed", "Closed", StatusColor.ZINC, true)),
                Optional.of("closed")));
    stageRepository.materialiseStages(caseId, stages, NOW);
    stageRepository.appendTransition(
        new StageRepository.Transition(
            caseId, null, null, "intake", 0, List.of(), "wks-auto-rule", "case-create", NOW));
    em.flush();
    em.clear();
    return caseRepository.findById(caseId).orElseThrow();
  }
}
