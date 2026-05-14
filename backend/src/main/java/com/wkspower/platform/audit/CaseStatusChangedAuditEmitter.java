package com.wkspower.platform.audit;

import com.wkspower.platform.domain.event.CaseStatusChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * AFTER_COMMIT consumer for {@link CaseStatusChanged} domain events. Persists one append-only row
 * in {@code audit_events} (event type {@code case.status.changed}) and emits a stable slf4j
 * wire-contract line — same persist-then-log ordering and failure semantics as {@link
 * CaseCreatedAuditEmitter} / {@link EditAuditEmitter}.
 *
 * <p>Wire shape:
 *
 * <pre>
 * event=case.status.changed caseId=&lt;UUID&gt; source=&lt;AuditSource.toString&gt; oldStatus=&lt;str|null&gt; newStatus=&lt;str&gt; processInstanceId=&lt;str|null&gt;
 * </pre>
 *
 * <p>Persist-failure wire shape (matches sibling emitters):
 *
 * <pre>
 * event=audit.persist.failed caseId=&lt;UUID&gt; reason=&lt;exception message&gt;
 * </pre>
 *
 * <p>Per {@code feedback_audit_after_commit}, this listener fires strictly AFTER the mutating
 * transaction commits — rollback suppresses the audit row entirely.
 */
@Component
public class CaseStatusChangedAuditEmitter {

  private static final Logger log = LoggerFactory.getLogger(CaseStatusChangedAuditEmitter.class);

  private final AuditEventWriter auditEventWriter;

  public CaseStatusChangedAuditEmitter(AuditEventWriter auditEventWriter) {
    this.auditEventWriter = auditEventWriter;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCaseStatusChanged(CaseStatusChanged event) {
    AuditEvent auditEvent = AuditEvent.fromCaseStatusChanged(event);

    try {
      auditEventWriter.insert(auditEvent);
    } catch (RuntimeException persistFailure) {
      log.warn(
          "event=audit.persist.failed caseId={} reason={}",
          event.caseId(),
          persistFailure.toString());
    }

    log.info(
        "event=case.status.changed caseId={} source={} oldStatus={} newStatus={} processInstanceId={}",
        event.caseId(),
        event.source(),
        event.oldStatus(),
        event.newStatus(),
        event.processInstanceId());
  }
}
