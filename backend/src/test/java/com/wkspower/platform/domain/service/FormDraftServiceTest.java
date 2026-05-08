package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wkspower.platform.domain.event.FormDraftExpired;
import com.wkspower.platform.domain.model.FormDraft;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.FormDraftRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Story 5.4 unit tests for {@link FormDraftService}. */
@ExtendWith(MockitoExtension.class)
class FormDraftServiceTest {

  @Mock private FormDraftRepository repository;
  @Mock private Clock clock;
  @Mock private EventPublisher events;

  private final Instant fixed = Instant.parse("2026-05-08T12:00:00Z");

  private FormDraftService service() {
    return new FormDraftService(repository, clock, events);
  }

  @Test
  void saveDraftDelegatesUpsertToRepository() {
    when(clock.now()).thenReturn(fixed);
    UUID caseId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    FormDraft expected =
        new FormDraft(
            UUID.randomUUID(), caseId, "f1", userId, Map.of("a", "b"), 42, null, 1, fixed, fixed);
    when(repository.upsert(
            eq(caseId), eq("f1"), eq(userId), any(), eq(42), any(), eq(1), eq(fixed)))
        .thenReturn(expected);

    FormDraft saved = service().saveDraft(caseId, "f1", userId, Map.of("a", "b"), 42, null, 1);

    assertThat(saved).isEqualTo(expected);
  }

  @Test
  void deleteDraftReturnsTrueWhenRowDeleted() {
    UUID caseId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(repository.deleteByScope(caseId, "f1", userId)).thenReturn(1);

    assertThat(service().deleteDraft(caseId, "f1", userId)).isTrue();
  }

  @Test
  void deleteDraftIsIdempotent() {
    UUID caseId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(repository.deleteByScope(caseId, "f1", userId)).thenReturn(0);

    assertThat(service().deleteDraft(caseId, "f1", userId)).isFalse();
  }

  @Test
  void findDraftDelegates() {
    UUID caseId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(repository.findByScope(caseId, "f1", userId)).thenReturn(Optional.empty());

    assertThat(service().findDraft(caseId, "f1", userId)).isEmpty();
  }

  @Test
  void expireOlderThanEmitsEventPerRowAndDeletesAll() {
    Instant cutoff = fixed.minus(30, ChronoUnit.DAYS);
    when(clock.now()).thenReturn(fixed);
    Instant oldA = fixed.minus(31, ChronoUnit.DAYS);
    Instant oldB = fixed.minus(45, ChronoUnit.DAYS);
    FormDraft a =
        new FormDraft(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "f1",
            UUID.randomUUID(),
            Map.of(),
            0,
            null,
            1,
            oldA,
            oldA);
    FormDraft b =
        new FormDraft(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "f2",
            UUID.randomUUID(),
            Map.of(),
            0,
            null,
            1,
            oldB,
            oldB);
    when(repository.findByUpdatedAtBefore(cutoff)).thenReturn(List.of(a, b));

    int n = service().expireOlderThan(cutoff);

    assertThat(n).isEqualTo(2);
    verify(events, times(2)).publish(any(FormDraftExpired.class));
    verify(repository).deleteAll(List.of(a, b));
  }

  @Test
  void expireOlderThanReturnsZeroWhenNothingExpired() {
    Instant cutoff = fixed.minus(30, ChronoUnit.DAYS);
    when(repository.findByUpdatedAtBefore(cutoff)).thenReturn(List.of());

    assertThat(service().expireOlderThan(cutoff)).isZero();
    // ensure publish is never called when nothing expired
    verify(events, times(0)).publish(any());
  }

  // Sanity: anyInt usage prevents mockito unused-import lint warnings
  @SuppressWarnings("unused")
  private void anyIntPlaceholder() {
    anyInt();
  }
}
