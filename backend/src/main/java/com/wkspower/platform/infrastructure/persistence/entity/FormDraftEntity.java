package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Story 5.4 — JPA entity for {@code form_drafts}. Inherits id / version / audit timestamps from
 * {@link BaseJpaEntity}; owns the draft-scope columns plus the JSON {@code payload} column that
 * carries the in-progress form values.
 *
 * <p>Hibernate 6 ({@link JdbcTypeCode} + {@link SqlTypes#JSON}) maps the {@code Map<String,
 * Object>} natively (same convention as {@link CaseEntity#getData()}). The {@code section_expanded}
 * column is nullable — single-page renderer drafts leave it null.
 *
 * <p>Uniqueness on {@code (case_id, form_id, user_id)} is enforced at the DB layer (AC5 — no
 * cross-user leakage). The service layer must read with {@code findByCaseIdAndFormIdAndUserId}
 * before deciding whether to insert or update — never blindly call {@code repository.save} (memory
 * {@code feedback_jpa_idclass_save_is_upsert.md}; this is a single-column @Id, so save is upsert,
 * but the unique constraint would still trip on a stale insert if scope was misread).
 */
@Entity
@Table(
    name = "form_drafts",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_form_drafts_scope",
            columnNames = {"case_id", "form_id", "user_id"}))
public class FormDraftEntity extends BaseJpaEntity {

  @Column(name = "case_id", nullable = false)
  private UUID caseId;

  @Column(name = "form_id", nullable = false, length = 255)
  private String formId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload", nullable = false)
  private Map<String, Object> payload = new HashMap<>();

  @Column(name = "scroll_y", nullable = false)
  private int scrollY;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "section_expanded")
  private Map<String, Boolean> sectionExpanded;

  @Column(name = "case_type_version_at_save", nullable = false)
  private int caseTypeVersionAtSave;

  protected FormDraftEntity() {
    // JPA
  }

  public FormDraftEntity(
      UUID id,
      UUID caseId,
      String formId,
      UUID userId,
      Map<String, Object> payload,
      int scrollY,
      Map<String, Boolean> sectionExpanded,
      int caseTypeVersionAtSave,
      Instant createdAt,
      Instant updatedAt) {
    super(id);
    this.caseId = caseId;
    this.formId = formId;
    this.userId = userId;
    this.payload = payload == null ? new HashMap<>() : new HashMap<>(payload);
    this.scrollY = scrollY;
    this.sectionExpanded = sectionExpanded == null ? null : new HashMap<>(sectionExpanded);
    this.caseTypeVersionAtSave = caseTypeVersionAtSave;
    setCreatedAt(createdAt);
    setUpdatedAt(updatedAt);
  }

  public UUID getCaseId() {
    return caseId;
  }

  public String getFormId() {
    return formId;
  }

  public UUID getUserId() {
    return userId;
  }

  public Map<String, Object> getPayload() {
    return payload;
  }

  public void setPayload(Map<String, Object> payload) {
    this.payload = payload == null ? new HashMap<>() : new HashMap<>(payload);
  }

  public int getScrollY() {
    return scrollY;
  }

  public void setScrollY(int scrollY) {
    this.scrollY = scrollY;
  }

  public Map<String, Boolean> getSectionExpanded() {
    return sectionExpanded;
  }

  public void setSectionExpanded(Map<String, Boolean> sectionExpanded) {
    this.sectionExpanded = sectionExpanded == null ? null : new HashMap<>(sectionExpanded);
  }

  public int getCaseTypeVersionAtSave() {
    return caseTypeVersionAtSave;
  }

  public void setCaseTypeVersionAtSave(int caseTypeVersionAtSave) {
    this.caseTypeVersionAtSave = caseTypeVersionAtSave;
  }

  @Override
  public void setUpdatedAt(Instant updatedAt) {
    super.setUpdatedAt(updatedAt);
  }
}
