package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.event.FormDraftExpired;
import com.wkspower.platform.domain.model.FormDraft;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.FormDraftRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Story 5.4 — domain service for form draft persistence (auto-save + resume + expiration).
 *
 * <p>Framework-free per ArchUnit rule {@code domainHasNoFrameworkImports}. Spring transaction
 * management lives at the controller layer (the controller's {@code @Transactional} wraps the
 * upsert + delete-on-submit path; the expiration job opens its own transaction via the
 * infrastructure adapter).
 */
public class FormDraftService {

  private final FormDraftRepository repository;
  private final Clock clock;
  private final EventPublisher events;

  public FormDraftService(FormDraftRepository repository, Clock clock, EventPublisher events) {
    this.repository = Objects.requireNonNull(repository, "repository");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.events = Objects.requireNonNull(events, "events");
  }

  public Optional<FormDraft> findDraft(UUID caseId, String formId, UUID userId) {
    return repository.findByScope(caseId, formId, userId);
  }

  /** Upsert semantics — finds existing row by scope, updates in place, else inserts. */
  public FormDraft saveDraft(
      UUID caseId,
      String formId,
      UUID userId,
      Map<String, Object> payload,
      int scrollY,
      Map<String, Boolean> sectionExpanded,
      int caseTypeVersionAtSave) {
    return repository.upsert(
        caseId,
        formId,
        userId,
        payload,
        scrollY,
        sectionExpanded,
        caseTypeVersionAtSave,
        clock.now());
  }

  /** Returns true if a draft existed and was deleted. Idempotent — false if nothing to delete. */
  public boolean deleteDraft(UUID caseId, String formId, UUID userId) {
    return repository.deleteByScope(caseId, formId, userId) > 0;
  }

  /**
   * AC4 — invoked by the scheduled expiration job. Returns deletion count. Loads the affected rows
   * first so a {@link FormDraftExpired} event can be emitted per row before the batch delete.
   */
  public int expireOlderThan(Instant cutoff) {
    List<FormDraft> expired = repository.findByUpdatedAtBefore(cutoff);
    if (expired.isEmpty()) {
      return 0;
    }
    Instant now = clock.now();
    for (FormDraft d : expired) {
      long ageDays = ChronoUnit.DAYS.between(d.updatedAt(), now);
      events.publish(new FormDraftExpired(d.caseId(), d.formId(), d.userId(), ageDays, now));
    }
    repository.deleteAll(expired);
    return expired.size();
  }
}
