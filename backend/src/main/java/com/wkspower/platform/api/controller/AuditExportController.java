package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.ErrorPayload;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Audit-export endpoint — license-gated with an honest-deferral 501 stub.
 *
 * <p>Story 7-6 AC-2: {@code POST /api/admin/audit/export} is registered and license-gated today.
 * The gate is real and tested. The underlying export capability is deferred to Epic 15 (Story
 * 15-7-audit-export-polish). When the feature is disabled, the endpoint returns 403 + {@code
 * WKS-LIC-005}. When the feature is enabled, the endpoint returns 501 + {@code WKS-API-501}
 * (honest-deferral stub) with a tracking-story reference.
 *
 * <p>A startup-time {@code WARN} log line announces the stub is registered, mirroring the {@link
 * com.wkspower.platform.infrastructure.config.KeycloakSeamAnnouncer} pattern from Story 14-1 /
 * WKS-AUTH-001. Operators who enable the {@code audit.export} feature and see 501s can correlate
 * with this WARN to understand the deferral.
 *
 * <p>Full implementation (CSV/PDF export, date-range filtering, streaming) lands in Story
 * 15-7-audit-export-polish once Epic 15's {@code domain/audit/} canonical package is established.
 */
@RestController
@RequestMapping("/api/admin/audit")
public class AuditExportController {

  private static final Logger LOG = LoggerFactory.getLogger(AuditExportController.class);

  static final String TRACKING_STORY = "15-7-audit-export-polish";
  static final String DEFERRED_MESSAGE =
      "Audit export is gated and unlocked, but full implementation lands in Epic 15.7."
          + " This endpoint is a honest-deferral stub.";

  /** Request body shape for audit export. */
  public record AuditExportRequest(String caseId, String format, String from, String to) {}

  private final LicenseService licenseService;

  public AuditExportController(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  /**
   * Announces the honest-deferral stub on startup. Mirrors {@link
   * com.wkspower.platform.infrastructure.config.KeycloakSeamAnnouncer}: operators can confirm the
   * seam is registered and understand that the full implementation is deferred.
   */
  @PostConstruct
  void announceStub() {
    LOG.warn(
        "WKS-API-501: POST /api/admin/audit/export is registered as a license-gated stub."
            + " The audit.export feature flag gates access, but full export implementation"
            + " (CSV/PDF, streaming, chain-hash) is deferred to Story {}."
            + " Callers with audit.export enabled will receive 501 until that story lands.",
        TRACKING_STORY);
  }

  /**
   * Audit export — gated by {@code audit.export} license feature.
   *
   * <p>Returns 403 + {@code WKS-LIC-005} when the feature is disabled. Returns 501 + {@code
   * WKS-API-501} when the feature is enabled (honest-deferral stub).
   */
  @PostMapping("/export")
  @Operation(
      summary = "Audit log export (license-gated stub)",
      description =
          "POST /api/admin/audit/export — gated by the audit.export license feature."
              + " Returns 403 when the feature is disabled (WKS-LIC-005)."
              + " Returns 501 when enabled — honest-deferral stub; full implementation"
              + " tracked in Story 15-7-audit-export-polish.")
  public ResponseEntity<ApiResponse<Void>> export(
      @RequestBody(required = false) AuditExportRequest request) {

    if (!licenseService.isFeatureEnabled(WksFeature.AUDIT_EXPORT)) {
      // Feature not enabled for this license tier — 403 with gating feature key in body.
      // field slot carries the feature key so SI clients can identify which gate blocked access.
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.error(
                  ErrorPayload.ofField(
                      ErrorCode.WKS_LIC_005.wire(),
                      "Audit export requires the audit.export license feature.",
                      "audit.export")));
    }

    // Feature is enabled but implementation is deferred — 501 honest-deferral stub.
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .body(
            ApiResponse.error(
                ErrorPayload.of(
                    ErrorCode.WKS_API_501.wire(),
                    DEFERRED_MESSAGE + " trackingStory=" + TRACKING_STORY)));
  }
}
