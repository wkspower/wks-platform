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

  /**
   * Story 4.5 AC3 — SHA-256 hex of raw BPMN bytes at deploy time. {@code NULL} for zero-attachment
   * deploys (D22 first-class). Forensic / integrity column — not a routing key.
   */
  @Column(name = "bpmn_content_hash", length = 64, nullable = true, updatable = false)
  private String bpmnContentHash;

  /**
   * Story 4.5 AC3 — SHA-256 hex of canonical MappingDefinition toString at deploy time. {@code
   * NULL} for zero-attachment deploys (D22 first-class). Forensic / integrity column.
   */
  @Column(name = "mapping_hash", length = 64, nullable = true, updatable = false)
  private String mappingHash;

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
    this(caseTypeId, version, definitionHash, definitionYaml, publishedAt, publishedBy, null, null);
  }

  public CaseTypeVersionEntity(
      String caseTypeId,
      int version,
      String definitionHash,
      String definitionYaml,
      Instant publishedAt,
      String publishedBy,
      String bpmnContentHash,
      String mappingHash) {
    this.caseTypeId = caseTypeId;
    this.version = version;
    this.definitionHash = definitionHash;
    this.definitionYaml = definitionYaml;
    this.publishedAt = publishedAt;
    this.publishedBy = publishedBy;
    this.bpmnContentHash = bpmnContentHash;
    this.mappingHash = mappingHash;
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

  /** Story 4.5 AC3 — may be {@code null} for zero-attachment deploys. */
  public String getBpmnContentHash() {
    return bpmnContentHash;
  }

  /** Story 4.5 AC3 — may be {@code null} for zero-attachment deploys. */
  public String getMappingHash() {
    return mappingHash;
  }
}
