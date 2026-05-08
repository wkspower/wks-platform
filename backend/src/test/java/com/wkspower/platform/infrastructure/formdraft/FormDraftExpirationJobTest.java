package com.wkspower.platform.infrastructure.formdraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.service.FormDraftService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Story 5.4 AC4 — unit test for {@link FormDraftExpirationJob} cutoff math + invocation. */
@ExtendWith(MockitoExtension.class)
class FormDraftExpirationJobTest {

  @Mock private FormDraftService draftService;
  @Mock private Clock clock;

  @Test
  void runUsesNowMinusTtlDaysAsCutoff() {
    Instant now = Instant.parse("2026-05-08T03:00:00Z");
    when(clock.now()).thenReturn(now);
    when(draftService.expireOlderThan(any(Instant.class))).thenReturn(0);

    new FormDraftExpirationJob(draftService, clock, 30).run();

    ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
    verify(draftService).expireOlderThan(captor.capture());
    assertThat(captor.getValue()).isEqualTo(now.minus(30, ChronoUnit.DAYS));
  }

  @Test
  void runIsResilientToZeroDeletes() {
    when(clock.now()).thenReturn(Instant.parse("2026-05-08T03:00:00Z"));
    when(draftService.expireOlderThan(any(Instant.class))).thenReturn(0);

    // Should not throw or log.error — behaviour is best-effort.
    new FormDraftExpirationJob(draftService, clock, 30).run();
  }
}
