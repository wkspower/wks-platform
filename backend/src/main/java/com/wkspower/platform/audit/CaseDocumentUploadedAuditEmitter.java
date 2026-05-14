package com.wkspower.platform.audit;

import com.wkspower.platform.domain.event.CaseDocumentUploaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * AFTER_COMMIT consumer for {@link CaseDocumentUploaded} domain events. Persists one append-only
 * row in {@code audit_events} (event type {@code case.document.uploaded}) and emits a stable slf4j
 * wire-contract line — same persist-then-log ordering and failure semantics as the sibling
 * emitters.
 *
 * <p>Wire shape:
 *
 * <pre>
 * event=case.document.uploaded caseId=&lt;UUID&gt; documentId=&lt;UUID&gt; source=&lt;AuditSource.toString&gt; fileName=&lt;str&gt; contentType=&lt;str&gt; sizeBytes=&lt;long&gt;
 * </pre>
 *
 * <p>Persist-failure wire shape (matches sibling emitters):
 *
 * <pre>
 * event=audit.persist.failed caseId=&lt;UUID&gt; reason=&lt;exception message&gt;
 * </pre>
 *
 * <p>Per {@code feedback_audit_after_commit}, this listener fires strictly AFTER the surrounding
 * upload transaction commits — rollback suppresses the audit row entirely. Per {@code
 * feedback_transactional_event_listener_requires_outer_tx}, {@code DocumentController.upload} is
 * annotated {@code @Transactional} so this AFTER_COMMIT listener actually fires.
 */
@Component
public class CaseDocumentUploadedAuditEmitter {

  private static final Logger log = LoggerFactory.getLogger(CaseDocumentUploadedAuditEmitter.class);

  private final AuditEventWriter auditEventWriter;

  public CaseDocumentUploadedAuditEmitter(AuditEventWriter auditEventWriter) {
    this.auditEventWriter = auditEventWriter;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCaseDocumentUploaded(CaseDocumentUploaded event) {
    AuditEvent auditEvent = AuditEvent.fromCaseDocumentUploaded(event);

    try {
      auditEventWriter.insert(auditEvent);
    } catch (RuntimeException persistFailure) {
      log.warn(
          "event=audit.persist.failed caseId={} reason={}",
          event.caseId(),
          persistFailure.toString());
    }

    log.info(
        "event=case.document.uploaded caseId={} documentId={} source={} fileName={} contentType={} sizeBytes={}",
        event.caseId(),
        event.documentId(),
        event.source(),
        event.fileName(),
        event.contentType(),
        event.sizeBytes());
  }
}
