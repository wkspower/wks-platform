package com.wkspower.platform.engine.listeners;

import com.wkspower.platform.domain.event.CaseStatusChanged;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.engine.delegate.ExecutionListener;
import org.cibseven.bpm.model.bpmn.instance.EndEvent;
import org.cibseven.bpm.model.bpmn.instance.ExtensionElements;
import org.cibseven.bpm.model.bpmn.instance.FlowElement;
import org.cibseven.bpm.model.bpmn.instance.UserTask;
import org.cibseven.bpm.model.bpmn.instance.cibseven.CamundaProperties;
import org.cibseven.bpm.model.bpmn.instance.cibseven.CamundaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Engine-callback adapter that fires on user-task and end-event {@code end} events. Reads the
 * {@code caseId} process variable, resolves the next status (per Story 2.4 Dev Notes §What counts
 * as a status boundary), updates {@code cases.status} via the {@link CaseStatusUpdater} port, and
 * publishes a {@link CaseStatusChanged} domain event.
 *
 * <p>The listener participates in the engine's transaction — a thrown exception rolls back both the
 * engine state and the case-row update atomically (Story 2.4 AC5). Per Story 2.4 Dev Notes
 * §Engine-callback hexagonal pattern, NO direct JPA writes happen here — the JPA adapter implements
 * the port and runs inside the same transaction.
 *
 * <p>This listener is registered against every parsed user task and end event by {@link
 * CaseStatusBpmnParseListener}.
 */
@Component
public class CaseStatusListener implements ExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(CaseStatusListener.class);

  private final CaseStatusUpdater caseStatusUpdater;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public CaseStatusListener(
      CaseStatusUpdater caseStatusUpdater, EventPublisher eventPublisher, Clock clock) {
    this.caseStatusUpdater = caseStatusUpdater;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  public void notify(DelegateExecution execution) {
    Object caseIdRaw = execution.getVariable("caseId");
    if (caseIdRaw == null) {
      // Not a WKS-managed process — silently no-op so embedded test fixtures and admin BPMNs
      // without our convention don't trip.
      return;
    }
    UUID caseId;
    try {
      caseId = UUID.fromString(caseIdRaw.toString());
    } catch (IllegalArgumentException ex) {
      log.warn(
          "CaseStatusListener: 'caseId' variable is not a UUID (value={}); skipping update",
          caseIdRaw);
      return;
    }

    String currentElementId = execution.getCurrentActivityId();
    FlowElement element = execution.getBpmnModelElementInstance();
    String newStatus = resolveNewStatus(execution, element, currentElementId);
    if (newStatus == null || newStatus.isBlank()) {
      // Nothing meaningful to write — e.g. user-task end with no further active activity (parallel
      // gateway race) or end-event with no derivable id.
      return;
    }

    Optional<String> previous = caseStatusUpdater.updateStatus(caseId, newStatus);
    if (previous.isEmpty()) {
      // Engine fired the listener for a process whose case row is missing — likely a partial-state
      // create that did not persist. Avoid emitting a CaseStatusChanged for a non-existent case
      // (Story 2.4 review).
      log.warn(
          "CaseStatusListener: no case row for caseId={} (process={}) — skipping event publish",
          caseId,
          execution.getProcessInstanceId());
      return;
    }
    // Publish only after the engine transaction commits — subscribers must not observe a status
    // change that the engine subsequently rolls back (Story 2.4 review decision 1).
    Instant now = clock.now();
    String processInstanceId = execution.getProcessInstanceId();
    UUID committedCaseId = caseId;
    String committedNewStatus = newStatus;
    String committedPrevious = previous.orElse(null);
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              eventPublisher.publish(
                  new CaseStatusChanged(
                      committedCaseId,
                      committedPrevious,
                      committedNewStatus,
                      processInstanceId,
                      now));
            }
          });
    } else {
      // Test harnesses (or any path that runs the listener outside a Spring TX) — fall back to
      // synchronous publication so behaviour is observable. Production always has a TX bound.
      eventPublisher.publish(
          new CaseStatusChanged(
              committedCaseId, committedPrevious, committedNewStatus, processInstanceId, now));
    }
  }

  /**
   * Per Story 2.4 Dev Notes §What counts as a status boundary:
   *
   * <ul>
   *   <li>End event: status is the end-event's {@code camunda:property name="status"} when present,
   *       otherwise the element id.
   *   <li>User task end: peek the next active activity via the runtime service and use its id.
   * </ul>
   */
  private static String resolveNewStatus(
      DelegateExecution execution, FlowElement element, String currentElementId) {
    if (element instanceof EndEvent endEvent) {
      String declared = readStatusProperty(endEvent.getExtensionElements());
      return declared != null ? declared : currentElementId;
    }
    if (element instanceof UserTask) {
      List<String> active =
          execution
              .getProcessEngineServices()
              .getRuntimeService()
              .getActiveActivityIds(execution.getProcessInstanceId());
      // The current user task is still listed as active during the 'end' event (the engine has
      // not removed it yet). Filter it out and pick the first remaining activity — matches the
      // Phase 0 convention of "next active activity id is the new status".
      List<String> remaining =
          active.stream().filter(id -> id != null && !id.equals(currentElementId)).toList();
      if (remaining.size() > 1) {
        // TODO(Story 2.5/2.7): parallel-gateway fork — multiple sibling activities are active and
        // findFirst() picks one non-deterministically. Reject parallel forks in BpmnValidator or
        // adopt a composite status when this is first exercised by a real fixture.
        log.warn(
            "CaseStatusListener: process {} has {} simultaneously active activities after"
                + " user-task end ({}); status mapping is non-deterministic — picking first",
            execution.getProcessInstanceId(),
            remaining.size(),
            remaining);
      }
      return remaining.stream().findFirst().orElse(null);
    }
    return null;
  }

  private static String readStatusProperty(ExtensionElements ext) {
    if (ext == null) {
      return null;
    }
    Collection<CamundaProperties> blocks = ext.getChildElementsByType(CamundaProperties.class);
    for (CamundaProperties block : blocks) {
      for (CamundaProperty p : block.getCamundaProperties()) {
        if ("status".equals(p.getCamundaName())) {
          return p.getCamundaValue();
        }
      }
    }
    return null;
  }
}
