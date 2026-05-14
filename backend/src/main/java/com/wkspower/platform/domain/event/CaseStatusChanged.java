package com.wkspower.platform.domain.event;

import com.wkspower.platform.domain.model.AuditSource;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a case's status changes — either via the zero-process manual transition path ({@code
 * CaseService.transition} with no BPMN attachment) or via the BPMN-driven path through {@code
 * ExecutionSignalRouter} (userTask status property + post-stage-advance status reset).
 *
 * <p>{@link #source} attributes the change: {@link AuditSource.User} on the manual path, {@link
 * AuditSource.Backend} on the BPMN path. {@code CaseStatusChangedAuditEmitter} consumes this event
 * via {@code @TransactionalEventListener(AFTER_COMMIT)} and persists one row in {@code
 * audit_events} (eventType {@code case.status.changed}).
 *
 * @param caseId case whose status changed
 * @param oldStatus previous status id (may be {@code null} on the very first transition out of the
 *     start event if no prior user-task fired)
 * @param newStatus new status id
 * @param processInstanceId BPMN process instance id (null on the zero-process path)
 * @param source typed attribution — {@code User(actorId)} on the manual path, {@code
 *     Backend(adapterName)} on the BPMN path
 * @param timestamp commit timestamp (taken via {@code Clock.now()} at the publish site)
 */
public record CaseStatusChanged(
    UUID caseId,
    String oldStatus,
    String newStatus,
    String processInstanceId,
    AuditSource source,
    Instant timestamp) {}
