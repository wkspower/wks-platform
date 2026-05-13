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
 * <p>Wire shape:
 *
 * <pre>
 * event=case.data.edit caseId=&lt;UUID&gt; source=&lt;AuditSource.toString&gt; result=&lt;APPLIED|BLOCKED|REJECTED&gt; fieldId=&lt;str&gt; [openTaskId=&lt;str&gt; formId=&lt;str&gt;]
 * </pre>
 *
 * <p>Per [[feedback_audit_after_commit]] (Sprint 10 3-9 incident): the event listener fires
 * strictly AFTER the surrounding transaction commits. A rollback after the event is published
 * suppresses the audit line entirely — the regression guard for the "ghost audit" defect.
 */
@Component
public class EditAuditEmitter {

  private static final Logger log = LoggerFactory.getLogger(EditAuditEmitter.class);

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCaseDataEdited(CaseDataEdited event) {
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
