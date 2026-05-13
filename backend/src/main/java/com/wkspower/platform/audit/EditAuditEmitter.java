package com.wkspower.platform.audit;

import com.wkspower.platform.domain.event.CaseDataEdited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Story 6.3 AC-6 — single AFTER_COMMIT consumer for {@link CaseDataEdited} events emitted by the
 * direct-edit path ({@code CaseService.update}). Renders one audit log line per event using a wire
 * shape stable for SI runbook greps.
 *
 * <p>Story 9-3 enhancement: each event is now ALSO persisted to the append-only {@code
 * audit_events} table via {@link AuditEventWriter#insert} (persist FIRST, log SECOND). If the
 * insert fails (DB down, FK violation), the failure is logged at WARN as {@code
 * event=audit.persist.failed} and the original {@code event=case.data.edit} slf4j line still fires
 * — the log stream remains the authoritative SI-runbook grep target per {@code
 * feedback_error_codes_are_wire_contract} (wire contracts do not silently disappear). The table is
 * enhancement not replacement.
 *
 * <p>Wire shape (unchanged from Story 6.3):
 *
 * <pre>
 * event=case.data.edit caseId=&lt;UUID&gt; source=&lt;AuditSource.toString&gt; result=&lt;APPLIED|BLOCKED|REJECTED&gt; fieldId=&lt;str&gt; [openTaskId=&lt;str&gt; formId=&lt;str&gt;]
 * </pre>
 *
 * <p>Failure wire shape:
 *
 * <pre>
 * event=audit.persist.failed caseId=&lt;UUID&gt; reason=&lt;exception message&gt;
 * </pre>
 *
 * <p>Per [[feedback_audit_after_commit]] (Sprint 10 3-9 incident): the event listener fires
 * strictly AFTER the surrounding transaction commits. A rollback after the event is published
 * suppresses the audit line entirely — the regression guard for the "ghost audit" defect. This
 * class is NOT {@code @Transactional}; the insert uses {@code REQUIRES_NEW} on the repository
 * method (see {@link AuditEventWriter#insert}).
 *
 * <p>No new error codes are minted for the WARN line per Story 9-3 spec — {@code
 * audit.persist.failed} is a log-only signal, not a {@code WKS-AUDIT-xxx} band entry.
 */
@Component
public class EditAuditEmitter {

  private static final Logger log = LoggerFactory.getLogger(EditAuditEmitter.class);

  private final AuditEventWriter auditEventRepository;

  public EditAuditEmitter(AuditEventWriter auditEventRepository) {
    this.auditEventRepository = auditEventRepository;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCaseDataEdited(CaseDataEdited event) {
    // Persist FIRST: append-only table is the timeline backing for Story 9-2. If the insert
    // throws, fall through to the slf4j line — never lose the wire-contract grep target.
    try {
      auditEventRepository.insert(AuditEvent.fromCaseDataEdited(event));
    } catch (RuntimeException persistFailure) {
      log.warn(
          "event=audit.persist.failed caseId={} reason={}",
          event.caseId(),
          persistFailure.getMessage());
    }

    // Story 6.3 wire-contract slf4j line — preserved verbatim for SI runbook greps.
    if (event.result() == CaseDataEdited.Result.BLOCKED) {
      log.info(
          "event=case.data.edit caseId={} source={} result={} fieldId={} openTaskId={} formId={}",
          event.caseId(),
          event.source(),
          event.result(),
          event.fieldId(),
          event.openTaskId(),
          event.formId());
    } else {
      log.info(
          "event=case.data.edit caseId={} source={} result={} fieldId={}",
          event.caseId(),
          event.source(),
          event.result(),
          event.fieldId());
    }
  }
}
