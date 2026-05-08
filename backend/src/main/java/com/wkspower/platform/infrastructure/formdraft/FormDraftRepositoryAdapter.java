package com.wkspower.platform.infrastructure.formdraft;

import com.wkspower.platform.domain.model.FormDraft;
import com.wkspower.platform.domain.port.FormDraftRepository;
import com.wkspower.platform.infrastructure.persistence.entity.FormDraftEntity;
import com.wkspower.platform.infrastructure.persistence.repository.FormDraftJpaRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Story 5.4 — JPA-backed adapter for the {@link FormDraftRepository} domain port. Maps {@link
 * FormDraftEntity} ⇆ {@link FormDraft} at the boundary so the domain service stays framework-free.
 *
 * <p>Memory {@code feedback_jpa_idclass_save_is_upsert.md}: this entity uses single-column @Id, so
 * {@code repository.save} is upsert, but the unique constraint on {@code (case_id, form_id,
 * user_id)} would still trip on a stale insert if scope was misread. {@link #upsert} reads first
 * via the scope query, then mutates the loaded row or inserts a new one.
 */
@Component
public class FormDraftRepositoryAdapter implements FormDraftRepository {

  private final FormDraftJpaRepository jpa;

  public FormDraftRepositoryAdapter(FormDraftJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<FormDraft> findByScope(UUID caseId, String formId, UUID userId) {
    return jpa.findByCaseIdAndFormIdAndUserId(caseId, formId, userId)
        .map(FormDraftRepositoryAdapter::toDomain);
  }

  @Override
  @Transactional
  public FormDraft upsert(
      UUID caseId,
      String formId,
      UUID userId,
      Map<String, Object> payload,
      int scrollY,
      Map<String, Boolean> sectionExpanded,
      int caseTypeVersionAtSave,
      Instant now) {
    FormDraftEntity entity =
        jpa.findByCaseIdAndFormIdAndUserId(caseId, formId, userId)
            .map(
                existing -> {
                  existing.setPayload(payload == null ? new HashMap<>() : payload);
                  existing.setScrollY(scrollY);
                  existing.setSectionExpanded(sectionExpanded);
                  existing.setCaseTypeVersionAtSave(caseTypeVersionAtSave);
                  existing.setUpdatedAt(now);
                  return existing;
                })
            .orElseGet(
                () ->
                    new FormDraftEntity(
                        UUID.randomUUID(),
                        caseId,
                        formId,
                        userId,
                        payload == null ? new HashMap<>() : payload,
                        scrollY,
                        sectionExpanded,
                        caseTypeVersionAtSave,
                        now,
                        now));
    return toDomain(jpa.save(entity));
  }

  @Override
  @Transactional
  public int deleteByScope(UUID caseId, String formId, UUID userId) {
    return jpa.deleteByScope(caseId, formId, userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<FormDraft> findByUpdatedAtBefore(Instant cutoff) {
    return jpa.findByUpdatedAtBefore(cutoff).stream()
        .map(FormDraftRepositoryAdapter::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteAll(List<FormDraft> drafts) {
    if (drafts == null || drafts.isEmpty()) {
      return;
    }
    List<UUID> ids = drafts.stream().map(FormDraft::id).toList();
    jpa.deleteAllById(ids);
  }

  private static FormDraft toDomain(FormDraftEntity e) {
    return new FormDraft(
        e.getId(),
        e.getCaseId(),
        e.getFormId(),
        e.getUserId(),
        e.getPayload() == null ? Map.of() : Map.copyOf(e.getPayload()),
        e.getScrollY(),
        e.getSectionExpanded() == null ? null : Map.copyOf(e.getSectionExpanded()),
        e.getCaseTypeVersionAtSave(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
