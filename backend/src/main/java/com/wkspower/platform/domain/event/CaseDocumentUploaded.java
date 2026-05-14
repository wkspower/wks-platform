package com.wkspower.platform.domain.event;

import com.wkspower.platform.domain.model.AuditSource;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitted after a document is persisted via {@code DocumentService.upload}. The user-uploading path
 * is the only audit source today — {@code source} is always an {@link AuditSource.User} carrying
 * the uploading {@code actorId}. {@code CaseDocumentUploadedAuditEmitter} consumes this event via
 * {@code @TransactionalEventListener(AFTER_COMMIT)} and persists one row in {@code audit_events}
 * (eventType {@code case.document.uploaded}).
 *
 * @param caseId case the document was attached to
 * @param documentId persisted document id
 * @param fileName sanitized filename (the value persisted on the {@code case_documents} row)
 * @param contentType MIME type
 * @param sizeBytes declared size in bytes
 * @param source typed attribution — {@code User(actorId)} on the manual upload path
 * @param timestamp upload timestamp (taken via {@code Clock.now()} at the publish site)
 */
public record CaseDocumentUploaded(
    UUID caseId,
    UUID documentId,
    String fileName,
    String contentType,
    long sizeBytes,
    AuditSource source,
    Instant timestamp) {}
