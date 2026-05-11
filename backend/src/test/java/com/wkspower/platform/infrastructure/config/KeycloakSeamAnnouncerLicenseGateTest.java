package com.wkspower.platform.infrastructure.config;

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
 * Story 7-5 AC5 — verifies the announcer reads the gate from {@link LicenseService} (not the
 * removed {@code wks.keycloak.enabled} env property) and emits the documented log lines for each
 * branch. The WARN wire string {@code WKS-AUTH-001} is asserted verbatim because operators grep for
 * it (Story 14.1.1 finding C4).
 */
class KeycloakSeamAnnouncerLicenseGateTest {

  private ListAppender<ILoggingEvent> appender;
  private Logger logger;

  @BeforeEach
  void attachAppender() {
    logger = (Logger) LoggerFactory.getLogger(KeycloakSeamAnnouncer.class);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void detachAppender() {
    logger.detachAppender(appender);
  }

  @Test
  void disabledFeatureEmitsInfoLine() {
    LicenseService service = Mockito.mock(LicenseService.class);
    when(service.isFeatureEnabled(WksFeature.AUTH_SSO)).thenReturn(false);

    new KeycloakSeamAnnouncer(service).announce();

    assertThat(appender.list).hasSize(1);
    ILoggingEvent event = appender.list.get(0);
    assertThat(event.getLevel()).isEqualTo(Level.INFO);
    assertThat(event.getFormattedMessage())
        .isEqualTo(
            "WKS auth: built-in cookie-JWT (auth.sso disabled by license — Enterprise tier"
                + " unlocks SSO; Story 10.4 will deliver the SAML provider).");
  }

  @Test
  void enabledFeatureEmitsWarnWireString() {
    LicenseService service = Mockito.mock(LicenseService.class);
    when(service.isFeatureEnabled(WksFeature.AUTH_SSO)).thenReturn(true);

    new KeycloakSeamAnnouncer(service).announce();

    assertThat(appender.list).hasSize(1);
    ILoggingEvent event = appender.list.get(0);
    assertThat(event.getLevel()).isEqualTo(Level.WARN);
    // WKS-AUTH-001 wire string preserved verbatim — Story 14.1.1 AC3 finding C4.
    assertThat(event.getFormattedMessage())
        .isEqualTo(
            "WKS-AUTH-001: Keycloak seam is currently INERT — built-in cookie-JWT remains the"
                + " sole auth gate. SSO enforcement requires Story 10.4 (SAML via Keycloak realm)"
                + " to land. To enforce auth posture, do NOT rely on this flag.");
  }
}
