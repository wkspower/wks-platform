package com.wkspower.platform.domain.event;

import com.wkspower.platform.domain.model.AuditSource;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Story 4.3 AC5 / AC7 — domain event emitted by {@code ExecutionSignalRouter} for every {@code
 * ExecutionSignal} it processes (success and miss alike). Carries the typed {@link AuditSource} —
 * {@code source.toString()} renders to {@code "backend(<adapterName>)"} on success and {@code
 * "backend(unmapped)"} on a {@code WKS-MAP-404} miss (FR8 source attribution).
 *
 * <p>Publication discipline: published via {@code EventPublisher.publishAfterCommit(...)} so audit
 * observers see the post-commit state and never observe a phantom row. This is the audit-row seam
 * AC7's IT asserts against; existing string-based {@code Stage.source} / {@code
 * StageEntered.source} columns continue to carry the legacy {@code "backend-signal"} bare string
 * (migration to typed {@link AuditSource} is folded into Story 4.4).
 *
 * @param caseId WKS case instance id the signal targeted
 * @param caseTypeId stable CaseType id (e.g. {@code "loan-application"})
 * @param caseTypeVersion pinned CaseType version string the signal resolved against (D20)
 * @param kind the {@link ExecutionSignalKind} of the originating signal
 * @param signalSource adapter-internal element id from {@code ExecutionSignal.source} (BPMN element
 *     id, state-machine state name, etc.) for traceability
 * @param source typed {@link AuditSource} — always an {@code AuditSource.Backend} carrying the
 *     adapter name (or {@code "unmapped"} on a {@code WKS-MAP-404} miss)
 * @param errorCode WKS error code wire string (e.g. {@code "WKS-MAP-404"}) on a miss; {@code null}
 *     on a successful dispatch
 * @param detail free-form key→value map (currently {@code originAdapter}, {@code reason}); may be
 *     extended in future stories
 * @param timestamp commit timestamp from {@code Clock.now}
 */
public record ExecutionSignalRouted(
    UUID caseId,
    String caseTypeId,
    String caseTypeVersion,
    ExecutionSignalKind kind,
    String signalSource,
    AuditSource source,
    String errorCode,
    Map<String, String> detail,
    Instant timestamp) {

  public ExecutionSignalRouted {
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(caseTypeVersion, "caseTypeVersion");
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(signalSource, "signalSource");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(timestamp, "timestamp");
    detail = detail == null ? Map.of() : Map.copyOf(detail);
  }
}
