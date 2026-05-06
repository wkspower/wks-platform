package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for {@code case_type_versions} (Story 3.4 / Decision 20). Append-only — there is no
 * setter for the natural key components and the table has no edit lifecycle. Does NOT extend {@link
 * BaseJpaEntity}: the natural composite key {@code (case_type_id, version)} is operator visible,
 * and the table has no soft-delete or audit columns beyond {@code published_at / published_by}.
 */
@Entity
@Table(name = "case_type_versions")
@IdClass(CaseTypeVersionId.class)
public class CaseTypeVersionEntity {

  @Id
  @Column(name = "case_type_id", length = 64, nullable = false, updatable = false)
  private String caseTypeId;

  @Id
  @Column(name = "version", nullable = false, updatable = false)
  private int version;

  @Column(name = "definition_hash", length = 64, nullable = false, updatable = false)
  private String definitionHash;

  @Column(name = "definition_yaml", nullable = false, updatable = false, columnDefinition = "TEXT")
  private String definitionYaml;

  @Column(name = "published_at", nullable = false, updatable = false)
  private Instant publishedAt;

  @Column(name = "published_by", length = 128, nullable = false, updatable = false)
  private String publishedBy;

  protected CaseTypeVersionEntity() {
    // JPA
  }

  public CaseTypeVersionEntity(
      String caseTypeId,
      int version,
      String definitionHash,
      String definitionYaml,
      Instant publishedAt,
      String publishedBy) {
    this.caseTypeId = caseTypeId;
    this.version = version;
    this.definitionHash = definitionHash;
    this.definitionYaml = definitionYaml;
    this.publishedAt = publishedAt;
    this.publishedBy = publishedBy;
  }

  public String getCaseTypeId() {
    return caseTypeId;
  }

  public int getVersion() {
    return version;
  }

  public String getDefinitionHash() {
    return definitionHash;
  }

  public String getDefinitionYaml() {
    return definitionYaml;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public String getPublishedBy() {
    return publishedBy;
  }
}
