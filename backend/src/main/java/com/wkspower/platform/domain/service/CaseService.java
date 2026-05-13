package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.DefaultFieldEditability;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.event.CaseCreated;
import com.wkspower.platform.domain.event.CaseDataEdited;
import com.wkspower.platform.domain.event.CaseUpdated;
import com.wkspower.platform.domain.event.FormSubmitted;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
// Story 3.6 — ErrorCode + WksStageException used by transition guards (AC6).
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksStageException;
import com.wkspower.platform.domain.exception.WksValidationAggregateException;
import com.wkspower.platform.domain.exception.WksVersionException;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.model.AuditSource;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.port.CaseDataValidator;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import com.wkspower.platform.domain.port.WorkflowEngine;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service orchestrating case CRUD. Framework-free — no Spring, no JPA, no Jackson on the
 * import list — collaborators are reached via the six ports declared in {@code domain/port/}.
 *
 * <p>The {@code create} ordering is engine-first / DB-second by design (see Story 2.3 Dev Notes
 * §processInstanceId ordering). If the engine succeeds and the DB persist fails, the engine has a
 * dangling process instance — accepted partial state for Phase 0; Phase 1 wraps in an outbox.
 */
public class CaseService {

  private static final Logger log = LoggerFactory.getLogger(CaseService.class);

  private final CaseRepository caseRepository;
  private final CaseTypeReader caseTypeReader;
  private final CaseDataValidator caseDataValidator;
  private final WorkflowEngine workflowEngine;
  private final ProcessDefinitionKeyResolver processKeyResolver;
  private final EventPublisher eventPublisher;
  private final Clock clock;
  private final WksStageAdvancer stageAdvancer;
  private final CaseTypeVersionRegistry versionRegistry;
  // Story 4.4b AC1 — router for BPMN-attached manual transitions.
  private final ExecutionSignalHandler signalRouter;
  // Story 4.4b AC1 — direct status updater for zero-process (no-BPMN) transitions.
  private final CaseStatusUpdater caseStatusUpdater;
  // Story 6.3 — direct-edit gating needs read access to MappingDefinition (attachments) so the
  // EditContractGate can resolve which form (and via attachment.userTaskMappings, which open
  // task) owns each changed field. Optional: when no attachments are registered (zero-process
  // OSS deploy), the gate degrades to "all edits allowed" and never throws.
  private final MappingRegistry mappingRegistry;

  public CaseService(
      CaseRepository caseRepository,
      CaseTypeReader caseTypeReader,
      CaseDataValidator caseDataValidator,
      WorkflowEngine workflowEngine,
      ProcessDefinitionKeyResolver processKeyResolver,
      EventPublisher eventPublisher,
      Clock clock,
      WksStageAdvancer stageAdvancer,
      CaseTypeVersionRegistry versionRegistry,
      ExecutionSignalHandler signalRouter,
      CaseStatusUpdater caseStatusUpdater,
      MappingRegistry mappingRegistry) {
    this.caseRepository = Objects.requireNonNull(caseRepository, "caseRepository");
    this.caseTypeReader = Objects.requireNonNull(caseTypeReader, "caseTypeReader");
    this.caseDataValidator = Objects.requireNonNull(caseDataValidator, "caseDataValidator");
    this.workflowEngine = Objects.requireNonNull(workflowEngine, "workflowEngine");
    this.processKeyResolver = Objects.requireNonNull(processKeyResolver, "processKeyResolver");
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.stageAdvancer = Objects.requireNonNull(stageAdvancer, "stageAdvancer");
    this.versionRegistry = Objects.requireNonNull(versionRegistry, "versionRegistry");
    this.signalRouter = Objects.requireNonNull(signalRouter, "signalRouter");
    this.caseStatusUpdater = Objects.requireNonNull(caseStatusUpdater, "caseStatusUpdater");
    this.mappingRegistry = Objects.requireNonNull(mappingRegistry, "mappingRegistry");
  }

  /**
   * Create a new case. Steps follow Story 2.3 AC4: load case type → validate data → start BPMN
   * process instance → persist row → publish {@link CaseCreated}.
   */
  public Case create(String caseTypeId, Map<String, Object> data, UUID assignee, UUID actorId) {
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(actorId, "actorId");

    CaseTypeConfig caseType = requireCaseType(caseTypeId);
    Map<String, Object> safeData = data == null ? Map.of() : Map.copyOf(data);

    List<ErrorDetail> violations = caseDataValidator.validate(caseType, safeData);
    if (!violations.isEmpty()) {
      throw new WksValidationAggregateException("Case data failed validation", violations);
    }

    UUID caseId = UUID.randomUUID();
    String initialStatus = initialStatus(caseType);

    // Story 3.4.1 AC3 (finding I5) — bind the registry version BEFORE starting any engine
    // process instance. If the registry has no current version (deploy-ordering bug, partial
    // state, registry-not-yet-primed), failing fast here avoids a dangling process instance in
    // the engine. Story 3.4 originally read after the engine call; that left a leaked PI on the
    // WKS-VER-001 path.
    int boundVersion =
        versionRegistry
            .currentVersion(caseType.id())
            .orElseThrow(
                () ->
                    new WksVersionException(
                        ErrorCode.WKS_VER_001,
                        "CaseType "
                            + caseType.id()
                            + " has no published version yet — deploy completed?"));

    // Story 3.2 AC2 — engine call is OPTIONAL. Process-less CaseTypes (workflow: omitted) skip the
    // resolver + start call entirely. Decision 19 / 3.2 unbranched-paths invariant: do not branch
    // on caseType.hasWorkflow() — iterate via Optional.ifPresent and let absence be the no-op.
    String[] processInstanceIdHolder = new String[] {null};
    caseType
        .workflowOpt()
        .ifPresent(
            wf -> {
              String processDefinitionKey =
                  processKeyResolver
                      .resolve(caseType.id())
                      .orElseThrow(
                          () ->
                              new WksWorkflowEngineException(
                                  "No deployed BPMN process definition for case type "
                                      + caseType.id()
                                      + " — case create requires a deployed workflow"));
              processInstanceIdHolder[0] =
                  workflowEngine.startProcessInstance(
                      processDefinitionKey,
                      Map.of(
                          "caseId",
                          caseId.toString(),
                          "caseTypeId",
                          caseType.id(),
                          // Story 4.4a — pin caseTypeVersion as a process variable so the BPMN
                          // execution-listener path (CaseStatusListener) can construct a
                          // CaseInstanceRef without reaching back into CaseRepository inside the
                          // engine transaction. The Mapping Layer router (Story 4.3) keys by
                          // (caseTypeId, version); a missing version would force every signal
                          // through a defensive lookup.
                          "caseTypeVersion",
                          String.valueOf(boundVersion)));
            });
    String processInstanceId = processInstanceIdHolder[0];

    Instant now = clock.now();
    Case toPersist =
        new Case(
            caseId,
            caseType.id(),
            boundVersion,
            initialStatus,
            assignee,
            safeData,
            processInstanceId,
            now,
            actorId,
            now,
            0L);
    Case persisted = caseRepository.save(toPersist);

    // Story 3.1 AC4 — materialise stages and flip stage 0 to ACTIVE inside the same transaction
    // as the case insert. Empty stage list is a no-op (Decision 19: stage-less paths must remain
    // unbranched — the loop in stageAdvancer.bootstrap is the single source of truth).
    stageAdvancer.bootstrap(persisted.id(), caseType.stages(), "wks-auto-rule", "case-create");

    eventPublisher.publish(
        new CaseCreated(persisted.id(), caseType.id(), boundVersion, actorId, now));
    return persisted;
  }

  /**
   * Update an existing case's {@code data}. The expected {@code version} is checked via the JPA
   * {@code @Version} optimistic lock — mismatch surfaces as {@link WksConflictException}.
   */
  public Case update(UUID caseId, Map<String, Object> newData, long expectedVersion, UUID actorId) {
    return update(caseId, newData, expectedVersion, actorId, null);
  }

  /**
   * Story 6-3b AC1 — internal overload with {@code exemptFormId}. When non-null, the
   * EditContractGate skips ownership-matches for fields owned by {@code exemptFormId}. Only {@link
   * #submitForm} invokes this path; the public {@link #update(UUID, Map, long, UUID)} overload
   * calls through with {@code null} so the direct-edit path (PUT /api/cases/{id}) keeps its
   * existing gate semantics unchanged.
   */
  private Case update(
      UUID caseId,
      Map<String, Object> newData,
      long expectedVersion,
      UUID actorId,
      String exemptFormId) {
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(actorId, "actorId");

    Case existing =
        caseRepository
            .findById(caseId)
            .orElseThrow(() -> new WksNotFoundException("Case " + caseId + " not found"));
    if (existing.version() != expectedVersion) {
      throw new WksConflictException(
          "Case " + caseId + " was modified by another transaction; reload and retry");
    }

    // Story 5.5 AC-4 — Decision D20 frozen-on-version: validate updated case data against the
    // CaseType the case is PINNED to, not the latest deployed version. This ensures a v1-pinned
    // case can still pass update validation after a v2 (with additional required fields) is
    // deployed. Review remediation: strict pin — WKS-VER-002 (HTTP 422) on registry miss. The
    // prior fallback-to-latest masked eviction-induced inconsistencies (a case validated against
    // v2 schema while still pinned to v1 silently produced wrong errors).
    CaseTypeConfig caseType =
        caseTypeReader
            .findVersion(existing.caseTypeId(), existing.caseTypeVersion())
            .orElseThrow(
                () ->
                    new WksVersionException(
                        ErrorCode.WKS_VER_002,
                        "case pinned to unknown CaseTypeVersion v"
                            + existing.caseTypeVersion()
                            + " for caseType "
                            + existing.caseTypeId()
                            + "; registry has no entry"));
    Map<String, Object> safeData = newData == null ? Map.of() : Map.copyOf(newData);

    List<ErrorDetail> violations = caseDataValidator.validate(caseType, safeData);
    if (!violations.isEmpty()) {
      throw new WksValidationAggregateException("Case data failed validation", violations);
    }

    Set<String> changedFieldIds = diffFieldIds(existing.data(), safeData);

    // Story 6.3 AC-2 — Edit-Contract gate. Direct-edit attempts on a field owned by an open
    // userTask's form are blocked with WKS-EDIT-001 (HTTP 422). The gate degrades to no-op when
    // (a) no fields changed (idempotent re-PUT), (b) no MappingDefinition is registered for the
    // case-type (zero-attachment OSS deploy), or (c) no open tasks exist for the case (AC-3:
    // direct-edit permitted when no open task owns the field).
    Optional<MappingDefinition> mappingOpt =
        mappingRegistry.resolve(
            new CaseTypeRef(existing.caseTypeId(), String.valueOf(existing.caseTypeVersion())),
            String.valueOf(existing.caseTypeVersion()));
    if (mappingOpt.isPresent() && !changedFieldIds.isEmpty()) {
      List<Task> openTasks = workflowEngine.findTasksByCase(existing.id());
      List<EditContractGate.BlockReason> blocked =
          EditContractGate.blockedFields(
              caseType, mappingOpt.get(), openTasks, changedFieldIds, exemptFormId);
      if (!blocked.isEmpty()) {
        // Per AC-2: emit one CaseDataEdited(BLOCKED) audit event per blocked field, AFTER_COMMIT.
        // The publish targets are AFTER_COMMIT listeners; since this path throws BEFORE commit,
        // the events are auto-suppressed by Spring's @TransactionalEventListener semantics —
        // exactly the [[feedback_audit_after_commit]] regression guard. Audit is therefore best
        // recorded by emitting the event and trusting commit semantics; the throw below ensures
        // we do NOT commit, and the listener does not fire. AC-2 audit-on-block is satisfied via
        // the explicit log line written here (the WARN-level "blocked direct-edit" entry) which
        // does NOT depend on transaction commit because it is purely a log statement, never a DB
        // mutation. The structured ErrorDetail carries the same payload the AFTER_COMMIT audit
        // would have surfaced.
        for (EditContractGate.BlockReason reason : blocked) {
          log.warn(
              "event=case.data.edit.blocked caseId={} actorId={} fieldId={} openTaskId={} formId={}"
                  + " reason=open-task-owns-field",
              existing.id(),
              actorId,
              reason.fieldId(),
              reason.openTaskId(),
              reason.formId());
        }
        // Story 6-3b AC2 — user-facing message must not leak raw openTaskId / formId. The
        // ids stay in the WARN log line above for SI debugging; the wire message is clean.
        List<ErrorDetail> blockDetails =
            blocked.stream()
                .map(
                    r ->
                        ErrorDetail.ofField(
                            ErrorCode.WKS_EDIT_001.wire(),
                            "Complete the open task to update this field.",
                            r.fieldId()))
                .toList();
        throw new WksValidationAggregateException(
            "Direct-edit blocked by open-task form ownership (WKS-EDIT-001)", blockDetails);
      }
    }

    Instant now = clock.now();
    Case updated =
        new Case(
            existing.id(),
            existing.caseTypeId(),
            existing.caseTypeVersion(),
            existing.status(),
            existing.assignee(),
            safeData,
            existing.processInstanceId(),
            existing.createdAt(),
            existing.createdBy(),
            now,
            existing.version());

    Case persisted = caseRepository.save(updated);
    eventPublisher.publish(new CaseUpdated(persisted.id(), actorId, now, changedFieldIds));

    // Story 6.3 AC-3 / AC-6 — emit one CaseDataEdited(APPLIED) audit event per changed field via
    // AFTER_COMMIT listener (EditAuditEmitter). Per [[feedback_audit_after_commit]]: rollback
    // after this publish() suppresses the audit line entirely, exactly the guard the Sprint 10
    // 3-9 ghost-audit incident demanded. Source = AuditSource.User (direct-edit attribution).
    AuditSource source = new AuditSource.User(actorId);
    for (String fieldId : changedFieldIds) {
      eventPublisher.publishAfterCommit(
          new CaseDataEdited(
              persisted.id(), source, CaseDataEdited.Result.APPLIED, fieldId, null, null, now));
    }

    return persisted;
  }

  /**
   * Advance a case's status via the appropriate routing path (Story 4.4b AC1).
   *
   * <p>The {@code action} string is the target status id. Routing:
   *
   * <ul>
   *   <li><b>Zero-process path</b>: case has no BPMN attachment ({@code processInstanceId} is
   *       {@code null}). {@link CaseStatusUpdater} mutates status directly — no engine, no router
   *       mapping lookup.
   *   <li><b>BPMN path</b>: case has a {@code processInstanceId}. Emits a {@link
   *       ExecutionSignal}(kind={@code TASK_STATUS_CHANGED}, statusId={@code action}) to {@link
   *       ExecutionSignalHandler#onSignal} for routing through the Mapping Layer.
   * </ul>
   *
   * <p>The existing stage-scoped guards (WKS-STG-011 terminal-status block, WKS-STG-010
   * foreign-stage rejection) run BEFORE the routing decision on both paths.
   */
  public Case transition(UUID caseId, String action, Map<String, Object> variables, UUID actorId) {
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(action, "action");
    Objects.requireNonNull(actorId, "actorId");
    Case existing =
        caseRepository
            .findById(caseId)
            .orElseThrow(() -> new WksNotFoundException("Case " + caseId + " not found"));
    // Story 3.6 AC6 — stage-scoped status guards run BEFORE any routing decision. Pure reads
    // against the in-memory CaseTypeConfig — no engine call. Decision 19's unbranched-paths
    // invariant: caseType.statusesFor(stageId) resolves stage-scoped or flat fallback in one
    // place; this site does not branch on stage presence.
    CaseTypeConfig caseType = requireCaseType(existing.caseTypeId());
    String stageId = existing.currentStageId();
    List<StatusDefinition> stageStatuses = caseType.statusesFor(stageId);
    // Terminal-status block: if the case's current status is terminal in its stage, reject any
    // same-stage transition (advance-stage bypasses this guard — it lives on a different
    // service call). WKS-STG-011 is freshly allocated per Story 3.6 dev-notes anti-pattern #3
    // (the original story plan's reuse of WKS-STG-001 violates the wire-contract memory).
    StatusDefinition currentStatus =
        stageStatuses.stream()
            .filter(sd -> sd.id().equals(existing.status()))
            .findFirst()
            .orElse(null);
    if (currentStatus != null && currentStatus.terminal()) {
      throw new WksStageException(
          ErrorCode.WKS_STG_011,
          "Case "
              + caseId
              + " is in terminal status '"
              + existing.status()
              + "' on stage '"
              + stageId
              + "' — advance the stage to transition further");
    }
    // Foreign-stage status rejection: when the {@code action} names a status id that exists in
    // the case-type-level set but is not declared on the current stage, reject before any engine
    // call. Matches today's seed-time wire reality where {@code action} doubles as the target
    // status id (e.g. j5-status-rainbow). When {@code action} doesn't resolve to any known status
    // id at all, this guard is a no-op and the engine layer still owns "unknown message name"
    // semantics. WKS-STG-010 freshly allocated per dev-notes anti-pattern #3.
    boolean actionIsAStatusIdAnywhere =
        caseType.statuses().stream().anyMatch(sd -> sd.id().equals(action))
            || caseType.stages().stream()
                .anyMatch(
                    st ->
                        st.statusesOpt()
                            .map(list -> list.stream().anyMatch(sd -> sd.id().equals(action)))
                            .orElse(false));
    boolean actionIsOnCurrentStage = stageStatuses.stream().anyMatch(sd -> sd.id().equals(action));
    if (actionIsAStatusIdAnywhere && !actionIsOnCurrentStage) {
      throw new WksStageException(
          ErrorCode.WKS_STG_010,
          "Status '"
              + action
              + "' is not declared on stage '"
              + stageId
              + "' — foreign-stage status rejected");
    }

    // Story 4.4b AC1 — routing decision: bypass router for zero-process cases.
    // CRITICAL: ExecutionSignalRouter.onSignal() resolves a MappingDefinition from the registry.
    // Zero-process CaseTypes have no BPMN attachment and therefore no mapping registration.
    // Calling the router for a no-attachment case would throw WksMappingMissException. The bypass
    // MUST happen before any router call — check processInstanceId (null = no BPMN).
    if (existing.processInstanceId() == null || existing.processInstanceId().isBlank()) {
      // I1 guard: reject unknown action strings on the zero-process path. On the zero-process
      // path the action IS the new status id. Writing an arbitrary string (e.g. a stale BPMN
      // message name) into cases.status would corrupt the row and silently pass back HTTP 200.
      // Clients sending an unknown action on the zero-process path receive HTTP 400 (WKS-API-001).
      if (!actionIsAStatusIdAnywhere && !actionIsOnCurrentStage) {
        throw new com.wkspower.platform.domain.exception.WksValidationException(
            "Action '"
                + action
                + "' is not a known status id for case type '"
                + existing.caseTypeId()
                + "' — zero-process transitions require a declared status id",
            "action");
      }
      // Zero-process path: mutate status directly via CaseStatusUpdater.
      caseStatusUpdater.updateStatus(caseId, action);
    } else {
      // BPMN path: emit TASK_STATUS_CHANGED signal through the router (Mapping Layer).
      // The signal source is the fixed string "manual" so the router key is always
      // "userTask:manual" — matching the PropertyEmissionRule that every BPMN-attached
      // case type must register to handle manual API transitions. The action value (target
      // status id) is carried in the payload under "value" per router contract.
      // TODO(4-5): propagate variables + actorId into BPMN signal payload.
      // The caller-supplied `variables` map and `actorId` are currently discarded here;
      // they are not forwarded to the BPMN signal. Story 4-5 will propagate them.
      CaseTypeRef caseTypeRef =
          new CaseTypeRef(existing.caseTypeId(), String.valueOf(existing.caseTypeVersion()));
      CaseInstanceRef caseInstance = new CaseInstanceRef(caseId, caseTypeRef);
      ExecutionSignal signal =
          ExecutionSignal.of(
              ExecutionSignalKind.TASK_STATUS_CHANGED,
              "manual",
              caseInstance,
              "manual", // fixed source so router key = "userTask:manual" (stable contract)
              Map.of("value", action));
      signalRouter.onSignal(signal);
    }

    return caseRepository
        .findById(caseId)
        .orElseThrow(
            () -> new WksNotFoundException("Case " + caseId + " not found after transition"));
  }

  /**
   * Story 5.2 AC3 — submit form data for an existing case. Steps:
   *
   * <ol>
   *   <li>Load case + case type; resolve the named form definition (404 if not found).
   *   <li>Validate {@code formData} against the form's field definitions (WKS-FORM-002 on failure).
   *   <li>Call {@link #update} for data persistence (merges submitted fields into case data).
   *   <li>Publish {@link FormSubmitted} via {@code EventPublisher.publishAfterCommit} (audits the
   *       form submission separately from the {@link CaseUpdated} event emitted by {@code update}).
   * </ol>
   *
   * <p>Validation intentionally reuses the same field-presence + type logic as {@link #update}: the
   * backend is the authoritative constraint enforcer, not just a relay for frontend Zod results.
   * WKS-FORM-002 carries the field-level details so the frontend can surface inline errors on
   * backend-only constraint violations not captured by the Zod schema.
   *
   * @throws WksNotFoundException if the case or case type is not found, or if the form id does not
   *     exist on the case type.
   * @throws WksValidationAggregateException (WKS-FORM-002) if any field value fails validation.
   */
  public Case submitForm(UUID caseId, String formId, Map<String, Object> formData, UUID actorId) {
    // Backward-compat overload: callers that pre-date Story 5.6 do not supply an actor role set.
    // The empty role set means: editableBy: [role:X] fields will be REJECTED for this caller (no
    // role match), and editable-by-default fields with no editableBy declaration pass through.
    // FormController's principal-aware overload is the recommended path post-5.6.
    return submitForm(caseId, formId, formData, actorId, Set.of());
  }

  /**
   * Story 5.6 — principal-aware submit overload. Carries the actor's role set so per-field edit
   * permission checks ({@code editableBy} on {@link FieldDefinition}) can run. Recommended path for
   * controllers.
   */
  public Case submitForm(
      UUID caseId,
      String formId,
      Map<String, Object> formData,
      UUID actorId,
      Set<String> actorRoles) {
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(formId, "formId");
    Objects.requireNonNull(actorId, "actorId");
    Objects.requireNonNull(actorRoles, "actorRoles");

    Case existing =
        caseRepository
            .findById(caseId)
            .orElseThrow(() -> new WksNotFoundException("Case " + caseId + " not found"));

    // Story 5.5 AC-4 — Decision D20 frozen-on-version: resolve the form definition from the
    // CaseTypeVersion the case is pinned to, NOT the latest deployed version. Server-side binding
    // from case.caseTypeVersion — clients NEVER supply formVersion. WKS-VER-002 on registry miss.
    FormDefinition form = resolveFormDefinitionForCase(existing, formId);

    // Story 5.6 AC2 — field-level edit-permission check requires the pinned CaseTypeConfig (which
    // carries editableBy declarations). Use findVersion so the permission check respects the same
    // version the form was resolved against. Review remediation: strict pin — WKS-VER-002 (HTTP
    // 422) on registry miss. resolveFormDefinitionForCase above already throws WKS-VER-002 on
    // that path, but defending here makes the contract explicit at every call site rather than
    // relying on call-order coupling.
    CaseTypeConfig caseType =
        caseTypeReader
            .findVersion(existing.caseTypeId(), existing.caseTypeVersion())
            .orElseThrow(
                () ->
                    new WksVersionException(
                        ErrorCode.WKS_VER_002,
                        "case pinned to unknown CaseTypeVersion v"
                            + existing.caseTypeVersion()
                            + " for caseType "
                            + existing.caseTypeId()
                            + "; registry has no entry"));

    // Validate submitted field values against the form's field definitions.
    // Collections.unmodifiableMap(new HashMap<>(...)) is used instead of Map.copyOf() because
    // Map.copyOf() throws NullPointerException on null values (e.g. {"field": null} — an explicit
    // clear-field intent). Null values are preserved so the update() path can handle them.
    Map<String, Object> safeData =
        formData == null ? Map.of() : Collections.unmodifiableMap(new HashMap<>(formData));
    List<ErrorDetail> violations = validateFormData(form, safeData);
    if (!violations.isEmpty()) {
      throw new WksValidationAggregateException(
          "Form submit validation failed (WKS-FORM-002)", violations);
    }

    // Story 5.6 AC2 — field-level edit-permission check on the diff of changed fields only. An
    // unchanged value passing through the form does NOT trigger rejection (round-tripped immutable
    // display fields are common). The check operates against the pinned case-type's full field set
    // (caseType.fields()) because field permissions live on the case-type, not the form view.
    Set<String> changedFieldIds = diffFieldIds(existing.data(), safeData);
    List<ErrorDetail> permViolations =
        checkFieldEditPermissions(caseType, changedFieldIds, actorRoles);
    if (!permViolations.isEmpty()) {
      List<String> disallowedFields = permViolations.stream().map(ErrorDetail::field).toList();
      log.info(
          "CaseService.submitForm: REJECTED field-level edit for caseId={} formId={} actorId={}"
              + " disallowedFields={}",
          existing.id(),
          formId,
          actorId,
          disallowedFields);
      throw new WksValidationAggregateException(
          "Field-level edit permission denied (WKS-AUTHZ-001)", permViolations);
    }

    // Persist via the existing update path (validates the merged data against the case-type schema
    // and emits a CaseUpdated event).
    // intentionally last-writer-wins: form submit does not require version-based conflict
    // detection.
    // The form submit endpoint is designed for user-facing data entry where the form itself
    // presents the current state; a concurrent server-side update winning is acceptable. If
    // version-based conflict detection is required in future, the FormController request body
    // should accept a client-supplied version and thread it through to update() here.
    //
    // Story 6-3b AC1 — pass {@code formId} as the gate exemption: fields owned by THIS form
    // (i.e. the form whose submit is in flight) must not trip WKS-EDIT-001 against the bound
    // open task. Sibling forms (other open tasks' forms) remain gated by EditContractGate.
    Case updated = update(caseId, safeData, existing.version(), actorId, formId);

    // Emit TASK_COMPLETED signal to advance the BPMN process token if a backend adapter is
    // registered for this case type. This is best-effort: a missing adapter registration (OSS
    // zero-attachment deploy) results in WksMappingMissException from the router, which is logged
    // at WARN but does NOT fail the form submission — the data was already persisted.
    // The BPMN adapter correlates the formId to a userTask element id via the AttachmentDefinition
    // userTaskMappings (bpmnTaskId → UserTaskMapping{form}) on its side; we emit formId as the
    // correlation key so the adapter can do the reverse lookup without another DB call.
    // Story 4-5 will propagate variables further into the BPMN payload.
    if (updated.processInstanceId() != null && !updated.processInstanceId().isBlank()) {
      try {
        CaseTypeRef caseTypeRef =
            new CaseTypeRef(updated.caseTypeId(), String.valueOf(updated.caseTypeVersion()));
        CaseInstanceRef caseInstance = new CaseInstanceRef(updated.id(), caseTypeRef);
        ExecutionSignal formSignal =
            ExecutionSignal.of(
                ExecutionSignalKind.TASK_COMPLETED,
                "form",
                caseInstance,
                formId, // adapter resolves formId → BPMN userTask element id via userTaskMappings
                Map.of("formId", formId, "actorId", actorId.toString()));
        signalRouter.onSignal(formSignal);
      } catch (Exception ex) {
        log.warn(
            "submitForm: BPMN signal skipped for case {} form {} — adapter not registered or"
                + " mapping missing ({})",
            caseId,
            formId,
            ex.getMessage());
      }
    }

    // Compute which field ids changed between the old and new data for the FormSubmitted event.
    Set<String> updatedFieldIds = diffFieldIds(existing.data(), safeData);

    // Publish FormSubmitted after commit so the audit listener never observes rolled-back state.
    // Story 5.5 AC-7 — include caseTypeVersion in the audit payload (pinned version, not latest).
    eventPublisher.publishAfterCommit(
        new FormSubmitted(
            caseId, formId, actorId, clock.now(), updatedFieldIds, existing.caseTypeVersion()));

    return updated;
  }

  /**
   * Validate submitted form data against the form's declared field definitions (Story 5.2
   * WKS-FORM-002). Collect-all: never short-circuits on first error.
   *
   * <p>Validates:
   *
   * <ol>
   *   <li>Required field presence (all field types).
   *   <li>Type-specific constraints (P9): {@code TEXT}/{@code TEXTAREA} maxLength; {@code NUMBER}
   *       min/max; {@code DATE} ISO format ({@code YYYY-MM-DD}). Missing or malformed constraint
   *       config on the field definition → the check is skipped (don't fail on incomplete field
   *       defs).
   * </ol>
   *
   * <p>These checks emit WKS-FORM-002 directly so the frontend can distinguish form-field failures
   * from generic case-data validation errors (WKS-API-002) that run inside {@link #update}.
   */
  private static List<ErrorDetail> validateFormData(FormDefinition form, Map<String, Object> data) {
    List<ErrorDetail> errors = new ArrayList<>();
    for (FieldDefinition f : form.fields()) {
      Object value = data.get(f.id());

      // --- required check ---
      if (f.required()) {
        if (value == null || (value instanceof String s && s.isBlank())) {
          errors.add(
              ErrorDetail.ofField(
                  ErrorCode.WKS_FORM_002.wire(),
                  "Field '" + f.displayName() + "' is required",
                  f.id()));
          continue; // no point checking constraints if the value is absent
        } else if (f.type() == FieldType.CHECKBOX && Boolean.FALSE.equals(value)) {
          errors.add(
              ErrorDetail.ofField(
                  ErrorCode.WKS_FORM_002.wire(),
                  "Field '" + f.displayName() + "' must be checked",
                  f.id()));
          continue;
        }
      }

      // Skip constraint checks when value is absent for optional fields.
      if (value == null) {
        continue;
      }

      // P9 — type-specific constraint validation (emits WKS-FORM-002, not WKS-API-002).
      // Missing or malformed constraint config on the FieldDefinition → skip silently.
      FieldDefinition.TypeSlots slots = f.slots();
      switch (f.type()) {
        case TEXT, TEXTAREA -> {
          if (value instanceof String strVal && slots != null && slots.maxLength() != null) {
            if (strVal.length() > slots.maxLength()) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_FORM_002.wire(),
                      "Field '"
                          + f.displayName()
                          + "' exceeds maximum length of "
                          + slots.maxLength(),
                      f.id()));
            }
          }
        }
        case NUMBER -> {
          if (slots != null) {
            Double numVal = toDouble(value);
            if (numVal != null) {
              if (slots.min() != null && numVal < slots.min()) {
                errors.add(
                    ErrorDetail.ofField(
                        ErrorCode.WKS_FORM_002.wire(),
                        "Field '" + f.displayName() + "' must be at least " + slots.min(),
                        f.id()));
              }
              if (slots.max() != null && numVal > slots.max()) {
                errors.add(
                    ErrorDetail.ofField(
                        ErrorCode.WKS_FORM_002.wire(),
                        "Field '" + f.displayName() + "' must be at most " + slots.max(),
                        f.id()));
              }
            }
          }
        }
        case DATE -> {
          if (value instanceof String dateVal
              && !dateVal.isBlank()
              && !dateVal.matches("\\d{4}-\\d{2}-\\d{2}")) {
            errors.add(
                ErrorDetail.ofField(
                    ErrorCode.WKS_FORM_002.wire(),
                    "Field '" + f.displayName() + "' must be a date in YYYY-MM-DD format",
                    f.id()));
          }
        }
        default -> {
          // No type-specific constraints for SELECT, CHECKBOX, FILE — skip.
        }
      }
    }
    return errors;
  }

  /**
   * Story 5.6 AC2 — Per-field edit-permission check. Returns one {@link ErrorDetail} per
   * changed-field whose {@code editableBy} declaration excludes the actor's role set, OR per
   * changed-field with no {@code editableBy} when the case-type's {@code defaultFieldEditability}
   * is {@code locked-by-default}.
   *
   * <p>Field path uses {@code "fields." + fieldId} so the frontend can map the violation to the
   * specific input. Wire code is {@link ErrorCode#WKS_AUTHZ_FIELD} for every entry.
   *
   * <p>Unknown changed-field ids (i.e. submitted ids not declared on the case type) are silently
   * skipped — {@code WKS-FORM-002} is the dedicated wire code for unknown-field rejection and runs
   * earlier in {@code submitForm}.
   */
  static List<ErrorDetail> checkFieldEditPermissions(
      CaseTypeConfig caseType, Set<String> changedFieldIds, Set<String> actorRoles) {
    if (changedFieldIds.isEmpty()) {
      return List.of();
    }
    boolean lockedByDefault =
        caseType.defaultFieldEditability() == DefaultFieldEditability.LOCKED_BY_DEFAULT;
    List<ErrorDetail> violations = new ArrayList<>();
    for (String fieldId : changedFieldIds) {
      FieldDefinition field = caseType.field(fieldId).orElse(null);
      if (field == null) {
        continue; // unknown field — WKS-FORM-002 owns this rejection path
      }
      List<String> editableBy = field.editableBy();
      boolean allowed;
      String requiredDescription;
      if (editableBy.isEmpty()) {
        allowed = !lockedByDefault;
        requiredDescription = "(locked-by-default)";
      } else {
        Set<String> requiredRoles =
            editableBy.stream()
                .filter(s -> s.startsWith("role:"))
                .map(s -> s.substring("role:".length()))
                .collect(Collectors.toSet());
        allowed = requiredRoles.stream().anyMatch(actorRoles::contains);
        requiredDescription = requiredRoles.isEmpty() ? "(none)" : String.join(",", requiredRoles);
      }
      if (!allowed) {
        violations.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_AUTHZ_FIELD.wire(),
                "Field '"
                    + fieldId
                    + "' is editable only by roles: ["
                    + requiredDescription
                    + "] — actor lacks the required role",
                "fields." + fieldId));
      }
    }
    return violations;
  }

  /**
   * Coerce a value to {@link Double} for numeric constraint checks. Returns {@code null} if the
   * value is not numeric (e.g. a string that isn't a parseable number — the full type check runs
   * inside {@link CaseDataValidator}).
   */
  private static Double toDouble(Object value) {
    if (value instanceof Number n) return n.doubleValue();
    if (value instanceof String s) {
      try {
        return Double.parseDouble(s);
      } catch (NumberFormatException ignored) {
        return null;
      }
    }
    return null;
  }

  /** Lookup by id; 404 if not found. */
  public Case findById(UUID caseId) {
    return caseRepository
        .findById(caseId)
        .orElseThrow(() -> new WksNotFoundException("Case " + caseId + " not found"));
  }

  /**
   * Paginated list of case summaries scoped to a case type. After fetching the lightweight rows we
   * enrich {@code CaseSummary.fields} from {@code caseType.listColumns} via {@code findDataByIds}
   * (Story 2.3 D4) — keeps the wide JSON column out of the main projection query while still
   * delivering the listColumns values the wire contract promises.
   */
  public Page<CaseSummary> list(CaseQuery query, PageRequest pageRequest) {
    Page<CaseSummary> page = caseRepository.findByCaseType(query, pageRequest);
    if (page.content().isEmpty()) {
      return page;
    }
    CaseTypeConfig caseType = requireCaseType(query.caseTypeId());
    Set<String> projectedFieldIds = Set.copyOf(caseType.listColumns());
    if (projectedFieldIds.isEmpty()) {
      return page;
    }
    List<UUID> ids = page.content().stream().map(CaseSummary::id).toList();
    Map<UUID, Map<String, Object>> dataById = caseRepository.findDataByIds(ids, projectedFieldIds);
    List<CaseSummary> enriched = new ArrayList<>(page.content().size());
    for (CaseSummary s : page.content()) {
      enriched.add(
          new CaseSummary(
              s.id(),
              s.caseTypeId(),
              s.status(),
              s.assignee(),
              s.createdAt(),
              s.updatedAt(),
              dataById.getOrDefault(s.id(), Map.of())));
    }
    return new Page<>(enriched, page.total(), page.page(), page.size());
  }

  /**
   * Story 5.6 AC-0 — Single source of truth for case-level access on write-side controllers — do
   * NOT inline.
   *
   * <p>Sprint 8 retro Action 2. Loads the case via {@link CaseRepository#findById} and confirms the
   * principal has access. Phase-0 implementation: every authenticated principal has access — this
   * method is the seam where Phase-1 RBAC lands without controller surgery (replace the body of one
   * method, not edits across every controller).
   *
   * <p>Signature uses primitive {@code UUID actorId} + {@code Set<String> actorRoles} rather than
   * {@code WksUserPrincipal} to preserve the framework-free posture of the domain layer (the
   * principal type lives in {@code security/} and depends on Spring Security {@code UserDetails};
   * importing it here would couple the domain to Spring). Controllers extract id+roles from the
   * principal at the call site.
   *
   * @param caseId the case to load
   * @param actorId the authenticated caller's id (kept for future RBAC-evaluation hook; unused in
   *     Phase-0)
   * @param actorRoles the authenticated caller's role set (kept for future RBAC; unused in Phase-0)
   * @return the loaded {@link Case} so callers can avoid a duplicate {@code findById}
   * @throws WksNotFoundException if the case does not exist (preserves pre-5.6 wire shape — same
   *     exception, same message format used by FormController's downstream {@code submitForm} and
   *     by FormDraftController's removed {@code requireCaseExists} helper)
   */
  public Case requireCaseAccess(UUID caseId, UUID actorId, Set<String> actorRoles) {
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(actorId, "actorId");
    Objects.requireNonNull(actorRoles, "actorRoles");
    return caseRepository
        .findById(caseId)
        .orElseThrow(() -> new WksNotFoundException("Case " + caseId + " not found"));
    // Phase-0: no additional access check beyond authentication. Hook for Phase-1 RBAC.
  }

  /**
   * Story 5.5 AC-4 — Decision D20 frozen-on-version invariant. Single source of truth for "what
   * form does this case see right now." Controllers MUST consume this method — do NOT inline the
   * lookup (memory {@code feedback_consolidate_property_readers.md} analog applies).
   *
   * <ol>
   *   <li>Reads {@code case.caseTypeVersion()} (existing field on the Case record).
   *   <li>Looks up the CaseTypeConfig snapshot from the {@code case_type_versions} registry via
   *       {@link com.wkspower.platform.domain.port.CaseTypeReader#findVersion}.
   *   <li>Finds the {@link FormDefinition} by {@code formId} on the pinned CaseTypeConfig.
   * </ol>
   *
   * @throws WksVersionException ({@code WKS-VER-002}, HTTP 422) when the case's {@code
   *     caseTypeVersion} is not in the {@code case_type_versions} registry — defensive guard,
   *     should not fire on a healthy system, but discovered cases must not 500.
   * @throws WksNotFoundException ({@code WKS-API-404}, HTTP 404) when the {@code formId} is not
   *     declared on the pinned CaseTypeConfig (invalid request — form removed post-deploy).
   */
  public FormDefinition resolveFormDefinitionForCase(Case c, String formId) {
    Objects.requireNonNull(c, "case");
    Objects.requireNonNull(formId, "formId");
    CaseTypeConfig pinnedCaseType =
        caseTypeReader
            .findVersion(c.caseTypeId(), c.caseTypeVersion())
            .orElseThrow(
                () ->
                    new WksVersionException(
                        ErrorCode.WKS_VER_002,
                        "Case "
                            + c.id()
                            + " is pinned to CaseTypeVersion "
                            + c.caseTypeVersion()
                            + " which is not in the registry"));
    return pinnedCaseType.forms().stream()
        .filter(f -> f.id().equals(formId))
        .findFirst()
        .orElseThrow(
            () ->
                new WksNotFoundException(
                    "Form '"
                        + formId
                        + "' not found on case type '"
                        + c.caseTypeId()
                        + "' at version "
                        + c.caseTypeVersion()));
  }

  /**
   * Locate the case type — 404 if it does not exist. Used on create and update; the controller has
   * already returned 403 on missing permission verbs by the time this fires.
   */
  public CaseTypeConfig requireCaseType(String caseTypeId) {
    return caseTypeReader
        .find(caseTypeId)
        .orElseThrow(() -> new WksNotFoundException("Case type " + caseTypeId + " not found"));
  }

  /**
   * Initial status for a newly-created case. Resolution order (Story 6.2 Decision B — refines Story
   * 2.3 + gap-10 fix-a):
   *
   * <ol>
   *   <li>If the YAML declared top-level {@code statuses:} explicitly ({@code
   *       explicitTopLevelStatuses == true}), use {@code statuses[0].id}. Author-declared top-level
   *       statuses always win — they signal the author intended a flat lifecycle.
   *   <li>Else if any stage declares an {@code initialStatus}, use the FIRST stage's value (gap-10
   *       fix-a preserved). Stage-scoped case types whose top-level statuses are validator-
   *       injected {@code [open, closed]} defaults fall through to this branch and pick {@code
   *       "drafting"} (or whatever the first stage declares).
   *   <li>Else fall back to {@code statuses[0].id} (the injected default {@code "open"}). Reachable
   *       when stages exist but none declares an initialStatus AND the YAML omitted top-level
   *       statuses.
   *   <li>Else return {@code null} — only reachable when both stages and top-level statuses are
   *       empty, which ConfigValidator's defaults guard against in practice.
   * </ol>
   *
   * <p>Story 2.4's BPMN listener / Story 4.4b's ExecutionSignalRouter keep the status in sync with
   * engine state thereafter.
   */
  static String initialStatus(CaseTypeConfig caseType) {
    // Branch a — author-declared top-level statuses always win.
    if (caseType.explicitTopLevelStatuses() && !caseType.statuses().isEmpty()) {
      return caseType.statuses().get(0).id();
    }
    // Branch b — first stage's initialStatus (gap-10 fix-a; preserves bpmn-sequential-staged
    // behavior where top-level statuses are ConfigValidator-injected defaults).
    for (var stage : caseType.stages()) {
      Optional<String> stageInitial = stage.initialStatus();
      if (stageInitial.isPresent()) {
        return stageInitial.get();
      }
    }
    // Branch c — fall back to top-level statuses (which may be the injected default).
    if (!caseType.statuses().isEmpty()) {
      return caseType.statuses().get(0).id();
    }
    // Branch d — neither stages nor top-level statuses available.
    return null;
  }

  /**
   * Set of field ids whose value differs between {@code oldData} and {@code newData}. A field
   * present on one side but absent from the other counts as changed.
   */
  static Set<String> diffFieldIds(Map<String, Object> oldData, Map<String, Object> newData) {
    Set<String> changed = new HashSet<>();
    Set<String> allKeys = new HashSet<>(oldData.keySet());
    allKeys.addAll(newData.keySet());
    for (String key : allKeys) {
      Object o = oldData.get(key);
      Object n = newData.get(key);
      if (!Objects.equals(o, n)) {
        changed.add(key);
      }
    }
    return changed;
  }
}
