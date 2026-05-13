package com.wkspower.platform.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Story 3.9 — emitted when {@link com.wkspower.platform.domain.service.CaseRebaseService#apply}
 * successfully mutates {@code cases.case_type_version}. Consumed by {@code
 * com.wkspower.platform.infrastructure.audit.RebaseAuditListener} via
 * {@code @TransactionalEventListener(AFTER_COMMIT)} so the {@code event=admin.case.rebase} audit
 * line is emitted ONLY after the surrounding transaction commits — never on rollback. This
 * structurally replaces the prior {@code log.info} inside {@code AdminController.rebaseApply} which
 * fired before commit (review finding #1).
 *
 * <p>Story 3.9.1 — extended with {@code stageRemap} field (optional, default empty). When present,
 * the audit line includes the remap pairs so operators can reconstruct the stage-flip history.
 * Decision: extend rather than mint a sibling {@code StageRemapApplied} event — smaller diff, no
 * current consumer needs distinct routing. Revisit if audit-query patterns surface the need.
 *
 * @param caseId the Case id whose version was rebased
 * @param caseTypeId the bound CaseType id
 * @param fromVersion the prior CaseType version (before the apply)
 * @param toVersion the target CaseType version (after the apply)
 * @param gateOutcome reason wire string surfaced in the audit line — fixed to {@code
 *     migration-rebase} for the rebase path; carried as a string to keep this domain event free of
 *     controller-layer enums
 * @param forceOverrideReason convenience alias of {@code gateOutcome} preserved so the audit line
 *     wire string {@code reason=Y} matches the prior {@code log.info} verbatim
 * @param actor authenticated email of the operator who issued the rebase, or {@code "ANONYMOUS"}
 * @param requestId correlation id from the inbound HTTP request, or {@code null} when absent
 * @param timestamp the {@link Instant} at which the rebase was applied (taken in the controller
 *     before the event is published)
 * @param stageRemap the operator-supplied stage-id remap map, or {@code null} / empty when no stage
 *     remap was applied. Keys are fromVersion stage ids; values are toVersion stage ids.
 */
public record RebaseApplied(
    UUID caseId,
    String caseTypeId,
    int fromVersion,
    int toVersion,
    String gateOutcome,
    String forceOverrideReason,
    String actor,
    String requestId,
    Instant timestamp,
    Map<String, String> stageRemap) {

  /** Backward-compat factory — no stageRemap (Story 3.9 callsites). */
  public static RebaseApplied withoutRemap(
      UUID caseId,
      String caseTypeId,
      int fromVersion,
      int toVersion,
      String gateOutcome,
      String forceOverrideReason,
      String actor,
      String requestId,
      Instant timestamp) {
    return new RebaseApplied(
        caseId,
        caseTypeId,
        fromVersion,
        toVersion,
        gateOutcome,
        forceOverrideReason,
        actor,
        requestId,
        timestamp,
        Map.of());
  }
}
