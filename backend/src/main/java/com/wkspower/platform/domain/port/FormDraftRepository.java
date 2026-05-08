package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.model.FormDraft;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Story 5.4 — outbound port for form draft persistence. Implemented by an infrastructure adapter
 * that delegates to a JPA repository. Keeps the domain service framework-free.
 *
 * <p>The single read accessor scopes to {@code (caseId, formId, userId)} — there is intentionally
 * no {@code findByCaseIdAndFormId(...)} variant (AC5 cross-user leakage guard).
 */
public interface FormDraftRepository {

  Optional<FormDraft> findByScope(UUID caseId, String formId, UUID userId);

  /**
   * Upsert: insert when no row exists for {@code (caseId, formId, userId)}, otherwise overwrite the
   * existing row in place. Returns the persisted form-draft (with assigned id + audit timestamps).
   */
  FormDraft upsert(
      UUID caseId,
      String formId,
      UUID userId,
      java.util.Map<String, Object> payload,
      int scrollY,
      java.util.Map<String, Boolean> sectionExpanded,
      int caseTypeVersionAtSave,
      Instant now);

  /**
   * @return number of rows deleted (0 or 1).
   */
  int deleteByScope(UUID caseId, String formId, UUID userId);

  List<FormDraft> findByUpdatedAtBefore(Instant cutoff);

  void deleteAll(List<FormDraft> drafts);
}
