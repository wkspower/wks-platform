package com.wkspower.platform.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Announces the Keycloak/SSO compose seam on production-profile startup (Story 14.1 AC5).
 *
 * <p>Two switches gate the seam: the compose flag {@code --profile production-sso} (provisions the
 * Keycloak container) and the env-driven property {@code wks.keycloak.enabled} ({@code
 * WKS_KEYCLOAK_ENABLED}). When the property is false (default) we log an INFO line stating built-in
 * auth (Story 1.2 cookie-JWT) remains active. When the property is true we additionally emit {@code
 * WKS-AUTH-001} at WARN to make explicit that the Keycloak container has been provisioned but
 * auth-provider integration is gated on Story 10.4 (SSO/SAML via Keycloak).
 *
 * <p>This class deliberately has NO dependency on {@code LicenseService} — Epic 7 has not shipped.
 * Story 7.3 (Feature Flag Registry) will subsume {@code wks.keycloak.enabled} under {@code
 * feature.sso} with license-tier gating; until then this property is the operator's direct toggle.
 */
@Component
@Profile("production")
public class KeycloakSeamAnnouncer {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakSeamAnnouncer.class);

  private final Environment env;

  public KeycloakSeamAnnouncer(Environment env) {
    this.env = env;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void announce() {
    boolean enabled = env.getProperty("wks.keycloak.enabled", Boolean.class, Boolean.FALSE);
    if (enabled) {
      LOG.warn(
          "WKS-AUTH-001: Keycloak container provisioned but auth-provider integration is gated on"
              + " Epic 7 (Story 10.4 — SSO/SAML via Keycloak, license-gated). Built-in auth"
              + " remains active.");
    } else {
      LOG.info(
          "WKS auth: built-in (Keycloak/SSO disabled — set WKS_KEYCLOAK_ENABLED=true and run"
              + " with --profile production-sso to enable; license-gated in Epic 7).");
    }
  }
}
