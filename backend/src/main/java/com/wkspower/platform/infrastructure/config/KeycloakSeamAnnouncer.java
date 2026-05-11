package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Announces the Keycloak/SSO compose seam on production-profile startup.
 *
 * <p>The seam is gated by {@link LicenseService#isFeatureEnabled(WksFeature)
 * isFeatureEnabled(AUTH_SSO)}. When the feature is off (OSS / Team / Expired / Degraded), an INFO
 * line states built-in cookie-JWT (Story 1.2) remains the sole auth gate. When the feature is on
 * (Enterprise / Demo with a VALID license), {@code WKS-AUTH-001} is emitted at WARN to make
 * explicit that the SAML provider is still gated on Story 10.4 — the license unlocks the surface
 * but no enforcement code exists yet.
 *
 * <p>Story 7-5 (this story) flips the gate source from the operator-direct {@code
 * wks.keycloak.enabled} env property to {@link LicenseService}; Story 10.4 will deliver the SAML
 * provider and replace the 401 fallthrough behind the gate.
 */
@Component
@Profile("production")
public class KeycloakSeamAnnouncer {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakSeamAnnouncer.class);

  private final LicenseService licenseService;

  public KeycloakSeamAnnouncer(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void announce() {
    boolean enabled = licenseService.isFeatureEnabled(WksFeature.AUTH_SSO);
    if (enabled) {
      // Story 14.1.1 AC3 (finding C4): the WARN message MUST be explicit that the
      // seam is INERT today and that flipping the flag changes NOTHING about auth
      // enforcement. The previous message said "auth-provider integration is gated
      // on Epic 7" — too soft; operators were inferring "this WILL enforce SSO once
      // I set the second flag" and shipping production with built-in cookie-JWT
      // believing the SSO posture was active. The wire string `WKS-AUTH-001` is
      // preserved verbatim — operators grep it.
      LOG.warn(
          "WKS-AUTH-001: Keycloak seam is currently INERT — built-in cookie-JWT remains the sole"
              + " auth gate. SSO enforcement requires Story 10.4 (SAML via Keycloak realm) to"
              + " land. To enforce auth posture, do NOT rely on this flag.");
    } else {
      LOG.info(
          "WKS auth: built-in cookie-JWT (auth.sso disabled by license — Enterprise tier unlocks"
              + " SSO; Story 10.4 will deliver the SAML provider).");
    }
  }
}
