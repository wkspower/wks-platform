package com.wkspower.platform.audit;

import com.wkspower.platform.domain.event.CaseCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * AFTER_COMMIT consumer for {@link CaseCreated} domain events. Persists one append-only row in
 * {@code audit_events} (event type {@code case.created}) and emits a stable slf4j wire-contract
 * line — same persist-then-log ordering and failure semantics as {@link EditAuditEmitter}.
 *
 * <p>Wire shape:
 *
 * <pre>
 * event=case.created caseId=&lt;UUID&gt; source=&lt;AuditSource.toString&gt; caseTypeId=&lt;str&gt; caseTypeVersion=&lt;int&gt;
 * </pre>
 *
 * <p>Persist-failure wire shape (matches {@link EditAuditEmitter}):
 *
 * <pre>
 * event=audit.persist.failed caseId=&lt;UUID&gt; reason=&lt;exception message&gt;
 * </pre>
 *
 * <p>Per {@code feedback_audit_after_commit}, this listener fires strictly AFTER the create
 * transaction commits — rollback of {@code CaseService.create} suppresses the audit row entirely.
 * The class is NOT {@code @Transactional}; the insert uses {@code REQUIRES_NEW} on the repository
 * method.
 */
@Component
public class CaseCreatedAuditEmitter {

  private static final Logger log = LoggerFactory.getLogger(CaseCreatedAuditEmitter.class);

  private final AuditEventWriter auditEventWriter;

  public CaseCreatedAuditEmitter(AuditEventWriter auditEventWriter) {
    this.auditEventWriter = auditEventWriter;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCaseCreated(CaseCreated event) {
    AuditEvent auditEvent = AuditEvent.fromCaseCreated(event);

    try {
      auditEventWriter.insert(auditEvent);
    } catch (RuntimeException persistFailure) {
      log.warn(
          "event=audit.persist.failed caseId={} reason={}",
          event.caseId(),
          persistFailure.toString());
    }

    log.info(
        "event=case.created caseId={} source={} caseTypeId={} caseTypeVersion={}",
        event.caseId(),
        auditEvent.source(),
        event.caseTypeId(),
        event.caseTypeVersion());
  }
}
