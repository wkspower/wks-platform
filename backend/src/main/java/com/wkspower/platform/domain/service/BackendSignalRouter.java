package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.SignalMapping;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.event.BackendSignalRouted;
import com.wkspower.platform.domain.exception.WksMappingMissException;
import com.wkspower.platform.domain.model.AuditSource;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.BackendSignal;
import com.wkspower.platform.domain.port.BackendSignalHandler;
import com.wkspower.platform.domain.port.BackendSignalKind;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Story 4.3 — single routing surface for every {@link BackendSignal} emitted by any {@code
 * BackendAdapter} (Story 4.1). Architecture Decision 22 — the Mapping Layer is the only seam
 * between WKS primitives and any execution backend; this router is the runtime half (Story 4.2's
 * {@code MappingDefinition} is the deploy-time half).
 *
 * <p><b>Single-subscriber invariant (AC1, AC6):</b> production wiring registers exactly one {@link
 * BackendSignalHandler} per adapter — this class. ArchUnit guardrail {@code
 * BackendAdapterPortIsolationTest} enforces the rule at build time. Adding a second subscriber is a
 * deliberate, reviewer-visible surface change.
 *
 * <p><b>No router-side reordering (AC2):</b> {@link BackendSignalKind}'s precedence ordering is a
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
 * BackendSignalRouted} event via {@link EventPublisher#publishAfterCommit} carrying a typed {@link
 * AuditSource.Backend} whose {@code toString()} renders to {@code "backend(<adapterName>)"}.
 * Existing string-based {@code source} columns (Stage.source, StageEntered.source, etc.) keep their
 * bare {@code "backend-signal"} value — migration is folded into Story 4.4 per {@code
 * feedback_fold_debt_into_stories.md}.
 *
 * <p>Pure-Java, framework-free (NFR36) — wired as a {@code @Bean} from {@code
 * infrastructure.config.BackendAdapterConfig}.
 */
public class BackendSignalRouter implements BackendSignalHandler {

  private static final Logger log = LoggerFactory.getLogger(BackendSignalRouter.class);

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

  public BackendSignalRouter(
      MappingRegistry mappingRegistry,
      WksStageAdvancer stageAdvancer,
      CaseStatusUpdater statusUpdater,
      CaseRepository caseRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.mappingRegistry = Objects.requireNonNull(mappingRegistry, "mappingRegistry");
    this.stageAdvancer = Objects.requireNonNull(stageAdvancer, "stageAdvancer");
    this.statusUpdater = Objects.requireNonNull(statusUpdater, "statusUpdater");
    this.caseRepository = Objects.requireNonNull(caseRepository, "caseRepository");
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  @Override
  public void onSignal(BackendSignal signal) {
    Objects.requireNonNull(signal, "signal");
    Optional<Case> caseOpt = caseRepository.findById(signal.caseInstance().id());
    if (caseOpt.isEmpty()) {
      // No case row to audit against — log structured warning only; the audit-row event requires
      // a caseTypeId/version we do not have. This branch is defensive: the adapter should never
      // emit a signal for a non-existent case in production, since the case lifecycle is the
      // adapter's parent.
      log.atWarn()
          .addKeyValue("wksErrorCode", "WKS-MAP-404")
          .addKeyValue("originAdapter", signal.adapterName())
          .addKeyValue("caseId", signal.caseInstance().id().toString())
          .log("backend signal targets unknown caseId — dropped");
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
      switch (signal.kind()) {
        case END_EVENT -> dispatchEndEvent(signal, caseRow, mappingOpt.get());
        case NAMED_SIGNAL -> dispatchNamedSignal(signal, caseRow, mappingOpt.get());
        case USER_TASK_PROPERTY -> dispatchUserTaskProperty(signal, caseRow, mappingOpt.get());
        case OUTCOME ->
            // Phase-1 reservation per AC2.
            throw new WksMappingMissException(
                signal.adapterName(), signal.caseInstance().id(), "outcome routing is Phase-1");
      }
      auditSuccess(signal, caseRow);
    } catch (WksMappingMissException miss) {
      auditMiss(signal, caseRow, miss.getMessage());
      // AC4: caught at router boundary; do NOT propagate to the adapter.
    }
  }

  // ---- per-kind dispatch -------------------------------------------------

  private void dispatchEndEvent(BackendSignal signal, Case caseRow, MappingDefinition mapping) {
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
  }

  private void dispatchNamedSignal(BackendSignal signal, Case caseRow, MappingDefinition mapping) {
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
  }

  private void dispatchUserTaskProperty(
      BackendSignal signal, Case caseRow, MappingDefinition mapping) {
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

    // D22 clarification of Story 2.9 ambiguity — userTask property emission is restricted to
    // status transitions within a stage. Only USER_TASK_PROPERTY emissions drive status writes;
    // any rule whose emits is not USER_TASK_PROPERTY is treated as a stage transition attempt and
    // rejected.
    if (rule.emits() != BackendSignalKind.USER_TASK_PROPERTY) {
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

  private void applyStageTransition(BackendSignal signal, Case caseRow, String spec) {
    String[] parts = spec.split("\\s*->\\s*");
    if (parts.length != 2 || parts[1].isBlank()) {
      throw new WksMappingMissException(
          signal.adapterName(),
          signal.caseInstance().id(),
          "malformed stageTransition '" + spec + "'");
    }
    String to = parts[1].trim();
    String sourceRef = signal.adapterName() + ":" + signal.source();
    if (COMPLETED.equals(to) || SKIPPED.equals(to)) {
      // AC2 — case-level lifecycle: advance through last stage.
      stageAdvancer.advance(caseRow.id(), LEGACY_BACKEND_SOURCE, sourceRef);
      return;
    }
    // skipTo handles forward-by-one as equivalent to advance (Story 3.1 AC6).
    stageAdvancer.skipTo(caseRow.id(), to, LEGACY_BACKEND_SOURCE, sourceRef);
  }

  private void auditSuccess(BackendSignal signal, Case caseRow) {
    publish(signal, caseRow, new AuditSource.Backend(signal.adapterName()), null, Map.of());
  }

  private void auditMiss(BackendSignal signal, Case caseRow, String reason) {
    Map<String, String> detail = new HashMap<>();
    detail.put("originAdapter", signal.adapterName());
    if (reason != null) {
      detail.put("reason", reason);
    }
    log.atWarn()
        .addKeyValue("wksErrorCode", "WKS-MAP-404")
        .addKeyValue("originAdapter", signal.adapterName())
        .addKeyValue("caseId", signal.caseInstance().id().toString())
        .addKeyValue("kind", signal.kind().name())
        .addKeyValue("signalSource", signal.source())
        .log("backend signal unmapped: {}", reason);
    publish(signal, caseRow, new AuditSource.Backend("unmapped"), "WKS-MAP-404", detail);
  }

  private void publish(
      BackendSignal signal,
      Case caseRow,
      AuditSource source,
      String errorCode,
      Map<String, String> detail) {
    String caseTypeVersion = String.valueOf(caseRow.caseTypeVersion());
    eventPublisher.publishAfterCommit(
        new BackendSignalRouted(
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
