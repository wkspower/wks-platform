package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.domain.model.CaseDocument;
import com.wkspower.platform.domain.port.DocumentRepository;
import com.wkspower.platform.infrastructure.persistence.entity.CaseDocumentEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseDocumentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed adapter for the {@link DocumentRepository} port (Story 14.2). Translates between the
 * pure-Java {@link CaseDocument} domain record and the JPA {@link CaseDocumentEntity}.
 */
@Component
public class DocumentRepositoryAdapter implements DocumentRepository {

  private final CaseDocumentJpaRepository jpa;

  public DocumentRepositoryAdapter(CaseDocumentJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  @Transactional
  public CaseDocument save(CaseDocument doc) {
    CaseDocumentEntity entity =
        new CaseDocumentEntity(
            doc.id(),
            doc.caseId(),
            doc.fileName(),
            doc.contentType(),
            doc.sizeBytes(),
            doc.storageKey(),
            doc.checksum(),
            doc.uploadedBy(),
            doc.uploadedAt());
    jpa.save(entity);
    return doc; // entity is immutable after insert — return the original record
  }

  @Override
  public List<CaseDocument> findByCaseIdOrderByUploadedAtDesc(UUID caseId) {
    return jpa.findByCaseIdOrderByUploadedAtDesc(caseId).stream().map(this::toRecord).toList();
  }

  @Override
  public Optional<CaseDocument> findById(UUID documentId) {
    return jpa.findById(documentId).map(this::toRecord);
  }

  private CaseDocument toRecord(CaseDocumentEntity e) {
    return new CaseDocument(
        e.getId(),
        e.getCaseId(),
        e.getFileName(),
        e.getContentType(),
        e.getSizeBytes(),
        e.getStorageKey(),
        e.getChecksum(),
        e.getUploadedBy(),
        e.getUploadedAt());
  }
}
