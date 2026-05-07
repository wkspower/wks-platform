package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksDocumentException;
import com.wkspower.platform.domain.model.CaseDocument;
import com.wkspower.platform.domain.port.DocumentRepository;
import com.wkspower.platform.domain.port.DocumentStore;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Framework-free domain service for case document operations (Story 14.2). No Spring, no JPA — all
 * infrastructure dependencies are injected through the {@link DocumentRepository} and {@link
 * DocumentStore} ports. Wired as a Spring {@code @Bean} in {@code DocumentServiceConfig} in the
 * infrastructure layer.
 *
 * <p>Validation logic lives in {@link DocumentValidator} (also pure Java) and is called from {@link
 * #upload} before any I/O.
 */
public class DocumentService {

  private final DocumentStore documentStore;
  private final DocumentRepository documentRepository;
  private final long maxSizeMb;

  public DocumentService(
      DocumentStore documentStore, DocumentRepository documentRepository, long maxSizeMb) {
    this.documentStore = documentStore;
    this.documentRepository = documentRepository;
    this.maxSizeMb = maxSizeMb;
  }

  /**
   * Validates, stores, and persists a document attachment for a case.
   *
   * @param caseId the owning case UUID
   * @param fileName the original filename from the client
   * @param stream the raw byte stream (caller must close after this method returns)
   * @param contentType declared MIME type
   * @param sizeBytes declared size in bytes
   * @param actorId the uploading user
   * @return the persisted {@link CaseDocument} domain record
   */
  public CaseDocument upload(
      UUID caseId,
      String fileName,
      InputStream stream,
      String contentType,
      long sizeBytes,
      UUID actorId) {

    // 1–3. Validation (throws WKS-DOC-001/002/003 on failure).
    DocumentValidator.validateSize(sizeBytes, maxSizeMb);
    DocumentValidator.validateContentType(contentType);
    DocumentValidator.sanitizeFileName(fileName);

    // 4–5. Compute SHA-256 checksum while streaming to store.
    UUID documentId = UUID.randomUUID();
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      InputStream digestedStream = new DigestInputStream(stream, digest);

      // 6. Store via DocumentStore port.
      String storageKey =
          documentStore.store(caseId, documentId, fileName, digestedStream, contentType, sizeBytes);
      String checksum = HexFormat.of().formatHex(digest.digest());

      // 7. Persist metadata via DocumentRepository port.
      Instant now = Instant.now();
      CaseDocument doc =
          new CaseDocument(
              documentId,
              caseId,
              fileName,
              contentType,
              sizeBytes,
              storageKey,
              checksum,
              actorId,
              now);
      return documentRepository.save(doc);
    } catch (WksDocumentException e) {
      throw e;
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005, "Unexpected error during document upload: " + e.getMessage(), e);
    }
  }

  /** Returns all documents for the given case ordered by upload time descending. */
  public List<CaseDocument> listByCase(UUID caseId) {
    return documentRepository.findByCaseIdOrderByUploadedAtDesc(caseId);
  }

  /** Returns the document metadata for the given document id, or empty if not found. */
  public Optional<CaseDocument> findById(UUID documentId) {
    return documentRepository.findById(documentId);
  }

  /**
   * Opens a stream for the document's backing file. Caller is responsible for closing the stream.
   */
  public InputStream openStream(CaseDocument doc) {
    return documentStore.retrieve(doc.storageKey());
  }

  /** Returns whether the given content type can be previewed inline (PDF or images). */
  public boolean isPreviewable(String contentType) {
    return DocumentValidator.isPreviewable(contentType);
  }

  /**
   * Returns a presigned URL if supported by the store, otherwise null. The controller uses null to
   * fall back to the download endpoint URL. Delegating through the port avoids an infra instanceof
   * check in domain; the {@code DocumentStore} port exposes this via a default method override.
   */
  public String getPresignedUrl(CaseDocument doc) {
    return documentStore.presignedUrl(doc.storageKey());
  }
}
