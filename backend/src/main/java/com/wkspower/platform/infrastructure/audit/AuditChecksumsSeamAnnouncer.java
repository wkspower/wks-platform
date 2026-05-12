package com.wkspower.platform.infrastructure.audit;

import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Announces the audit-checksums seam on every startup.
 *
 * <p>Mirrors the pattern of {@link
 * com.wkspower.platform.infrastructure.config.KeycloakSeamAnnouncer}: {@code @Component} +
 * {@code @EventListener(ApplicationReadyEvent.class)} — deliberately NOT
 * {@code @Profile("production")} so the WARN is visible in all environments (operators running in
 * dev mode see the same honest message).
 *
 * <p>Story 7-6 AC-3: the {@code audit.checksums} feature flag is observable via {@code GET
 * /api/license/features} (Story 7-4 surface; no code change needed here — the {@link
 * WksFeature#AUDIT_CHECKSUMS} constant registered in Story 7-3 is automatically projected).
 * However, chain-hash crypto (tamper-evident audit-row linking) is deferred to Epic 15; this
 * announcer emits the honest WARN so operators who enable the feature on their license know the
 * cryptographic guarantee is not yet active.
 *
 * <p>When Story 15-N ships the chain-hash implementation, this WARN line is replaced by an INFO
 * confirming the chain-hash is active, and the chain-hash field is added to audit rows.
 *
 * <p>Wire code: {@code WKS-AUDIT-001} — first member of the WKS-AUDIT band. Stable contract per
 * {@code feedback_error_codes_are_wire_contract.md}; operators grep this code in logs.
 */
@Component
public class AuditChecksumsSeamAnnouncer {

  private static final Logger LOG = LoggerFactory.getLogger(AuditChecksumsSeamAnnouncer.class);

  /** Stable wire code for the audit-checksums deferral announcement. */
  public static final String WKS_AUDIT_001 = "WKS-AUDIT-001";

  private final LicenseService licenseService;

  public AuditChecksumsSeamAnnouncer(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void announce() {
    boolean enabled = licenseService.isFeatureEnabled(WksFeature.AUDIT_CHECKSUMS);
    if (enabled) {
      LOG.warn(
          "{} audit.checksums is a registered feature flag but chain-hash implementation is"
              + " deferred to Epic 15. Audit rows are NOT cryptographically chained on this build."
              + " The flag is observable via GET /api/license/features but provides no"
              + " tamper-evidence until the chain-hash implementation lands in 15-N.",
          WKS_AUDIT_001);
    } else {
      LOG.info(
          "WKS audit checksums: audit.checksums feature disabled by current license."
              + " Chain-hash implementation deferred to Epic 15 (15-N).");
    }
  }
}
