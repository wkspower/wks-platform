package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.model.CaseDocument;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for document metadata persistence (Story 14.2). Implemented by {@code
 * infrastructure.persistence.DocumentRepositoryAdapter} which wraps the JPA repository. Decouples
 * the domain service from JPA so the domain layer remains framework-free.
 */
public interface DocumentRepository {

  /** Persists a new document record. Returns the saved record. */
  CaseDocument save(CaseDocument document);

  /** Returns all documents for a case ordered by upload time descending. */
  List<CaseDocument> findByCaseIdOrderByUploadedAtDesc(UUID caseId);

  /** Returns the document with the given id, or empty if not found. */
  Optional<CaseDocument> findById(UUID documentId);
}
