package com.wkspower.platform.domain.event;

import com.wkspower.platform.domain.model.AuditSource;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Story 6.3 AC-6 — domain event consolidating every direct-edit case-data mutation outcome through
 * a single audit channel. Emitted by {@code CaseService.update} (the {@code PUT /api/cases/{id}}
 * direct-edit path) on both the applied and blocked branches so the SI audit log records the
 * intent regardless of result.
 *
 * <p>Per [[feedback_audit_after_commit]]: subscribers MUST listen with {@code
 * @TransactionalEventListener(phase = AFTER_COMMIT)} — never {@code log.info} inside a {@code
 * @Transactional} scope, because Postgres aborts on a subsequent DB exception while the audit line
 * already fired. The {@link com.wkspower.platform.audit.EditAuditEmitter} listener satisfies this
 * contract.
 *
 * <p>The {@code source} discriminator reuses the existing sealed {@link AuditSource} interface
 * (Story 4.3) — Story 6.3's edit-contract uses {@link AuditSource.User} for direct-edit
 * attribution; future {@code FormSubmitted}-bridged events would use a parallel sealed permit.
 *
 * @param caseId case whose data was the target of the edit
 * @param source attribution — typically {@link AuditSource.User} for the direct-edit path
 * @param result {@link Result#APPLIED} on commit, {@link Result#BLOCKED} when AC-2 gating
 *     intercepted, {@link Result#REJECTED} when AC-5's race-conflict path rejects (future)
 * @param fieldId the field the caller attempted to mutate (one event per blocked field; on
 *     APPLIED, multiple field events may be emitted from a single update)
 * @param openTaskId the open userTask id whose form owns {@code fieldId} on the BLOCKED path; null
 *     for APPLIED
 * @param formId the form id binding {@code fieldId} on the BLOCKED path; null for APPLIED
 * @param timestamp event time (clock-injected so tests can pin)
 */
public record CaseDataEdited(
    UUID caseId,
    AuditSource source,
    Result result,
    String fieldId,
    String openTaskId,
    String formId,
    Instant timestamp) {

  public enum Result {
    APPLIED,
    BLOCKED,
    REJECTED
  }
}
