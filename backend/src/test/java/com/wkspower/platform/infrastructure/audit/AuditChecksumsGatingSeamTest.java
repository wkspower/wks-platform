package com.wkspower.platform.infrastructure.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

/**
 * Story 7-6 AC-3 — Verifies that {@link AuditChecksumsSeamAnnouncer} emits the correct log line
 * based on the {@code audit.checksums} license feature state.
 *
 * <p>When the feature is enabled, a WARN line with the {@code WKS-AUDIT-001} wire code must be
 * emitted to signal that chain-hash implementation is deferred to Epic 15.
 *
 * <p>When the feature is disabled, an INFO line is emitted (no WARN needed — the feature is simply
 * not active).
 *
 * <p>Mirrors the {@link
 * com.wkspower.platform.infrastructure.config.KeycloakSeamAnnouncerLicenseGateTest} pattern
 * exactly.
 */
class AuditChecksumsGatingSeamTest {

  private ListAppender<ILoggingEvent> appender;
  private Logger logger;

  @BeforeEach
  void attachAppender() {
    logger = (Logger) LoggerFactory.getLogger(AuditChecksumsSeamAnnouncer.class);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void detachAppender() {
    logger.detachAppender(appender);
  }

  // ---------------------------------------------------------------------------
  // AC-3: licenseStatus_reflects_auditChecksums_flag_andStartupWarnEmitted
  // (single test method per story it: block, covering both branches)
  // ---------------------------------------------------------------------------

  @Test
  void licenseStatus_reflects_auditChecksums_flag_andStartupWarnEmitted() {
    // --- Branch 1: feature disabled → INFO (no WARN) ---
    LicenseService disabledService = Mockito.mock(LicenseService.class);
    when(disabledService.isFeatureEnabled(WksFeature.AUDIT_CHECKSUMS)).thenReturn(false);

    new AuditChecksumsSeamAnnouncer(disabledService).announce();

    assertThat(appender.list).hasSize(1);
    ILoggingEvent disabledEvent = appender.list.get(0);
    assertThat(disabledEvent.getLevel())
        .as("feature disabled: should emit INFO, not WARN")
        .isEqualTo(Level.INFO);
    assertThat(disabledEvent.getFormattedMessage())
        .as("feature disabled: INFO message mentions audit.checksums feature")
        .contains("audit.checksums");

    appender.list.clear();

    // --- Branch 2: feature enabled → WARN with WKS-AUDIT-001 wire code ---
    LicenseService enabledService = Mockito.mock(LicenseService.class);
    when(enabledService.isFeatureEnabled(WksFeature.AUDIT_CHECKSUMS)).thenReturn(true);

    new AuditChecksumsSeamAnnouncer(enabledService).announce();

    assertThat(appender.list).hasSize(1);
    ILoggingEvent enabledEvent = appender.list.get(0);
    assertThat(enabledEvent.getLevel())
        .as("feature enabled: should emit WARN (chain-hash impl deferred)")
        .isEqualTo(Level.WARN);
    // WKS-AUDIT-001 wire string must be present verbatim — operators grep it in logs.
    assertThat(enabledEvent.getFormattedMessage())
        .as("feature enabled: WARN must contain WKS-AUDIT-001 wire code")
        .contains(AuditChecksumsSeamAnnouncer.WKS_AUDIT_001);
    assertThat(enabledEvent.getFormattedMessage())
        .as("feature enabled: WARN must mention chain-hash deferral")
        .contains("chain-hash");
    assertThat(enabledEvent.getFormattedMessage())
        .as("feature enabled: WARN must mention Epic 15")
        .contains("Epic 15");
  }
}
