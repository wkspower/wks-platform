package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.event.CaseCreated;
import com.wkspower.platform.domain.event.CaseDataEdited;
import com.wkspower.platform.domain.event.CaseUpdated;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksValidationAggregateException;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.model.AuditSource;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.port.CaseDataValidator;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pure-Java enumeration of {@code CaseService} branches. No Spring context, no engine, no DB —
 * every collaborator is a hand-rolled stub.
 */
class CaseServiceTest {

  private static final UUID ACTOR = UUID.randomUUID();
  private static final Instant FIXED = Instant.parse("2026-04-26T10:00:00Z");

  private StubRepo repo;
  private StubValidator validator;
  private StubEngine engine;
  private StubPublisher publisher;
  private StubResolver resolver;

  @BeforeEach
  void resetStubs() {
    repo = new StubRepo();
    validator = new StubValidator();
    engine = new StubEngine("pi-1", null);
    publisher = new StubPublisher();
    resolver = new StubResolver("applicationProcess");
  }

  private CaseService svc(CaseTypeConfig config) {
    return svc(config, new MappingRegistry());
  }

  private CaseService svc(CaseTypeConfig config, MappingRegistry mappingRegistry) {
    WksStageAdvancer advancer =
        new WksStageAdvancer(new NoopStageRepository(), publisher, () -> FIXED);
    com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry registry =
        new com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry();
    // Seed a v1 row so CaseService.create's registry bind succeeds for the fixture id.
    registry.seed(
        config.id(),
        config.version() == 0 ? 1 : config.version(),
        ("id: " + config.id()).getBytes());
    // Story 4.4b AC1 — wire no-op stubs for the new router + status-updater dependencies.
    ExecutionSignalHandler noopRouter = signal -> {};
    CaseStatusUpdater noopStatusUpdater =
        (id, status) -> {
          // Update in the repo stub so findById returns the new status after transition.
          return repo.findById(id)
              .map(
                  existing -> {
                    String prev = existing.status();
                    Case updated =
                        new Case(
                            existing.id(),
                            existing.caseTypeId(),
                            existing.caseTypeVersion(),
                            status,
                            existing.assignee(),
                            existing.data(),
                            existing.processInstanceId(),
                            existing.createdAt(),
                            existing.createdBy(),
                            existing.updatedAt(),
                            existing.version());
                    repo.save(updated);
                    return prev;
                  });
        };
    return new CaseService(
        repo,
        reader(config),
        validator,
        engine,
        resolver,
        publisher,
        () -> FIXED,
        advancer,
        registry,
        noopRouter,
        noopStatusUpdater,
        mappingRegistry);
  }

  @Test
  void createHappyPathPersistsAndPublishesCaseCreated() {
    CaseService svc = svc(loanType());

    Case created = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);

    assertThat(created.caseTypeId()).isEqualTo("loan-application");
    assertThat(created.caseTypeVersion()).isEqualTo(1);
    assertThat(created.status()).isEqualTo("open");
    assertThat(created.processInstanceId()).isEqualTo("pi-1");
    assertThat(created.data()).containsEntry("name", "Asha");
    assertThat(repo.saved).hasSize(1);

    assertThat(publisher.events).hasSize(1);
    CaseCreated event = (CaseCreated) publisher.events.get(0);
    assertThat(event.caseId()).isEqualTo(created.id());
    assertThat(event.actorId()).isEqualTo(ACTOR);
  }

  @Test
  void createWithUnknownCaseTypeThrows404() {
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.create("no-such-type", Map.of(), null, ACTOR))
        .isInstanceOf(WksNotFoundException.class);
  }

  @Test
  void createWithFailingValidationAggregatesErrors() {
    validator.queue(
        List.of(
            ErrorDetail.ofField("WKS-API-001", "must not be blank", "name"),
            ErrorDetail.ofField("WKS-API-001", "too long", "notes")));
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.create("loan-application", Map.of(), null, ACTOR))
        .isInstanceOf(WksValidationAggregateException.class)
        .satisfies(
            ex -> {
              WksValidationAggregateException agg = (WksValidationAggregateException) ex;
              assertThat(agg.getErrors()).hasSize(2);
            });
  }

  @Test
  void createWhenProcessKeyUnknownThrowsEngineException() {
    resolver = new StubResolver(null);
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.create("loan-application", Map.of("name", "x"), null, ACTOR))
        .isInstanceOf(WksWorkflowEngineException.class);
  }

  @Test
  void createWhenEngineFailsPropagates() {
    engine = new StubEngine(null, new WksWorkflowEngineException("engine down"));
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.create("loan-application", Map.of("name", "x"), null, ACTOR))
        .isInstanceOf(WksWorkflowEngineException.class);
  }

  @Test
  void updateHappyPathBumpsVersionAndPublishesCaseUpdated() {
    CaseTypeConfig loan = loanType();
    CaseService svc = svc(loan);
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);
    publisher.events.clear();

    Case updated =
        svc.update(seeded.id(), Map.of("name", "Asha", "amount", 250000), seeded.version(), ACTOR);

    assertThat(updated.data()).containsEntry("amount", 250000);
    assertThat(updated.version()).isGreaterThan(seeded.version());
    assertThat(publisher.events).hasSize(1);
    CaseUpdated event = (CaseUpdated) publisher.events.get(0);
    assertThat(event.changedFieldIds()).contains("amount");
  }

  // ----------------------------------------------------------------------
  // Story 6.3 AC-2 / AC-3 / AC-6 — edit-contract gating + AFTER_COMMIT audit channel.
  // Exercises CaseService.update with EditContractGate wired through MappingRegistry +
  // WorkflowEngine.findTasksByCase. The gate degrades to allow when the case-type has no
  // mapping registered; the tests below explicitly register a MappingDefinition tying a
  // BPMN userTask key to a form that owns one of the two case-type fields.
  // ----------------------------------------------------------------------

  @Test
  void directEdit_returns422_whenOpenTaskOwnsField() {
    CaseTypeConfig loanWithForm = loanTypeWithForm();
    MappingRegistry mappingRegistry = mappingRegistryFor(loanWithForm);
    CaseService svc = svc(loanWithForm, mappingRegistry);
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);
    publisher.events.clear();
    publisher.afterCommitEvents.clear();

    // Open task owns the "name" field via the intake-form -> intake-task mapping.
    engine.setOpenTasks(List.of(taskOpen(seeded.id(), "task-1", "intake-task")));

    assertThatThrownBy(
            () -> svc.update(seeded.id(), Map.of("name", "Bob"), seeded.version(), ACTOR))
        .isInstanceOf(WksValidationAggregateException.class)
        .satisfies(
            ex -> {
              WksValidationAggregateException agg = (WksValidationAggregateException) ex;
              assertThat(agg.getErrors())
                  .extracting(ErrorDetail::code)
                  .containsExactly(ErrorCode.WKS_EDIT_001.wire());
              assertThat(agg.getErrors().get(0).field()).isEqualTo("name");
              // Story 6-3b AC2 — user-facing message must NOT leak raw ids. The localized
              // copy is "Complete the open task to update this field."; the WKS-EDIT-001
              // log line still carries openTaskId / formId for SI debugging.
              assertThat(agg.getErrors().get(0).message())
                  .isEqualTo("Complete the open task to update this field.");
              assertThat(agg.getErrors().get(0).message()).doesNotContain("openTaskId=", "formId=");
            });

    // Pre-commit throw -> no AFTER_COMMIT audit, no CaseUpdated.
    assertThat(publisher.afterCommitEvents).noneMatch(e -> e instanceof CaseDataEdited);
    assertThat(publisher.events).noneMatch(e -> e instanceof CaseUpdated);
  }

  @Test
  void directEdit_returns200_whenNoOpenTaskOwnsField_andUserPermitted() {
    CaseTypeConfig loanWithForm = loanTypeWithForm();
    MappingRegistry mappingRegistry = mappingRegistryFor(loanWithForm);
    CaseService svc = svc(loanWithForm, mappingRegistry);
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);
    publisher.events.clear();
    publisher.afterCommitEvents.clear();

    // No open tasks -> AC-3 happy path.
    engine.setOpenTasks(List.of());

    Case updated = svc.update(seeded.id(), Map.of("name", "Bob"), seeded.version(), ACTOR);

    assertThat(updated.data()).containsEntry("name", "Bob");
    assertThat(publisher.events).anyMatch(e -> e instanceof CaseUpdated);

    // AC-6: one CaseDataEdited(APPLIED) per changed field, AFTER_COMMIT, sourced as User.
    assertThat(publisher.afterCommitEvents)
        .filteredOn(e -> e instanceof CaseDataEdited)
        .hasSize(1)
        .extracting(e -> (CaseDataEdited) e)
        .singleElement()
        .satisfies(
            edited -> {
              assertThat(edited.result()).isEqualTo(CaseDataEdited.Result.APPLIED);
              assertThat(edited.fieldId()).isEqualTo("name");
              assertThat(edited.source()).isInstanceOf(AuditSource.User.class);
              assertThat(((AuditSource.User) edited.source()).actorId()).isEqualTo(ACTOR);
            });
  }

  @Test
  void directEdit_emits_no_audit_event_when_no_fields_changed() {
    CaseTypeConfig loanWithForm = loanTypeWithForm();
    MappingRegistry mappingRegistry = mappingRegistryFor(loanWithForm);
    CaseService svc = svc(loanWithForm, mappingRegistry);
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);
    publisher.events.clear();
    publisher.afterCommitEvents.clear();

    engine.setOpenTasks(List.of());

    // Re-PUT identical data — no field changes.
    svc.update(seeded.id(), Map.of("name", "Asha"), seeded.version(), ACTOR);

    assertThat(publisher.afterCommitEvents).noneMatch(e -> e instanceof CaseDataEdited);
  }

  // ---- 6.3 helpers ----

  private static CaseTypeConfig loanTypeWithForm() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(
            new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("officer", List.of(Permission.VIEW, Permission.CREATE))),
        List.of(),
        List.of(
            new FormDefinition(
                "intake-form",
                "single",
                "monolithic",
                "single-page",
                List.of(
                    new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
                List.of(),
                "submit_for_processing")));
  }

  private static MappingRegistry mappingRegistryFor(CaseTypeConfig caseType) {
    MappingRegistry mr = new MappingRegistry();
    AttachmentDefinition attachment =
        new AttachmentDefinition(
            "bpmn",
            "loan-application.bpmn",
            "case",
            Optional.empty(),
            Map.of("intake-task", new UserTaskMapping("intake-task", "intake-form")),
            Optional.empty(),
            Map.of(),
            List.of(),
            Map.of());
    mr.register(
        new CaseTypeRef(caseType.id(), String.valueOf(caseType.version())),
        String.valueOf(caseType.version()),
        new MappingDefinition(List.of(attachment)));
    return mr;
  }

  private static Task taskOpen(UUID caseId, String taskId, String defKey) {
    return new Task(
        taskId,
        "pi-1",
        "pd-1",
        caseId,
        "loan-application",
        defKey,
        "Intake",
        null,
        "submit_for_processing",
        FIXED,
        null);
  }

  @Test
  void updateWithVersionMismatchThrowsConflict() {
    CaseService svc = svc(loanType());
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);

    assertThatThrownBy(
            () -> svc.update(seeded.id(), Map.of("name", "Bob"), seeded.version() + 99, ACTOR))
        .isInstanceOf(WksConflictException.class);
  }

  @Test
  void updateWhenCaseMissingThrows404() {
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.update(UUID.randomUUID(), Map.of(), 0L, ACTOR))
        .isInstanceOf(WksNotFoundException.class);
  }

  @Test
  void updateWithFailingValidationAggregates() {
    CaseService svc = svc(loanType());
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);

    validator.queue(List.of(ErrorDetail.ofField("WKS-API-001", "bad value", "name")));

    assertThatThrownBy(() -> svc.update(seeded.id(), Map.of("name", ""), seeded.version(), ACTOR))
        .isInstanceOf(WksValidationAggregateException.class);
  }

  // ---- Story 3.6 AC6 — transition guards run BEFORE engine call ----

  @Test
  void transitionRejectsTerminalStatusWithWksStg011() {
    var stage =
        new StageDefinition(
            "underwriting",
            "Underwriting",
            0,
            List.of(
                new StatusDefinition("pending", "Pending", StatusColor.AMBER, false),
                new StatusDefinition("ready", "Ready", StatusColor.EMERALD, true)),
            Optional.of("pending"));
    var ct =
        new CaseTypeConfig(
            "ct-term",
            "CT",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE, false)),
            List.of(),
            List.of(new RoleDefinition("officer", List.of(Permission.VIEW))),
            List.of(stage),
            List.of());
    UUID caseId = UUID.randomUUID();
    Case existing =
        new Case(
            caseId,
            "ct-term",
            1,
            "ready", // currently in terminal status
            null,
            Map.of(),
            "pi-1",
            FIXED,
            ACTOR,
            FIXED,
            0L,
            "underwriting",
            0);
    repo.saved.add(existing);
    CaseService svc = svc(ct);
    assertThatThrownBy(() -> svc.transition(caseId, "anything", Map.of(), ACTOR))
        .isInstanceOf(com.wkspower.platform.domain.exception.WksStageException.class)
        .matches(
            t ->
                ((com.wkspower.platform.domain.exception.WksStageException) t)
                    .getCode()
                    .equals("WKS-STG-011"))
        .hasMessageContaining("terminal");
  }

  @Test
  void transitionRejectsForeignStageStatusWithWksStg010() {
    var stageA =
        new StageDefinition(
            "intake",
            "Intake",
            0,
            List.of(new StatusDefinition("collecting", "Collecting", StatusColor.BLUE, false)),
            Optional.of("collecting"));
    var stageB =
        new StageDefinition(
            "decision",
            "Decision",
            1,
            List.of(new StatusDefinition("approved", "Approved", StatusColor.EMERALD, true)),
            Optional.of("approved"));
    var ct =
        new CaseTypeConfig(
            "ct-foreign",
            "CT",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE, false)),
            List.of(),
            List.of(new RoleDefinition("officer", List.of(Permission.VIEW))),
            List.of(stageA, stageB),
            List.of());
    UUID caseId = UUID.randomUUID();
    Case existing =
        new Case(
            caseId,
            "ct-foreign",
            1,
            "collecting",
            null,
            Map.of(),
            "pi-1",
            FIXED,
            ACTOR,
            FIXED,
            0L,
            "intake",
            0);
    repo.saved.add(existing);
    CaseService svc = svc(ct);
    assertThatThrownBy(() -> svc.transition(caseId, "approved", Map.of(), ACTOR))
        .isInstanceOf(com.wkspower.platform.domain.exception.WksStageException.class)
        .matches(
            t ->
                ((com.wkspower.platform.domain.exception.WksStageException) t)
                    .getCode()
                    .equals("WKS-STG-010"))
        .hasMessageContaining("foreign-stage");
  }

  @Test
  void transitionRejectsUnknownActionOnZeroProcessPath() {
    // I1 guard: zero-process path must reject unknown action strings (not declared status ids).
    // Writing an arbitrary string (e.g. a stale BPMN message name "submit-form") into
    // cases.status corrupts the row and was silently returning HTTP 200. The fix: throw
    // WksValidationException (HTTP 400) when the action is not a known status id.
    // The foreign-stage guard must NOT fire for "submit-form" (it only fires when the action IS
    // a known status id but on a different stage). The I1 guard fires next: unknown → rejected.
    var stage =
        new StageDefinition(
            "intake",
            "Intake",
            0,
            List.of(new StatusDefinition("collecting", "Collecting", StatusColor.BLUE, false)),
            Optional.of("collecting"));
    var ct =
        new CaseTypeConfig(
            "ct-pass",
            "CT",
            1,
            null,
            null,
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE, false)),
            List.of(),
            List.of(new RoleDefinition("officer", List.of(Permission.VIEW))),
            List.of(stage),
            List.of());
    UUID caseId = UUID.randomUUID();
    Case existing =
        new Case(
            caseId,
            "ct-pass",
            1,
            "collecting",
            null,
            Map.of(),
            null, // no processInstanceId → zero-process path
            FIXED,
            ACTOR,
            FIXED,
            0L,
            "intake",
            0);
    repo.saved.add(existing);
    CaseService svc = svc(ct);
    // "submit-form" is not a known status id anywhere on ct-pass — zero-process path must reject.
    assertThatThrownBy(() -> svc.transition(caseId, "submit-form", Map.of(), ACTOR))
        .isInstanceOf(com.wkspower.platform.domain.exception.WksValidationException.class)
        .hasMessageContaining("submit-form")
        .hasMessageContaining("not a known status id");
  }

  // ---- gap-10 fix-a: initialStatus resolution ----------------------------

  /**
   * Story 6.2 Decision B — when the YAML omits top-level statuses (validator injects {@code [open,
   * closed]} with {@code explicitTopLevelStatuses = false}) and stages declare {@code
   * initialStatus}, the first stage's initialStatus is used (gap-10 fix-a preserved).
   */
  @Test
  void initialStatus_injectedDefaults_plusStageInitial_usesStageInitial() {
    // Simulate bpmn-sequential-staged: ConfigValidator injected [open, closed] defaults
    // (explicitTopLevelStatuses = false). Stages declare their own statuses and initialStatus.
    CaseTypeConfig stagedType =
        CaseTypeConfig.builder()
            .id("staged")
            .displayName("Staged")
            .version(1)
            .statuses(
                List.of(
                    new StatusDefinition("open", "Open", StatusColor.ZINC),
                    new StatusDefinition("closed", "Closed", StatusColor.ZINC)))
            .explicitTopLevelStatuses(false)
            .stages(
                List.of(
                    new StageDefinition(
                        "intake",
                        "Intake",
                        0,
                        List.of(new StatusDefinition("drafting", "Drafting", StatusColor.AMBER)),
                        Optional.of("drafting")),
                    new StageDefinition(
                        "review",
                        "Review",
                        1,
                        List.of(new StatusDefinition("in-review", "In Review", StatusColor.BLUE)),
                        Optional.of("in-review"))))
            .build();

    assertThat(CaseService.initialStatus(stagedType)).isEqualTo("drafting");
  }

  /**
   * Story 6.2 Decision B — when the YAML explicitly declared top-level {@code statuses:} ({@code
   * explicitTopLevelStatuses == true}) AND stages also declare initialStatus, the explicit
   * top-level wins. Author intent: flat lifecycle even alongside stages.
   */
  @Test
  void initialStatus_explicitTopLevel_winsOverStageInitial() {
    CaseTypeConfig type =
        CaseTypeConfig.builder()
            .id("explicit-top")
            .displayName("Explicit")
            .version(1)
            .statuses(
                List.of(
                    new StatusDefinition("triage", "Triage", StatusColor.BLUE),
                    new StatusDefinition("closed", "Closed", StatusColor.ZINC)))
            .explicitTopLevelStatuses(true)
            .stages(
                List.of(
                    new StageDefinition(
                        "intake",
                        "Intake",
                        0,
                        List.of(new StatusDefinition("drafting", "Drafting", StatusColor.AMBER)),
                        Optional.of("drafting"))))
            .build();

    assertThat(CaseService.initialStatus(type)).isEqualTo("triage");
  }

  /**
   * Story 6.2 Decision B — no stages and only injected default statuses → returns the first
   * injected default ("open"). Reaches branch c (fallback to top-level statuses[0]).
   */
  @Test
  void initialStatus_noStages_injectedDefaultsOnly_returnsOpen() {
    CaseTypeConfig type =
        CaseTypeConfig.builder()
            .id("no-stages")
            .displayName("No Stages")
            .version(1)
            .statuses(
                List.of(
                    new StatusDefinition("open", "Open", StatusColor.ZINC),
                    new StatusDefinition("closed", "Closed", StatusColor.ZINC)))
            .explicitTopLevelStatuses(false)
            .build();
    assertThat(CaseService.initialStatus(type)).isEqualTo("open");
  }

  /**
   * Story 6.2 Decision B — empty stages and empty top-level statuses → branch d returns null.
   * Defensive — ConfigValidator's injected defaults make this unreachable in practice, but the
   * method must not throw.
   */
  @Test
  void initialStatus_nothing_returnsNull() {
    CaseTypeConfig type =
        CaseTypeConfig.builder()
            .id("nothing")
            .displayName("Nothing")
            .version(1)
            .explicitTopLevelStatuses(false)
            .build();
    assertThat(CaseService.initialStatus(type)).isNull();
  }

  /**
   * Story 6.2 AC6 — when top-level statuses are explicitly declared and NO stage has an
   * initialStatus, the top-level statuses[0].id is used (legacy / flat case types).
   */
  @Test
  void initialStatus_topLevelStatuses_usesFirstTopLevelStatus() {
    CaseTypeConfig flatType = loanType(); // has status [open], no stages
    assertThat(CaseService.initialStatus(flatType)).isEqualTo("open");
  }

  /**
   * Story 6.2 AC6 — when stages exist but none declares an initialStatus (bare-string stage form),
   * falls back to top-level statuses[0].id.
   */
  @Test
  void initialStatus_stagesWithoutInitialStatus_fallsBackToTopLevel() {
    CaseTypeConfig typeWithBareStagsAndFlatStatuses =
        CaseTypeConfig.builder()
            .id("bare-stages")
            .displayName("Bare")
            .version(1)
            .statuses(List.of(new StatusDefinition("pending", "Pending", StatusColor.ZINC)))
            .stages(
                List.of(
                    new StageDefinition("s1", "S1", 0), // no statuses, no initialStatus
                    new StageDefinition("s2", "S2", 1)))
            .build();

    assertThat(CaseService.initialStatus(typeWithBareStagsAndFlatStatuses)).isEqualTo("pending");
  }

  @Test
  void diffFieldIdsHandlesAddedRemovedAndChanged() {
    Map<String, Object> oldMap = Map.of("a", 1, "b", "two");
    Map<String, Object> newMap = Map.of("a", 1, "b", "TWO", "c", true);

    var changed = CaseService.diffFieldIds(oldMap, newMap);

    assertThat(changed).contains("b", "c");
    assertThat(changed).doesNotContain("a");
  }

  // ---- helpers -----------------------------------------------------------

  private static CaseTypeConfig loanType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("officer", List.of(Permission.VIEW, Permission.CREATE))),
        List.of(),
        List.of());
  }

  private static CaseTypeReader reader(CaseTypeConfig config) {
    return new CaseTypeReader() {
      @Override
      public Optional<CaseTypeConfig> find(String id) {
        return id.equals(config.id()) ? Optional.of(config) : Optional.empty();
      }

      @Override
      public Collection<CaseTypeConfig> all() {
        return List.of(config);
      }

      @Override
      public Optional<CaseTypeConfig> findVersion(String id, int version) {
        return id.equals(config.id()) ? Optional.of(config) : Optional.empty();
      }
    };
  }

  private static final class StubRepo implements CaseRepository {
    final List<Case> saved = new ArrayList<>();

    @Override
    public Case save(Case caseToSave) {
      saved.removeIf(c -> c.id().equals(caseToSave.id()));
      Case bumped =
          new Case(
              caseToSave.id(),
              caseToSave.caseTypeId(),
              caseToSave.caseTypeVersion(),
              caseToSave.status(),
              caseToSave.assignee(),
              caseToSave.data(),
              caseToSave.processInstanceId(),
              caseToSave.createdAt(),
              caseToSave.createdBy(),
              caseToSave.updatedAt(),
              caseToSave.version() + 1);
      saved.add(bumped);
      return bumped;
    }

    @Override
    public Optional<Case> findById(UUID id) {
      return saved.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public Page<CaseSummary> findByCaseType(CaseQuery query, PageRequest pageRequest) {
      return new Page<>(List.of(), 0, pageRequest.page(), pageRequest.size());
    }

    @Override
    public Map<UUID, Map<String, Object>> findDataByIds(
        java.util.Collection<UUID> ids, java.util.Set<String> projectedFieldIds) {
      return Map.of();
    }

    @Override
    public int updateCaseTypeVersion(UUID caseId, int toCaseTypeVersion, long expectedVersion) {
      return 0;
    }

    @Override
    public int updateCaseTypeVersionAndStage(
        UUID caseId,
        int toCaseTypeVersion,
        String toStageId,
        int toStageOrdinal,
        long expectedVersion) {
      return 0;
    }
  }

  private static final class StubValidator implements CaseDataValidator {
    private List<ErrorDetail> next = List.of();

    void queue(List<ErrorDetail> errors) {
      this.next = errors;
    }

    @Override
    public List<ErrorDetail> validate(CaseTypeConfig caseType, Map<String, Object> data) {
      List<ErrorDetail> r = next;
      next = List.of();
      return r;
    }
  }

  private static final class StubEngine implements WorkflowEngine {
    private final String returnedPi;
    private final RuntimeException failure;
    private List<com.wkspower.platform.domain.model.Task> openTasks = List.of();

    StubEngine(String returnedPi, RuntimeException failure) {
      this.returnedPi = returnedPi;
      this.failure = failure;
    }

    void setOpenTasks(List<com.wkspower.platform.domain.model.Task> tasks) {
      this.openTasks = List.copyOf(tasks);
    }

    @Override
    public DeploymentResult deploy(DeploymentRequest request) {
      return new DeploymentResult("d", request.processDefinitionKey(), "p", 1, FIXED);
    }

    @Override
    public Optional<DeploymentInfo> latestDeployment(String key) {
      return Optional.empty();
    }

    @Override
    public String startProcessInstance(String key, Map<String, Object> variables) {
      if (failure != null) throw failure;
      return returnedPi;
    }

    @Override
    public Optional<com.wkspower.platform.domain.model.Task> findTask(String taskId) {
      return Optional.empty();
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {}

    @Override
    public void claimTask(String taskId, UUID userId) {}

    @Override
    public void signalTransition(
        String processInstanceId, String action, Map<String, Object> variables) {}

    @Override
    public List<com.wkspower.platform.domain.model.Task> findTasksByCase(UUID caseId) {
      return openTasks;
    }

    @Override
    public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
      return null;
    }
  }

  private static final class StubPublisher implements EventPublisher {
    final List<Object> events = new ArrayList<>();
    final List<Object> afterCommitEvents = new ArrayList<>();

    @Override
    public void publish(Object event) {
      events.add(event);
    }

    @Override
    public void publishAfterCommit(Object event) {
      afterCommitEvents.add(event);
    }
  }

  /**
   * Stage repo stub — never persists, returns empty history. CaseService stubs use zero-stage
   * CaseTypes.
   */
  private static final class NoopStageRepository implements StageRepository {
    @Override
    public List<com.wkspower.platform.domain.model.Stage> loadHistory(UUID caseId) {
      return List.of();
    }

    @Override
    public void materialiseStages(
        UUID caseId, List<StageDefinition> stages, java.time.Instant createdAt) {}

    @Override
    public void appendTransition(Transition transition) {}

    @Override
    public void remapStage(
        UUID caseId, String fromStageId, String toStageId, int toOrdinal, java.time.Instant at) {}
  }

  private static final class StubResolver implements ProcessDefinitionKeyResolver {
    private final String key;

    StubResolver(String key) {
      this.key = key;
    }

    @Override
    public Optional<String> resolve(String caseTypeId) {
      return Optional.ofNullable(key);
    }
  }
}
