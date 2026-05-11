package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.OutcomeMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.SignalMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.event.ExecutionSignalRouted;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksMappingMissException;
import com.wkspower.platform.domain.model.AuditSource;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.RecentSignalEntry;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Story 4.3 — single routing surface for every {@link ExecutionSignal} emitted by any {@code
 * WorkflowAdapter} (Story 4.1). Architecture Decision 22 — the Mapping Layer is the only seam
 * between WKS primitives and any execution backend; this router is the runtime half (Story 4.2's
 * {@code MappingDefinition} is the deploy-time half).
 *
 * <p><b>Single-subscriber invariant (AC1, AC6):</b> production wiring registers exactly one {@link
 * ExecutionSignalHandler} per adapter — this class. ArchUnit guardrail {@code
 * WorkflowAdapterPortIsolationTest} enforces the rule at build time. Adding a second subscriber is
 * a deliberate, reviewer-visible surface change.
 *
 * <p><b>No router-side reordering (AC2):</b> {@link ExecutionSignalKind}'s precedence ordering is a
 * <b>mapping-author</b> guarantee — Story 4.2's validator emits {@code WKS-MAP-002} when two rules
 * from different kinds target the same {@code (stage, status)} without explicit precedence. The
 * router processes signals in the order the adapter emits them. Do <b>not</b> add a priority queue
 * here.
 *
 * <p><b>Transaction model (AC8):</b> the router runs inside whatever transactional context the
 * adapter provides (BPMN: engine transaction; in-memory state machine: adapter-defined). Stage and
 * status mutations propagate exceptions to roll the engine state back together with the WKS write.
 * Only {@link WksMappingMissException} is caught at the router boundary and converted to an
 * audit-only event — every other exception is rethrown.
 *
 * <p><b>Audit attribution (AC5 / FR8):</b> every routed signal — success or miss — emits a {@link
 * ExecutionSignalRouted} event via {@link EventPublisher#publishAfterCommit} carrying a typed
 * {@link AuditSource.Backend} whose {@code toString()} renders to {@code "backend(<adapterName>)"}.
 * Existing string-based {@code source} columns (Stage.source, StageEntered.source, etc.) keep their
 * bare {@code "backend-signal"} value — migration is folded into Story 4.4 per {@code
 * feedback_fold_debt_into_stories.md}.
 *
 * <p>Pure-Java, framework-free (NFR36) — wired as a {@code @Bean} from {@code
 * infrastructure.config.WorkflowAdapterConfig}.
 */
public class ExecutionSignalRouter implements ExecutionSignalHandler {

  private static final Logger log = LoggerFactory.getLogger(ExecutionSignalRouter.class);

  /** Terminal stage markers per AC2 — invoke case-level lifecycle (advance through last stage). */
  private static final String COMPLETED = "completed";

  private static final String SKIPPED = "skipped";

  /** Legacy {@code source} string for {@link WksStageAdvancer} call sites — Decision 1. */
  private static final String LEGACY_BACKEND_SOURCE = "backend-signal";

  private final MappingRegistry mappingRegistry;
  private final WksStageAdvancer stageAdvancer;
  private final CaseStatusUpdater statusUpdater;
  private final CaseRepository caseRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;
  // Story 4.4b AC3 — CaseTypeReader for post-stage-advance status-reset to next stage's
  // initialStatus. Injected across the hexagonal boundary (Story 3-6 §AC6 deferred-work landing).
  private final CaseTypeReader caseTypeReader;
  // Story 4.6 AC5 — best-effort additive instrumentation. Optional so legacy / non-Spring
  // callers (existing IT fakes) can construct the router without supplying a buffer.
  private final SignalAuditRingBuffer signalAuditRingBuffer;

  public ExecutionSignalRouter(
      MappingRegistry mappingRegistry,
      WksStageAdvancer stageAdvancer,
      CaseStatusUpdater statusUpdater,
      CaseRepository caseRepository,
      EventPublisher eventPublisher,
      Clock clock,
      CaseTypeReader caseTypeReader) {
    this(
        mappingRegistry,
        stageAdvancer,
        statusUpdater,
        caseRepository,
        eventPublisher,
        clock,
        caseTypeReader,
        null);
  }

  /**
   * Story 4.6 AC5 — overload accepting the {@link SignalAuditRingBuffer}. When non-null, every
   * audit-emit branch additionally records a {@link RecentSignalEntry} into the buffer. Recording
   * is best-effort and exception-suppressed: an audit-side failure never aborts canonical routing.
   */
  public ExecutionSignalRouter(
      MappingRegistry mappingRegistry,
      WksStageAdvancer stageAdvancer,
      CaseStatusUpdater statusUpdater,
      CaseRepository caseRepository,
      EventPublisher eventPublisher,
      Clock clock,
      CaseTypeReader caseTypeReader,
      SignalAuditRingBuffer signalAuditRingBuffer) {
    this.mappingRegistry = Objects.requireNonNull(mappingRegistry, "mappingRegistry");
    this.stageAdvancer = Objects.requireNonNull(stageAdvancer, "stageAdvancer");
    this.statusUpdater = Objects.requireNonNull(statusUpdater, "statusUpdater");
    this.caseRepository = Objects.requireNonNull(caseRepository, "caseRepository");
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.caseTypeReader = Objects.requireNonNull(caseTypeReader, "caseTypeReader");
    // Optional — may be null in legacy callers.
    this.signalAuditRingBuffer = signalAuditRingBuffer;
  }

  @Override
  public void onSignal(ExecutionSignal signal) {
    Objects.requireNonNull(signal, "signal");
    Optional<Case> caseOpt = caseRepository.findById(signal.caseInstance().id());
    if (caseOpt.isEmpty()) {
      // Story 4.3.1 AC5 — case-not-found is now an audit-emitting branch (WKS-MAP-405). The
      // CaseInstanceRef carries caseTypeId + version, so we can synthesise a ExecutionSignalRouted
      // event for operator observability rather than silently dropping. Distinct from WKS-MAP-404
      // (rule miss): the case row itself is gone, so the rule lookup never ran.
      String caseTypeVersion =
          signal.caseInstance().caseType() == null
              ? "?"
              : signal.caseInstance().caseType().version();
      String caseTypeId =
          signal.caseInstance().caseType() == null
              ? "?"
              : signal.caseInstance().caseType().caseTypeId();
      log.atWarn()
          .addKeyValue("wksErrorCode", ErrorCode.WKS_MAP_405.wire())
          .addKeyValue("originAdapter", signal.adapterName())
          .addKeyValue("caseId", signal.caseInstance().id().toString())
          .log("backend signal targets unknown caseId — case not found");
      Map<String, String> detail = new HashMap<>();
      detail.put("originAdapter", signal.adapterName());
      detail.put("reason", "case not found in repository");
      eventPublisher.publishAfterCommit(
          new ExecutionSignalRouted(
              signal.caseInstance().id(),
              caseTypeId,
              caseTypeVersion,
              signal.kind(),
              signal.source(),
              new AuditSource.Backend(signal.adapterName()),
              ErrorCode.WKS_MAP_405.wire(),
              detail,
              clock.now()));
      // Story 4.6 AC5 — case-not-found audit-emit branch.
      recordAudit(
          caseTypeId,
          new RecentSignalEntry(
              clock.now(),
              signal.kind(),
              signal.source(),
              "case-not-found",
              null,
              null,
              signal.caseInstance().id(),
              ErrorCode.WKS_MAP_405.wire()));
      return;
    }
    Case caseRow = caseOpt.get();

    String caseTypeVersion = String.valueOf(caseRow.caseTypeVersion());
    CaseTypeRef pinned = new CaseTypeRef(caseRow.caseTypeId(), caseTypeVersion);

    Optional<MappingDefinition> mappingOpt = mappingRegistry.resolve(pinned, caseTypeVersion);
    if (mappingOpt.isEmpty()) {
      // AC9 — fail-loud, never fall back to "latest version" silently.
      auditMiss(signal, caseRow, "caseTypeVersion not in registry");
      return;
    }

    try {
      // Story 4.4b AC3 — stageAdvance dispatch methods return true when a stage-transition was
      // applied. In that case resetStatusForAdvancedStage already emits two ExecutionSignalRouted
      // events (effect=stage-advance + effect=status-reset) with a shared correlationId; the
      // standard auditSuccess call must be skipped to avoid emitting a third redundant event.
      boolean stageAdvanceHandled = false;
      switch (signal.kind()) {
        case STAGE_TRANSITION ->
            stageAdvanceHandled = dispatchEndEvent(signal, caseRow, mappingOpt.get());
        case NAMED_SIGNAL ->
            stageAdvanceHandled = dispatchNamedSignal(signal, caseRow, mappingOpt.get());
          // Story 4.3.1 AC10 — split USER_TASK_PROPERTY into TASK_STATUS_CHANGED + TASK_COMPLETED.
          // Both inbound kinds dispatch through the same property-emission lookup; the rule's emits
          // value distinguishes "drives status transition" (TASK_STATUS_CHANGED) from
          // "drives stage advance" (TASK_COMPLETED).
        case TASK_STATUS_CHANGED, TASK_COMPLETED ->
            stageAdvanceHandled = dispatchUserTaskProperty(signal, caseRow, mappingOpt.get());
        case OUTCOME ->
            // Story 6.2 AC2 — multi-outcome routing via mapping layer.
            stageAdvanceHandled = dispatchOutcome(signal, caseRow, mappingOpt.get());
      }
      if (!stageAdvanceHandled) {
        auditSuccess(signal, caseRow);
      }
    } catch (WksMappingMissException miss) {
      auditMiss(signal, caseRow, miss.getMessage());
      // AC4: caught at router boundary; do NOT propagate to the adapter.
    } catch (RuntimeException other) {
      // Story 4.3.1 AC4 — synchronous failure-audit emit BEFORE the rollback rethrow. Operator
      // observability of failed dispatches (transaction rollback path) is the load-bearing
      // contract advertised by Story 4.3 AC8. publishAfterCommit would never land if the outer
      // transaction rolls back; we publish synchronously via {@code publish} so the audit row is
      // visible irrespective of the engine's outcome. The exception is rethrown so the engine
      // sees the failure and rolls its own state back.
      Map<String, String> detail = new HashMap<>();
      detail.put("originAdapter", signal.adapterName());
      detail.put("reason", "dispatch failed: " + other.getClass().getSimpleName());
      String exceptionCode = extractWksErrorCode(other);
      log.atWarn()
          .addKeyValue("wksErrorCode", exceptionCode == null ? "WKS-RTM-500" : exceptionCode)
          .addKeyValue("originAdapter", signal.adapterName())
          .addKeyValue("caseId", caseRow.id().toString())
          .addKeyValue("kind", signal.kind().name())
          .addKeyValue("signalSource", signal.source())
          .log("backend signal dispatch failed — rolling back: {}", other.getMessage());
      eventPublisher.publish(
          new ExecutionSignalRouted(
              caseRow.id(),
              caseRow.caseTypeId(),
              caseTypeVersion,
              signal.kind(),
              signal.source(),
              new AuditSource.Backend(signal.adapterName()),
              exceptionCode == null ? "WKS-RTM-500" : exceptionCode,
              detail,
              clock.now()));
      throw other;
    }
  }

  /**
   * Best-effort extraction of a WKS error code from a domain exception. Domain exceptions in the
   * stage band ({@code WksStageException}) and friends carry a wire-string code field; we look it
   * up reflectively via a no-arg {@code wksErrorCode()} or {@code errorCode()} accessor so the
   * router stays decoupled from each domain exception class. Returns {@code null} when no code
   * surface is available — the caller defaults to {@code WKS-RTM-500}.
   */
  private static String extractWksErrorCode(RuntimeException ex) {
    for (String name : new String[] {"errorCode", "wksErrorCode", "code"}) {
      try {
        var m = ex.getClass().getMethod(name);
        Object v = m.invoke(ex);
        if (v != null) {
          String s = v.toString();
          // Only accept values that conform to the WKS wire-code shape so that unrelated
          // accessors (e.g. HttpStatusCode.code(), TransactionSystemException.toString()) are
          // never published as audit wire codes.
          if (s.startsWith("WKS-")) {
            return s;
          }
        }
      } catch (ReflectiveOperationException ignored) {
        // try next accessor name
      }
    }
    return null;
  }

  // ---- per-kind dispatch -------------------------------------------------

  /**
   * Returns {@code true} when a stage transition was applied (audit events emitted inside {@link
   * #applyStageTransition}); {@code false} if a {@link WksMappingMissException} is thrown before
   * any transition (never actually returns false — exception propagates instead).
   */
  private boolean dispatchEndEvent(
      ExecutionSignal signal, Case caseRow, MappingDefinition mapping) {
    EndEventMapping rule =
        firstAttachment(mapping)
            .flatMap(AttachmentDefinition::endEventMapping)
            .orElseThrow(
                () ->
                    new WksMappingMissException(
                        signal.adapterName(),
                        signal.caseInstance().id(),
                        "no endEvent rule in mapping"));
    applyStageTransition(signal, caseRow, rule.stageTransition());
    return true;
  }

  /**
   * Returns {@code true} when a stage transition was applied; {@code false} never actually returned
   * — exception propagates on miss.
   */
  private boolean dispatchNamedSignal(
      ExecutionSignal signal, Case caseRow, MappingDefinition mapping) {
    SignalMapping rule =
        firstAttachment(mapping)
            .map(a -> a.signalMappings().get(signal.source()))
            .orElseThrow(
                () ->
                    new WksMappingMissException(
                        signal.adapterName(),
                        signal.caseInstance().id(),
                        "no signal rule for source '" + signal.source() + "'"));
    if (rule == null) {
      throw new WksMappingMissException(
          signal.adapterName(),
          signal.caseInstance().id(),
          "no signal rule for source '" + signal.source() + "'");
    }
    applyStageTransition(signal, caseRow, rule.stageTransition());
    return true;
  }

  /**
   * Story 6.2 AC2 / AC5 — multi-outcome routing dispatch. Resolves the outcome key from the signal
   * payload, looks up the matching {@link OutcomeMapping} rule from the first attachment, and
   * delegates to {@link #applyStageTransition} (which emits the two-event {@code stage-advance} +
   * {@code status-reset} pair with a shared correlationId).
   *
   * <p>On miss: throws {@link WksMappingMissException} carrying {@code WKS-MAP-404} BEFORE any
   * mutation — the router's existing {@code auditMiss} path handles the audit row emission.
   *
   * <p>The {@code source = backend(formOutcome)} discrimination reuses the existing {@link
   * AuditSource.Backend} with {@code adapterName = signal.adapterName()} — the frontend dispatcher
   * sets adapterName to {@code "formOutcome"} for outcome signals so the two-row audit contract
   * (form-row source=form, effect-row source=backend(formOutcome)) is satisfied without introducing
   * a new AuditSource variant. Document in PR.
   *
   * @return {@code true} always (stage-advance handled — caller skips {@code auditSuccess}).
   */
  private boolean dispatchOutcome(ExecutionSignal signal, Case caseRow, MappingDefinition mapping) {
    // Read the outcome key from the signal payload.
    Object raw = signal.payload().get("outcome");
    if (!(raw instanceof String outcomeKey) || ((String) raw).isBlank()) {
      throw new WksMappingMissException(
          signal.adapterName(),
          signal.caseInstance().id(),
          "outcome dispatch missing scalar 'outcome' in payload");
    }

    // Resolve the rule from the first attachment's outcomeMappings.
    OutcomeMapping rule =
        firstAttachment(mapping)
            .map(a -> a.outcomeMappings().get(outcomeKey))
            .orElseThrow(
                () ->
                    new WksMappingMissException(
                        signal.adapterName(),
                        signal.caseInstance().id(),
                        "no outcome rule for key '" + outcomeKey + "'"));
    if (rule == null) {
      throw new WksMappingMissException(
          signal.adapterName(),
          signal.caseInstance().id(),
          "no outcome rule for key '" + outcomeKey + "'");
    }

    // Apply the stage transition (emits stage-advance + status-reset pair with shared
    // correlationId).
    applyStageTransition(signal, caseRow, rule.stageTransition());
    return true; // stage-advance handled — caller must NOT call auditSuccess.
  }

  /**
   * Returns {@code true} when a stage advance was applied (TASK_COMPLETED path); {@code false} when
   * a status-only update was performed (TASK_STATUS_CHANGED path). The caller uses this to decide
   * whether to emit the standard single-event {@code auditSuccess} or skip it (because {@link
   * #resetStatusForAdvancedStage} already emitted two events).
   */
  private boolean dispatchUserTaskProperty(
      ExecutionSignal signal, Case caseRow, MappingDefinition mapping) {
    String onKey = "userTask:" + signal.source();
    PropertyEmissionRule rule =
        firstAttachment(mapping).stream()
            .flatMap(a -> a.propertyEmissionRules().stream())
            .filter(r -> onKey.equals(r.on()))
            .findFirst()
            .orElseThrow(
                () ->
                    new WksMappingMissException(
                        signal.adapterName(),
                        signal.caseInstance().id(),
                        "no property rule for on='" + onKey + "'"));

    if (rule.emits() == ExecutionSignalKind.TASK_COMPLETED) {
      // Story 4.3.1 AC10 — task-complete kind carries no status value; advance the stage forward
      // so the BPMN end-of-task drives case progression without requiring an explicit status
      // property on the userTask.
      // Story 4.4b AC3 — also resets status to next stage's initialStatus and emits two events.
      String sourceRef = signal.adapterName() + ":" + signal.source();
      stageAdvancer.advance(caseRow.id(), LEGACY_BACKEND_SOURCE, sourceRef);
      // nextStageHint is null for advance() — next stage determined by ordinal+1 inside advancer.
      resetStatusForAdvancedStage(signal, caseRow, null);
      return true; // stage advance handled — caller must NOT call auditSuccess.
    }

    if (rule.emits() != ExecutionSignalKind.TASK_STATUS_CHANGED) {
      throw new WksMappingMissException(
          signal.adapterName(),
          signal.caseInstance().id(),
          "userTaskProperty cannot drive stage transition");
    }

    Object value = signal.payload().get("value");
    if (!(value instanceof String newStatus) || ((String) value).isBlank()) {
      throw new WksMappingMissException(
          signal.adapterName(),
          signal.caseInstance().id(),
          "userTask property emission missing scalar 'value' in payload");
    }
    statusUpdater.updateStatus(caseRow.id(), newStatus);
    return false; // status-only update — caller emits standard auditSuccess.
  }

  // ---- helpers -----------------------------------------------------------

  /**
   * Phase-0 simplification — a CaseType has at most one attachment by AC of Story 4.2 ({@code
   * WKS-MAP-006} forbids duplicate scopes). Multi-attachment routing (per-stage attachments) lands
   * with Story 4.5; today the router consults the single declared attachment.
   */
  private static Optional<AttachmentDefinition> firstAttachment(MappingDefinition mapping) {
    return mapping.attachments().isEmpty()
        ? Optional.empty()
        : Optional.of(mapping.attachments().get(0));
  }

  /**
   * Story 4.4b AC3 — stage transition with post-advance status-reset. After the stage advance
   * succeeds, resolves the next stage's {@code initialStatus} via {@link CaseTypeReader} and resets
   * the case status. Emits TWO {@link ExecutionSignalRouted} events with a shared {@code
   * correlationId} UUID: one {@code effect=stage-advance}, one {@code effect=status-reset}. This is
   * an intentional divergence from the pre-4.4b one-event shape — rationale: per Q3 lock, aligns
   * with Epic 9 Activity Feed semantics and {@code EventPublisher.publishAfterCommit}
   * one-event-per-effect convention.
   *
   * <p>The {@code to} target stage is determined from the spec BEFORE calling the advancer, so
   * {@link #resetStatusForAdvancedStage} can use it directly without re-reading from the DB
   * (avoiding JPA first-level-cache staleness issues in transactional test contexts).
   */
  private void applyStageTransition(ExecutionSignal signal, Case caseRow, String spec) {
    String[] parts = spec.split("\\s*->\\s*");
    if (parts.length != 2 || parts[1].isBlank()) {
      throw new WksMappingMissException(
          signal.adapterName(),
          signal.caseInstance().id(),
          "malformed stageTransition '" + spec + "'");
    }
    // Story 6.2 — WKS-MAP-404: reject a transition spec whose source stage segment is blank
    // (e.g. "-> review", " -> ", "-> "). MappingValidator's stageTransition regex anchors the
    // grammar at deploy time, but a malformed spec sneaking through (admin REST PATCH, runtime
    // hot-reload race) must not be silently treated as "from anywhere".
    if (parts[0].isBlank()) {
      throw new WksMappingMissException(
          signal.adapterName(),
          signal.caseInstance().id(),
          "source stage missing in transition spec '" + spec + "'");
    }
    String to = parts[1].trim();
    String sourceRef = signal.adapterName() + ":" + signal.source();
    // The advancer determines the actual next stage (null = last stage completed on advance()).
    // For skipTo, the target is explicit. For COMPLETED/SKIPPED, advance() moves to ordinal+1.
    // We pre-capture nextStageId for the status-reset: for skipTo it's `to`; for advance it's
    // resolved by the advancer. We pass the spec's `to` as a hint — null means "completed".
    String nextStageHint;
    if (COMPLETED.equals(to) || SKIPPED.equals(to)) {
      // AC2 — case-level lifecycle: advance through last stage.
      stageAdvancer.advance(caseRow.id(), LEGACY_BACKEND_SOURCE, sourceRef);
      // nextStage hint: the advancer knows ordinal+1 but we don't without a separate query.
      // Use null to trigger a DB re-read path; for non-H2 callers this is consistent.
      nextStageHint = null;
    } else {
      // skipTo handles forward-by-one as equivalent to advance (Story 3.1 AC6).
      stageAdvancer.skipTo(caseRow.id(), to, LEGACY_BACKEND_SOURCE, sourceRef);
      nextStageHint = to; // explicit target — no re-read needed.
    }
    // Story 4.4b AC3 — after stage advance, reset currentStatusId to the next stage's
    // initialStatus. Pass the pre-determined nextStageHint to avoid stale first-level-cache reads.
    resetStatusForAdvancedStage(signal, caseRow, nextStageHint);
  }

  /**
   * Story 4.4b AC3 — resets the case status to the next stage's {@code initialStatus} after a stage
   * advance. Emits two {@link ExecutionSignalRouted} events with a shared {@code correlationId}:
   * one for the stage-advance effect, one for the status-reset effect.
   *
   * @param nextStageHint the target stage id known before the advance (from the mapping spec), or
   *     {@code null} when the advance target is determined by the advancer (COMPLETED/SKIPPED
   *     paths). When non-null this avoids a JPA first-level-cache staleness issue in transactional
   *     contexts.
   */
  private void resetStatusForAdvancedStage(
      ExecutionSignal signal, Case preMutationRow, String nextStageHint) {
    UUID correlationId = UUID.randomUUID();
    AuditSource auditSource = new AuditSource.Backend(signal.adapterName());
    String caseTypeVersion = String.valueOf(preMutationRow.caseTypeVersion());

    // Emit stage-advance audit (effect = stage-advance).
    Map<String, String> stageAdvanceDetail = new HashMap<>();
    stageAdvanceDetail.put("effect", "stage-advance");
    stageAdvanceDetail.put("correlationId", correlationId.toString());
    eventPublisher.publishAfterCommit(
        new ExecutionSignalRouted(
            preMutationRow.id(),
            preMutationRow.caseTypeId(),
            caseTypeVersion,
            signal.kind(),
            signal.source(),
            auditSource,
            null,
            stageAdvanceDetail,
            clock.now()));

    // Determine the next stage id: use the hint when available (avoids DB re-read), otherwise
    // fall back to reading the post-advance case row from the repository.
    final String nextStageId;
    if (nextStageHint != null) {
      nextStageId = nextStageHint;
    } else {
      // COMPLETED/SKIPPED path: re-read from repo (advance() determined the next ordinal).
      Optional<Case> postAdvanceOpt = caseRepository.findById(preMutationRow.id());
      if (postAdvanceOpt.isEmpty()) {
        return;
      }
      nextStageId = postAdvanceOpt.get().currentStageId();
    }

    if (nextStageId == null) {
      // Reached the end (last stage completed) — no status reset needed.
      return;
    }

    // Resolve the CaseTypeConfig for this pinned version to get the next stage's status set.
    Optional<CaseTypeConfig> caseTypeOpt =
        caseTypeReader.findVersion(preMutationRow.caseTypeId(), preMutationRow.caseTypeVersion());
    if (caseTypeOpt.isEmpty()) {
      log.atWarn()
          .addKeyValue("caseTypeId", preMutationRow.caseTypeId())
          .addKeyValue("caseTypeVersion", preMutationRow.caseTypeVersion())
          .log(
              "4.4b AC3: could not resolve CaseTypeConfig for status-reset after stage advance"
                  + " — status not reset");
      return;
    }
    CaseTypeConfig caseType = caseTypeOpt.get();
    List<? extends com.wkspower.platform.domain.config.model.StatusDefinition> nextStageStatuses =
        caseType.statusesFor(nextStageId);
    if (nextStageStatuses.isEmpty()) {
      // Story 6.2 — WKS-STAT-001: stage advance succeeded but next stage declares no statuses
      // AND no top-level fallback applies. The case's existing status persists across the stage
      // boundary (stale). WARN so the operator can investigate the missing declaration.
      log.atWarn()
          .addKeyValue("wksErrorCode", ErrorCode.WKS_STAT_001.wire())
          .addKeyValue("caseId", preMutationRow.id().toString())
          .addKeyValue("oldStatus", preMutationRow.status())
          .addKeyValue("nextStageId", nextStageId)
          .log(
              "stage advance left case status stale at {}; next stage {} declares no statuses",
              preMutationRow.status(),
              nextStageId);
      return;
    }
    String initialStatus = nextStageStatuses.get(0).id();

    // Apply the status reset.
    statusUpdater.updateStatus(preMutationRow.id(), initialStatus);

    // Emit status-reset audit (effect = status-reset).
    Map<String, String> statusResetDetail = new HashMap<>();
    statusResetDetail.put("effect", "status-reset");
    statusResetDetail.put("correlationId", correlationId.toString());
    statusResetDetail.put("newStatus", initialStatus);
    statusResetDetail.put("nextStageId", nextStageId);
    eventPublisher.publishAfterCommit(
        new ExecutionSignalRouted(
            preMutationRow.id(),
            preMutationRow.caseTypeId(),
            caseTypeVersion,
            signal.kind(),
            signal.source(),
            auditSource,
            null,
            statusResetDetail,
            clock.now()));
  }

  private void auditSuccess(ExecutionSignal signal, Case caseRow) {
    publish(signal, caseRow, new AuditSource.Backend(signal.adapterName()), null, Map.of());
    // Story 4.6 AC5 — matched-rule success audit-emit branch.
    recordAudit(
        caseRow.caseTypeId(),
        new RecentSignalEntry(
            clock.now(),
            signal.kind(),
            signal.source(),
            "matched-rule",
            null,
            describeEffect(signal),
            caseRow.id(),
            null));
  }

  private void auditMiss(ExecutionSignal signal, Case caseRow, String reason) {
    Map<String, String> detail = new HashMap<>();
    detail.put("originAdapter", signal.adapterName());
    if (reason != null) {
      detail.put("reason", reason);
    }
    log.atWarn()
        .addKeyValue("wksErrorCode", ErrorCode.WKS_MAP_404.wire())
        .addKeyValue("originAdapter", signal.adapterName())
        .addKeyValue("caseId", signal.caseInstance().id().toString())
        .addKeyValue("kind", signal.kind().name())
        .addKeyValue("signalSource", signal.source())
        .log("backend signal unmapped: {}", reason);
    // Story 4.3.1 AC6 — un-spoofable miss-sentinel via AuditSource.ExecutionUnmapped. An adapter
    // named "unmapped" cannot collide with the miss audit string because the sub-record renders
    // distinctly as execution(unmapped:<originAdapter>).
    publish(
        signal,
        caseRow,
        new AuditSource.ExecutionUnmapped(signal.adapterName()),
        ErrorCode.WKS_MAP_404.wire(),
        detail);
    // Story 4.6 AC5 — unmapped audit-emit branch (covers both
    // "caseTypeVersion not in registry" and WksMappingMissException paths).
    String decision =
        reason != null && reason.contains("registry") ? "version-not-registered" : "unmapped";
    recordAudit(
        caseRow.caseTypeId(),
        new RecentSignalEntry(
            clock.now(),
            signal.kind(),
            signal.source(),
            decision,
            null,
            null,
            caseRow.id(),
            ErrorCode.WKS_MAP_404.wire()));
  }

  /**
   * Story 4.6 AC5 — best-effort recording into the in-memory ring buffer. Wrapped in try/catch so
   * an audit-side failure can never abort canonical routing. The buffer reference may be {@code
   * null} in legacy / non-Spring callers (existing IT fakes); the method is a no-op in that case.
   */
  private void recordAudit(String caseTypeId, RecentSignalEntry entry) {
    if (signalAuditRingBuffer == null) {
      return;
    }
    try {
      signalAuditRingBuffer.record(caseTypeId, entry);
    } catch (RuntimeException e) {
      log.warn("SignalAuditRingBuffer.record failed — non-fatal", e);
    }
  }

  private static String describeEffect(ExecutionSignal signal) {
    return switch (signal.kind()) {
      case STAGE_TRANSITION -> "stageTransition";
      case NAMED_SIGNAL -> "stageTransition";
      case TASK_STATUS_CHANGED -> "statusChange";
      case TASK_COMPLETED -> "taskCompleted";
      case OUTCOME -> "outcome";
    };
  }

  private void publish(
      ExecutionSignal signal,
      Case caseRow,
      AuditSource source,
      String errorCode,
      Map<String, String> detail) {
    String caseTypeVersion = String.valueOf(caseRow.caseTypeVersion());
    eventPublisher.publishAfterCommit(
        new ExecutionSignalRouted(
            caseRow.id(),
            caseRow.caseTypeId(),
            caseTypeVersion,
            signal.kind(),
            signal.source(),
            source,
            errorCode,
            detail,
            clock.now()));
  }
}
