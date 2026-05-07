package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for {@code case_documents} (Story 14.2). Does NOT extend {@link BaseJpaEntity} because
 * the table has no optimistic-lock {@code version} column — documents are immutable after upload.
 * The {@code uploaded_at} column is set once at insert time (DEFAULT now() in DDL; also set
 * explicitly here for testability).
 *
 * <p>P5 — {@link CaseDocumentEntityListener} removes the backing storage object before the row is
 * deleted, covering cascade-delete via {@code ON DELETE CASCADE} on {@code case_id}.
 */
@Entity
@Table(name = "case_documents")
@EntityListeners(CaseDocumentEntityListener.class)
public class CaseDocumentEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "case_id", nullable = false, updatable = false)
  private UUID caseId;

  @Column(name = "file_name", nullable = false, length = 512)
  private String fileName;

  @Column(name = "content_type", nullable = false, length = 128)
  private String contentType;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "storage_key", nullable = false, length = 768)
  private String storageKey;

  @Column(name = "checksum", nullable = false, length = 64)
  private String checksum;

  // P6: nullable so user offboarding (ON DELETE SET NULL) does not block deletion.
  @Column(name = "uploaded_by", nullable = true)
  private UUID uploadedBy;

  @Column(name = "uploaded_at", nullable = false)
  private Instant uploadedAt;

  protected CaseDocumentEntity() {
    // JPA
  }

  public CaseDocumentEntity(
      UUID id,
      UUID caseId,
      String fileName,
      String contentType,
      long sizeBytes,
      String storageKey,
      String checksum,
      UUID uploadedBy,
      Instant uploadedAt) {
    this.id = id;
    this.caseId = caseId;
    this.fileName = fileName;
    this.contentType = contentType;
    this.sizeBytes = sizeBytes;
    this.storageKey = storageKey;
    this.checksum = checksum;
    this.uploadedBy = uploadedBy;
    this.uploadedAt = uploadedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCaseId() {
    return caseId;
  }

  public String getFileName() {
    return fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public String getChecksum() {
    return checksum;
  }

  public UUID getUploadedBy() {
    return uploadedBy;
  }

  public Instant getUploadedAt() {
    return uploadedAt;
  }
}
