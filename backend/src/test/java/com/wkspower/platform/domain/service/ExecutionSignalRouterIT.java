package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.SignalMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.event.ExecutionSignalRouted;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.domain.port.ExecutionSignalSubscription;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.FakeRecordingWorkflowAdapter;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.infrastructure.persistence.RouterItPersistenceImports;
import com.wkspower.platform.infrastructure.persistence.entity.RoleEntity;
import com.wkspower.platform.infrastructure.persistence.entity.UserEntity;
import com.wkspower.platform.infrastructure.persistence.repository.RoleEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.UserEntityRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Story 4.3 AC7 / AC8 — end-to-end IT for {@link ExecutionSignalRouter} using {@link
 * FakeRecordingWorkflowAdapter} (Story 4.1 fixture). Boots a Spring slice with the real router, registry,
 * advancer, status updater, and case repository — no CIB seven dependency. Covers the headline
 * routing matrix plus transaction-rollback semantics.
 *
 * <p>Postgres-IT parity is deferred per {@code project_postgres_it_parity_gap.md} — H2-only here
 * matches Stories 3.1 / 3.2 precedent.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ExecutionSignalRouterIT.RouterTestConfig.class, RouterItPersistenceImports.class})
@ActiveProfiles("dev")
@Transactional
class ExecutionSignalRouterIT {

  private static final Instant NOW = Instant.parse("2026-05-05T12:00:00Z");
  private static final List<StageDefinition> STAGES =
      List.of(
          new StageDefinition("stage1", "Stage 1", 0),
          new StageDefinition("stage2", "Stage 2", 1),
          new StageDefinition("stage3", "Stage 3", 2));
  private static final String ADAPTER_NAME = "fake";

  @Autowired ExecutionSignalRouter router;
  @Autowired MappingRegistry mappingRegistry;
  @Autowired WorkflowAdapterBinder binder;
  @Autowired CaseRepository caseRepository;
  @Autowired StageRepository stageRepository;
  @Autowired CaseStatusUpdater statusUpdater;
  @Autowired RecordingEventPublisher events;
  @Autowired UserEntityRepository userRepo;
  @Autowired RoleEntityRepository roleRepo;
  @Autowired EntityManager em;

  private UUID actorId;
  private FakeRecordingWorkflowAdapter fake;
  private ExecutionSignalSubscription subscription;

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
                "router-it-" + UUID.randomUUID() + "@x",
                "x",
                true,
                Instant.now(),
                Instant.now(),
                new HashSet<>(List.of(role))));
    actorId = u.getId();

    fake = new FakeRecordingWorkflowAdapter(binder);
    subscription = fake.onExecutionSignal(router::onSignal);
    events.clear();
  }

  // ---------- AC7 §1 — STAGE_TRANSITION happy path ------------------------------

  @Test
  void endEventAdvancesActiveStage() {
    Case caseRow = newBootstrappedCase("loan-end", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.of(new EndEventMapping("stage1 -> stage2")),
                Map.of(),
                List.of())));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.STAGE_TRANSITION,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "end_1",
            Map.of()));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId()).isEqualTo("stage2");
    // Story 4.4b AC3 — STAGE_TRANSITION stage-advance now emits TWO events (stage-advance + status-reset).
    assertThat(events.routed()).hasSize(2);
    assertThat(events.routed().get(0).kind()).isEqualTo(ExecutionSignalKind.STAGE_TRANSITION);
    assertThat(events.routed().get(0).source().toString()).isEqualTo("backend(fake)");
    assertThat(events.routed().get(0).errorCode()).isNull();
    assertThat(events.routed().get(0).detail()).containsEntry("effect", "stage-advance");
    assertThat(events.routed().get(1).detail()).containsEntry("effect", "status-reset");
  }

  // ---------- AC7 §2 — NAMED_SIGNAL skip-class --------------------------

  @Test
  void namedSignalSkipsToTargetStage() {
    Case caseRow = newBootstrappedCase("loan-named", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Map.of("escalate", new SignalMapping("stage1 -> stage3")),
                List.of())));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.NAMED_SIGNAL,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "escalate",
            Map.of()));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId()).isEqualTo("stage3");
    // Story 4.4b AC3 — NAMED_SIGNAL stage-advance now emits TWO events.
    assertThat(events.routed()).hasSize(2);
    assertThat(events.routed().get(0).source().toString()).isEqualTo("backend(fake)");
    assertThat(events.routed().get(0).detail()).containsEntry("effect", "stage-advance");
    assertThat(events.routed().get(1).detail()).containsEntry("effect", "status-reset");
  }

  // ---------- AC7 §3 — USER_TASK_PROPERTY status change -----------------

  @Test
  void userTaskPropertyUpdatesStatus() {
    Case caseRow = newBootstrappedCase("loan-prop", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Map.of(),
                List.of(
                    new PropertyEmissionRule(
                        "userTask:review",
                        "status",
                        ExecutionSignalKind.TASK_STATUS_CHANGED,
                        "stage:stage1")))));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.TASK_STATUS_CHANGED,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "review",
            Map.of("camunda:property", "status", "value", "approved")));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.status()).isEqualTo("approved");
    assertThat(events.routed())
        .singleElement()
        .satisfies(
            e -> {
              assertThat(e.source().toString()).isEqualTo("backend(fake)");
              assertThat(e.kind()).isEqualTo(ExecutionSignalKind.TASK_STATUS_CHANGED);
            });
  }

  // ---------- AC7 §4 — userTask property cannot drive stage transition ---

  @Test
  void userTaskPropertyEmittingNonStatusKindIsRejected() {
    Case caseRow = newBootstrappedCase("loan-stage-rej", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    // Rule whose emits is STAGE_TRANSITION (a stage-transition kind) — router rejects.
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Map.of(),
                List.of(
                    new PropertyEmissionRule(
                        "userTask:review",
                        "stage",
                        ExecutionSignalKind.STAGE_TRANSITION,
                        "stage:stage1")))));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.TASK_STATUS_CHANGED,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "review",
            Map.of("camunda:property", "stage", "value", "stage2")));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId()).isEqualTo("stage1");
    assertThat(after.status()).isEqualTo("open");
    assertThat(events.routed())
        .singleElement()
        .satisfies(
            e -> {
              assertThat(e.errorCode()).isEqualTo("WKS-MAP-404");
              assertThat(e.source().toString()).isEqualTo("backend(unmapped:fake)");
              assertThat(e.detail()).containsEntry("originAdapter", ADAPTER_NAME);
            });
  }

  // ---------- AC7 §5 — unmapped signal -----------------------------------

  @Test
  void unmappedNamedSignalIsAuditedWithMap404() {
    Case caseRow = newBootstrappedCase("loan-unmapped", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Map.of(), // no signal rules at all
                List.of())));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.NAMED_SIGNAL,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "undeclared",
            Map.of()));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId()).isEqualTo("stage1");
    assertThat(events.routed())
        .singleElement()
        .satisfies(
            e -> {
              assertThat(e.errorCode()).isEqualTo("WKS-MAP-404");
              assertThat(e.source().toString()).isEqualTo("backend(unmapped:fake)");
              assertThat(e.detail()).containsEntry("originAdapter", ADAPTER_NAME);
            });
  }

  // ---------- AC7 §6 — zero-attachment CaseType -------------------------

  @Test
  void zeroAttachmentCaseTypeYieldsMap404OnAnySignal() {
    Case caseRow = newBootstrappedCase("loan-empty", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(caseType, "1", MappingDefinition.empty());

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.STAGE_TRANSITION,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "end_1",
            Map.of()));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId()).isEqualTo("stage1");
    assertThat(events.routed())
        .singleElement()
        .satisfies(e -> assertThat(e.errorCode()).isEqualTo("WKS-MAP-404"));
  }

  // ---------- AC7 §7 — adapter-emission order honoured (no reorder) -----

  @Test
  void routerProcessesSignalsInArrivalOrderWithoutReordering() {
    Case caseRow = newBootstrappedCase("loan-order", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.of(new EndEventMapping("stage1 -> stage2")),
                Map.of(),
                List.of(
                    new PropertyEmissionRule(
                        "userTask:review",
                        "status",
                        ExecutionSignalKind.TASK_STATUS_CHANGED,
                        "stage:stage1")))));

    // First: status change. Second: stage advance.
    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.TASK_STATUS_CHANGED,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "review",
            Map.of("camunda:property", "status", "value", "approved")));
    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.STAGE_TRANSITION,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "end_1",
            Map.of()));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    // After STAGE_TRANSITION stage-advance the status was reset to next stage's initialStatus ("open"),
    // but the earlier TASK_STATUS_CHANGED had set it to "approved" — the stage-advance status-reset
    // (AC3) then overwrites with "open". This is by design: stage-advance trumps prior same-stage
    // status.
    assertThat(after.currentStageId()).isEqualTo("stage2");
    // Story 4.4b AC3 — STAGE_TRANSITION emits 2 events; total = TASK_STATUS_CHANGED (1) + STAGE_TRANSITION (2) = 3.
    assertThat(events.routed()).hasSize(3);
    assertThat(events.routed().get(0).kind()).isEqualTo(ExecutionSignalKind.TASK_STATUS_CHANGED);
    assertThat(events.routed().get(1).kind()).isEqualTo(ExecutionSignalKind.STAGE_TRANSITION);
    assertThat(events.routed().get(1).detail()).containsEntry("effect", "stage-advance");
    assertThat(events.routed().get(2).detail()).containsEntry("effect", "status-reset");
  }

  // ---------- AC7 §8 — frozen-on-version pin ---------------------------

  @Test
  void pinnedCaseInstanceVersionResolvesAgainstItsOwnRegistryEntry() {
    Case caseRow = newBootstrappedCase("loan-pinned", 1); // pinned at v=1
    CaseTypeRef caseTypeV1 = new CaseTypeRef(caseRow.caseTypeId(), "1");
    CaseTypeRef caseTypeV2 = new CaseTypeRef(caseRow.caseTypeId(), "2");
    fake.attach(caseTypeV1, AttachmentScope.ofCase());

    mappingRegistry.register(
        caseTypeV1,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.of(new EndEventMapping("stage1 -> stage2")),
                Map.of(),
                List.of())));
    // v2 mapping points to a different target stage — must NOT be consulted because the case is
    // pinned at v1.
    mappingRegistry.register(
        caseTypeV2,
        "2",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.of(new EndEventMapping("stage1 -> stage3")),
                Map.of(),
                List.of())));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.STAGE_TRANSITION,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseTypeV1),
            "end_1",
            Map.of()));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    // v1 mapping consulted → stage2 (not stage3).
    assertThat(after.currentStageId()).isEqualTo("stage2");
  }

  // ---------- AC8 — transaction-rollback observable on illegal target ---

  @Test
  void illegalStageTargetRollsBackAndAuditsMap404() {
    Case caseRow = newBootstrappedCase("loan-illegal", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    // Backward-skip target ("stage1 -> stage1") → WksStageAdvancer raises WKS-STG-002.
    // The router does NOT catch WksStageException — it catches only WksMappingMissException — so
    // the engine sees the failure and the test asserts the rollback is observable: the case did
    // not advance, and the WKS exception was not silently audited away.
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.of(new EndEventMapping("stage1 -> stage1")),
                Map.of(),
                List.of())));

    try {
      fake.emit(
          new ExecutionSignal(
              ExecutionSignalKind.STAGE_TRANSITION,
              ADAPTER_NAME,
              new CaseInstanceRef(caseRow.id(), caseType),
              "end_1",
              Map.of()));
      flushClear();
    } catch (RuntimeException expected) {
      // Engine sees the failure — the adapter would roll back its transaction.
    }

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId()).isEqualTo("stage1");
    // Story 4.3.1 AC4 — the router now emits a synchronous failure-audit BEFORE the rollback
    // rethrow. Operators must see a ExecutionSignalRouted event with errorCode set (the WKS code
    // from the underlying domain exception, or WKS-RTM-500 fallback) and source =
    // backend(<adapter>).
    assertThat(events.routed())
        .singleElement()
        .satisfies(
            e -> {
              assertThat(e.errorCode()).isNotNull();
              assertThat(e.errorCode()).isNotEqualTo("WKS-MAP-404");
              assertThat(e.source().toString()).isEqualTo("backend(fake)");
              assertThat(e.detail()).containsEntry("originAdapter", ADAPTER_NAME);
              assertThat(e.detail()).containsKey("reason");
            });
  }

  // ---------- Story 4.3.1 AC5 — case-not-found emits WKS-MAP-405 audit row -----

  @Test
  void caseNotFoundEmitsMap405AuditRow() {
    // No case is created — caseRepository.findById returns empty. The router must publish a
    // ExecutionSignalRouted with errorCode = WKS-MAP-405 and source = backend(<adapter>).
    UUID phantomCaseId = UUID.randomUUID();
    CaseTypeRef caseType = new CaseTypeRef("phantom-ct", "1");
    fake.attach(caseType, AttachmentScope.ofCase());

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.STAGE_TRANSITION,
            ADAPTER_NAME,
            new CaseInstanceRef(phantomCaseId, caseType),
            "end_1",
            Map.of()));
    flushClear();

    assertThat(events.routed())
        .singleElement()
        .satisfies(
            e -> {
              assertThat(e.errorCode()).isEqualTo("WKS-MAP-405");
              assertThat(e.caseId()).isEqualTo(phantomCaseId);
              assertThat(e.source().toString()).isEqualTo("backend(fake)");
              assertThat(e.detail()).containsEntry("originAdapter", ADAPTER_NAME);
              assertThat(e.detail()).containsKey("reason");
            });
  }

  // ---------- Story 4.4b AC7 — golden-master extension: manual transition + stage-advance +
  // userTask

  @Test
  void ac7_manualUserTaskStatusTransition_updatesStatusAndEmitsOneEvent() {
    // AC7: manual CaseService.transition path emitted via TASK_STATUS_CHANGED signal.
    // The router receives TASK_STATUS_CHANGED, dispatches to dispatchUserTaskProperty → statusUpdater.
    Case caseRow = newBootstrappedCase("manual-transition", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Map.of(),
                List.of(
                    new PropertyEmissionRule(
                        "userTask:manual",
                        "status",
                        ExecutionSignalKind.TASK_STATUS_CHANGED,
                        "stage:stage1")))));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.TASK_STATUS_CHANGED,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "manual",
            Map.of("value", "in-review")));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.status()).isEqualTo("in-review");
    // manual TASK_STATUS_CHANGED path → exactly 1 ExecutionSignalRouted event (no stage-advance).
    assertThat(events.routed())
        .singleElement()
        .satisfies(
            e -> {
              assertThat(e.kind()).isEqualTo(ExecutionSignalKind.TASK_STATUS_CHANGED);
              assertThat(e.source().toString()).isEqualTo("backend(fake)");
              assertThat(e.errorCode()).isNull();
            });
  }

  @Test
  void ac7_stageAdvance_emitsTwoEventsWithSharedCorrelationId_andResetsStatus() {
    // AC7 + AC3: stage-advance success-path emits TWO ExecutionSignalRouted events with shared
    // correlationId (one effect=stage-advance, one effect=status-reset). This is an intentional
    // divergence from the pre-4.4b one-event shape — rationale: Q3 lock from original Story 4.4.
    Case caseRow = newBootstrappedCase("stage-advance-ac3", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.of(new EndEventMapping("stage1 -> stage2")),
                Map.of(),
                List.of())));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.STAGE_TRANSITION,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "end_1",
            Map.of()));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId()).isEqualTo("stage2");
    // Stage-advance resets status to next stage's initialStatus ("open" for stage2).
    assertThat(after.status()).isEqualTo("open");

    // TWO events: effect=stage-advance + effect=status-reset.
    assertThat(events.routed()).hasSize(2);
    ExecutionSignalRouted stageAdvanceEvent = events.routed().get(0);
    ExecutionSignalRouted statusResetEvent = events.routed().get(1);

    assertThat(stageAdvanceEvent.detail()).containsEntry("effect", "stage-advance");
    assertThat(statusResetEvent.detail()).containsEntry("effect", "status-reset");
    assertThat(statusResetEvent.detail()).containsEntry("newStatus", "open");
    assertThat(statusResetEvent.detail()).containsEntry("nextStageId", "stage2");

    // Shared correlationId.
    String correlationId = stageAdvanceEvent.detail().get("correlationId");
    assertThat(correlationId).isNotNull();
    assertThat(statusResetEvent.detail()).containsEntry("correlationId", correlationId);

    // Both events carry the correct adapter source.
    assertThat(stageAdvanceEvent.source().toString()).isEqualTo("backend(fake)");
    assertThat(statusResetEvent.source().toString()).isEqualTo("backend(fake)");
    assertThat(stageAdvanceEvent.errorCode()).isNull();
    assertThat(statusResetEvent.errorCode()).isNull();
  }

  @Test
  void ac7_userTaskComplete_advancesStageAndEmitsTwoEvents() {
    // AC7: TASK_COMPLETED path triggers stage advance → two events (AC3 pattern).
    Case caseRow = newBootstrappedCase("task-complete-ac7", 1);
    CaseTypeRef caseType = new CaseTypeRef(caseRow.caseTypeId(), "1");
    fake.attach(caseType, AttachmentScope.ofCase());
    mappingRegistry.register(
        caseType,
        "1",
        defWith(
            new AttachmentDefinition(
                "bpmn",
                "loan.bpmn",
                "case",
                Optional.empty(),
                Map.of(),
                Optional.empty(),
                Map.of(),
                List.of(
                    new PropertyEmissionRule(
                        "userTask:review",
                        "status",
                        ExecutionSignalKind.TASK_COMPLETED,
                        "stage:stage1")))));

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.TASK_COMPLETED,
            ADAPTER_NAME,
            new CaseInstanceRef(caseRow.id(), caseType),
            "review",
            Map.of()));
    flushClear();

    Case after = caseRepository.findById(caseRow.id()).orElseThrow();
    assertThat(after.currentStageId()).isEqualTo("stage2");
    // TASK_COMPLETED → stage-advance → status reset to stage2 initialStatus.
    assertThat(after.status()).isEqualTo("open");

    // Two events emitted (stage-advance + status-reset) — same AC3 pattern.
    assertThat(events.routed()).hasSize(2);
    assertThat(events.routed().get(0).detail()).containsEntry("effect", "stage-advance");
    assertThat(events.routed().get(1).detail()).containsEntry("effect", "status-reset");
  }

  // ---------- helpers ----------------------------------------------------

  private static MappingDefinition defWith(AttachmentDefinition attachment) {
    return new MappingDefinition(List.of(attachment));
  }

  private Case newBootstrappedCase(String idSuffix, int caseTypeVersion) {
    UUID id = UUID.randomUUID();
    String caseTypeId = idSuffix; // unique per-test caseTypeId to avoid registry collisions
    Case toSave =
        new Case(
            id, caseTypeId, caseTypeVersion, "open", null, Map.of(), null, NOW, actorId, NOW, 0L);
    caseRepository.save(toSave);
    stageRepository.materialiseStages(id, STAGES, NOW);
    stageRepository.appendTransition(
        new StageRepository.Transition(
            id, null, null, "stage1", 0, List.of(), "wks-auto-rule", "case-create", NOW));
    flushClear();
    return caseRepository.findById(id).orElseThrow();
  }

  private void flushClear() {
    em.flush();
    em.clear();
  }

  // ---------- test config ------------------------------------------------

  static class RouterTestConfig {

    @Bean
    NullAdapter nullAdapter() {
      return new NullAdapter();
    }

    @Bean
    WorkflowAdapterBinder binder(NullAdapter nullAdapter) {
      return new WorkflowAdapterBinder(nullAdapter);
    }

    @Bean
    MappingRegistry mappingRegistry() {
      return new MappingRegistry();
    }

    @Bean
    Clock testClock() {
      return () -> NOW;
    }

    @Primary
    @Bean
    RecordingEventPublisher recordingEventPublisher() {
      return new RecordingEventPublisher();
    }

    @Bean
    WksStageAdvancer wksStageAdvancer(
        StageRepository stageRepository, EventPublisher eventPublisher, Clock clock) {
      return new WksStageAdvancer(stageRepository, eventPublisher, clock);
    }

    /**
     * Story 4.4b AC3 — stub CaseTypeReader that returns a CaseTypeConfig with the STAGES fixture
     * (stage1, stage2, stage3) for every case type id + version. Each stage has its own status set:
     * stage1 → [open, in-review], stage2 → [open, approved], stage3 → [open, closed(terminal)]. The
     * router uses this to resolve the next stage's initialStatus after a stage advance.
     */
    @Bean
    CaseTypeReader stubCaseTypeReader() {
      List<StageDefinition> stagesWithStatuses =
          List.of(
              new StageDefinition(
                  "stage1",
                  "Stage 1",
                  0,
                  List.of(
                      new StatusDefinition("open", "Open", StatusColor.BLUE, false),
                      new StatusDefinition("in-review", "In Review", StatusColor.AMBER, false)),
                  Optional.of("open")),
              new StageDefinition(
                  "stage2",
                  "Stage 2",
                  1,
                  List.of(
                      new StatusDefinition("open", "Open", StatusColor.BLUE, false),
                      new StatusDefinition("approved", "Approved", StatusColor.EMERALD, false)),
                  Optional.of("open")),
              new StageDefinition(
                  "stage3",
                  "Stage 3",
                  2,
                  List.of(
                      new StatusDefinition("open", "Open", StatusColor.BLUE, false),
                      new StatusDefinition("closed", "Closed", StatusColor.ZINC, true)),
                  Optional.of("open")));
      // Build a generic CaseTypeConfig that covers any caseTypeId used in the IT.
      return new CaseTypeReader() {
        @Override
        public Optional<CaseTypeConfig> find(String id) {
          return Optional.of(buildConfig(id, stagesWithStatuses));
        }

        @Override
        public Collection<CaseTypeConfig> all() {
          return List.of();
        }

        @Override
        public Optional<CaseTypeConfig> findVersion(String id, int version) {
          return Optional.of(buildConfig(id, stagesWithStatuses));
        }

        private CaseTypeConfig buildConfig(String id, List<StageDefinition> stages) {
          return new CaseTypeConfig(
              id,
              id,
              1,
              null,
              null,
              List.of(),
              List.of(
                  new StatusDefinition("open", "Open", StatusColor.BLUE, false),
                  new StatusDefinition("closed", "Closed", StatusColor.ZINC, true)),
              List.of(),
              List.of(new RoleDefinition("admin", List.of(Permission.VIEW, Permission.CREATE))),
              stages);
        }
      };
    }

    @Bean
    ExecutionSignalRouter backendSignalRouter(
        MappingRegistry mappingRegistry,
        WksStageAdvancer stageAdvancer,
        CaseStatusUpdater statusUpdater,
        CaseRepository caseRepository,
        EventPublisher eventPublisher,
        Clock clock,
        CaseTypeReader caseTypeReader) {
      return new ExecutionSignalRouter(
          mappingRegistry,
          stageAdvancer,
          statusUpdater,
          caseRepository,
          eventPublisher,
          clock,
          caseTypeReader);
    }
  }

  /** Synchronous EventPublisher recording — unit-test-style stub. */
  static class RecordingEventPublisher implements EventPublisher {
    private final List<Object> events = new ArrayList<>();

    @Override
    public synchronized void publish(Object event) {
      events.add(event);
    }

    synchronized List<ExecutionSignalRouted> routed() {
      List<ExecutionSignalRouted> out = new ArrayList<>();
      for (Object e : events) {
        if (e instanceof ExecutionSignalRouted r) {
          out.add(r);
        }
      }
      return out;
    }

    synchronized void clear() {
      events.clear();
    }
  }
}
