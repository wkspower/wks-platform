package com.wkspower.platform.infrastructure.formdraft;

import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.service.FormDraftService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Story 5.4 AC4 — daily scheduled job that expires drafts inactive for {@code
 * wks.form.draft.ttl-days} (default 30). Defaults to 03:00 daily; both the cron expression and the
 * TTL are externalised to {@code application.yaml} so operators can tune without a redeploy.
 *
 * <p>{@code @EnableScheduling} lives on {@code WksPlatformApplication} (the canonical Spring Boot
 * location) — do NOT add it on a separate {@code @Configuration} per Sprint 5 retro lesson.
 */
@Component
public class FormDraftExpirationJob {

  private static final Logger log = LoggerFactory.getLogger(FormDraftExpirationJob.class);

  private final FormDraftService draftService;
  private final Clock clock;
  private final long ttlDays;

  public FormDraftExpirationJob(
      FormDraftService draftService,
      Clock clock,
      @Value("${wks.form.draft.ttl-days:30}") long ttlDays) {
    this.draftService = draftService;
    this.clock = clock;
    this.ttlDays = ttlDays;
  }

  @Scheduled(cron = "${wks.form.draft.expiration-cron:0 0 3 * * *}")
  public void run() {
    Instant cutoff = clock.now().minus(ttlDays, ChronoUnit.DAYS);
    int deleted = draftService.expireOlderThan(cutoff);
    if (deleted > 0) {
      log.info("FormDraftExpirationJob: expired {} drafts (cutoff {})", deleted, cutoff);
    }
  }
}
