package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.LicenseSnapshot;
import com.wkspower.platform.domain.service.LicenseState;
import com.wkspower.platform.domain.service.WksFeature;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the current license state to authenticated frontend clients.
 *
 * <p>{@code GET /api/license/status} returns a {@link LicenseStatusDto} so the frontend can render
 * an appropriate banner for {@code expired} and {@code degraded} states. The {@code oss} state (no
 * license file present) is the expected default — no banner is shown for it.
 *
 * <p>Accessible to all authenticated users. No {@code @PreAuthorize} needed — {@code
 * SecurityConfig} grants all {@code /api/**} requests to authenticated users by default.
 */
@RestController
@RequestMapping("/api/license")
public class LicenseController {

  /**
   * Wire-stable DTO for the license status response.
   *
   * @param state one of {@code "valid" | "oss" | "expired" | "degraded"}
   * @param tier the active tier string (e.g. {@code "oss"}, {@code "team"}, {@code "enterprise"})
   * @param expiredAt ISO-8601 instant string when state is {@code "expired"}; {@code null}
   *     otherwise
   */
  public record LicenseStatusDto(String state, String tier, String expiredAt) {}

  /**
   * Wire shape for one feature entry in the debug response.
   *
   * @param key the stable feature wire string
   * @param description human-readable description
   * @param bundleTiers tier names whose bundles include this feature by default
   * @param enabled whether the feature is enabled under the current license
   */
  public record LicenseFeatureView(
      String key, String description, Set<String> bundleTiers, boolean enabled) {}

  /**
   * Response body for {@code GET /api/license/features}.
   *
   * @param tier the active tier string for the current license
   * @param features all registered features with coverage and enabled state
   */
  public record LicenseFeaturesDto(String tier, List<LicenseFeatureView> features) {}

  private final LicenseService licenseService;

  public LicenseController(LicenseService licenseService) {
    this.licenseService = licenseService;
  }

  @GetMapping("/status")
  @Operation(
      summary = "License status",
      description =
          "Returns the current license state, tier, and expiry (if expired). Accessible to all"
              + " authenticated users. No banner is shown by the frontend for the 'oss' state.")
  public ApiResponse<LicenseStatusDto> status(HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-store");

    LicenseSnapshot snap = licenseService.getLicenseSnapshot();
    String stateStr = snap.licenseState().toWireString();
    String expiredAt =
        (snap.licenseState() == LicenseState.EXPIRED && snap.expiry() != null)
            ? DateTimeFormatter.ISO_INSTANT.format(snap.expiry()) // CF2: ISO_INSTANT format
            : null;

    return ApiResponse.success(new LicenseStatusDto(stateStr, snap.tier(), expiredAt));
  }

  /**
   * Lists all registered license-gated features with their default tier bundles and current enabled
   * state under the active license.
   *
   * <p>Accessible to all authenticated users. Admin-only gating is deferred to a future security
   * story.
   */
  @GetMapping("/features")
  @Operation(
      summary = "License feature registry (debug)",
      description =
          "Lists all registered license-gated features with their default tier bundles and current"
              + " enabled state under the active license. Useful for operator debugging.")
  public ApiResponse<LicenseFeaturesDto> features(HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-store");

    String activeTier = licenseService.getTier();
    List<LicenseFeatureView> featureViews =
        Arrays.stream(WksFeature.values())
            .map(
                f ->
                    new LicenseFeatureView(
                        f.key(),
                        f.description(),
                        f.defaultTiers(),
                        licenseService.isFeatureEnabled(f)))
            .toList();

    return ApiResponse.success(new LicenseFeaturesDto(activeTier, featureViews));
  }
}
