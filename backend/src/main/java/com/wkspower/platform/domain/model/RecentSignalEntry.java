package com.wkspower.platform.domain.model;

import com.wkspower.platform.domain.port.ExecutionSignalKind;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Story 4.6 AC3 / AC5 — pure-domain value record describing one signal-routing decision recorded by
 * {@code ExecutionSignalRouter} into {@code SignalAuditRingBuffer} for the admin Mapping Inspector
 * view.
 *
 * <p>Lossy by design: this is a debug-side projection of the canonical {@code
 * ExecutionSignalRouted} event, retained only in-memory and bounded per CaseType. Persistence is
 * out of scope (architecture §4.6 — the Activity Feed in Epic 9 is the durable equivalent).
 *
 * <p>{@code decision} is one of the literal strings {@code matched-rule} | {@code unmapped} |
 * {@code case-not-found} | {@code version-not-registered} (mirrors the four audit-emit branches in
 * the router). {@code errorCode} carries {@code WKS-MAP-404} / {@code WKS-MAP-405} for the
 * miss-side branches and {@code null} for matched-rule.
 *
 * @param timestamp wall-clock time the routing decision was recorded
 * @param kind the inbound signal kind
 * @param source the adapter-internal element id (BPMN element id, etc.)
 * @param decision literal decision token — see class javadoc
 * @param matchedRule textual identifier of the rule that matched on success, or {@code null}
 * @param effect short human-readable description of the effect applied, or {@code null} on miss
 * @param caseId the case the signal targeted, may be {@code null} for case-not-found branch
 * @param errorCode WKS wire error code on miss, {@code null} on success
 */
public record RecentSignalEntry(
    Instant timestamp,
    ExecutionSignalKind kind,
    String source,
    String decision,
    String matchedRule,
    String effect,
    UUID caseId,
    String errorCode) {

  public RecentSignalEntry {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(decision, "decision");
    // matchedRule, effect, caseId, errorCode are nullable per branch.
  }
}
