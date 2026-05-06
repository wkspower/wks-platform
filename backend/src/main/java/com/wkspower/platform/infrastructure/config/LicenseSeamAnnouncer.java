package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.service.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Announces the license seam on production-profile startup (Story 7.1 AC1).
 *
 * <p>Mirrors the pattern of {@link KeycloakSeamAnnouncer} exactly: {@code @Component @Profile
 * ("production")} + {@code @EventListener(ApplicationReadyEvent.class)}. This class deliberately
 * does NOT modify {@code KeycloakSeamAnnouncer} — that class is untouched per Story 7.1 scope
 * boundary.
 *
 * <p>Logs tier + expiry from {@link LicenseService} so operators can confirm the license was
 * accepted at boot without reading application logs at DEBUG level.
 */
@Component
@Profile("production")
public class LicenseSeamAnnouncer {

  private static final Logger LOG = LoggerFactory.getLogger(LicenseSeamAnnouncer.class);

  private final LicenseService licenseService;

  public LicenseSeamAnnouncer(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void announce() {
    String tier = licenseService.getTier();
    if ("oss".equals(tier)) {
      LOG.info(
          "{} WKS license: no valid license loaded — operating in OSS mode."
              + " Set WKS_LICENSE_FILE to the path of a signed license JWT to enable EE features.",
          ErrorCode.WKS_LIC_001.wire());
    } else {
      LOG.info(
          "WKS license active — tier={}, holder={}, expires={}",
          tier,
          licenseService.getLicenseHolder(),
          licenseService.getExpiry() != null ? licenseService.getExpiry().toString() : "never");
    }
  }
}
