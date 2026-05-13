package com.wkspower.platform.api.controller;

import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves an optional runtime CSS theme override file, gated by the {@code white-label} license
 * feature.
 *
 * <p>When {@link LicenseService#isFeatureEnabled(WksFeature) isFeatureEnabled(WHITE_LABEL)} is
 * {@code true} AND {@code WKS_THEME_CSS_PATH} is set, the file at that path is served as {@code
 * text/css}. The frontend injects it via a script that appends a {@code <link>} tag after all
 * Vite-bundled CSS links, ensuring any SI-provided {@code :root} overrides cascade over the
 * defaults in {@code tokens.css}.
 *
 * <p>When the {@code white-label} feature is disabled by the current license, the endpoint returns
 * <strong>404 Not Found</strong> — the custom override resource does not exist for unprivileged
 * tiers. The Vite plugin ({@code themeLinkLast} in {@code vite.config.ts}, Story 14-5) always emits
 * the {@code <link>} tag; the browser silently ignores the 404 and falls back to the bundled {@code
 * tokens.css}. This is the correct semantic: the resource simply doesn't exist; 403 would imply the
 * caller could retry with different credentials.
 *
 * <p>When the feature is enabled but no path is configured, returns an empty CSS body so the link
 * tag resolves without a browser error.
 *
 * <p>This endpoint is unauthenticated (it serves static CSS — no sensitive data) and is registered
 * as {@code permitAll} in {@link com.wkspower.platform.security.SecurityConfig}.
 *
 * <p>Guards (I1): the path must end with {@code .css} (case-insensitive) and the file must be at
 * most 1 MiB. Violations return empty CSS and are logged as warnings so operators can diagnose
 * misconfiguration.
 *
 * <p>Story 7-6 AC-1 — white-label theme gating.
 */
@RestController
@RequestMapping("/api")
public class ThemeController {

  private static final Logger log = LoggerFactory.getLogger(ThemeController.class);
  private static final long MAX_CSS_BYTES = 1_048_576L; // 1 MiB

  private final String themeCssPath;
  private final LicenseService licenseService;

  public ThemeController(
      @Value("${wks.theme.css-path:}") String themeCssPath, LicenseService licenseService) {
    this.themeCssPath = themeCssPath;
    this.licenseService = licenseService;
  }

  @PostConstruct
  void logResolvedPath() {
    if (themeCssPath == null || themeCssPath.isBlank()) {
      log.info(
          "ThemeController: WKS_THEME_CSS_PATH not configured — /api/theme.css returns empty CSS when white-label enabled");
    } else {
      log.info(
          "ThemeController: resolved theme CSS path = {} (served only when white-label feature enabled)",
          themeCssPath);
    }
  }

  @GetMapping(value = "/theme.css", produces = "text/css")
  @Operation(
      summary = "Runtime CSS theme override (white-label gated)",
      description =
          "Serves the SI-provided CSS override file (WKS_THEME_CSS_PATH) when the white-label"
              + " license feature is enabled. Returns 404 when the feature is disabled (the"
              + " resource does not exist for this tier); the frontend falls back to bundled"
              + " tokens.css silently. Returns empty CSS when enabled but no path configured.")
  @SecurityRequirements // public — no auth required
  public ResponseEntity<Resource> theme() throws IOException {
    // AC-1: gate on white-label license feature. 404 (not 403) — see class Javadoc.
    if (!licenseService.isFeatureEnabled(WksFeature.WHITE_LABEL)) {
      return ResponseEntity.notFound().build();
    }

    if (themeCssPath == null || themeCssPath.isBlank()) {
      return emptyCss();
    }

    // Guard I1a: path must end with .css
    if (!themeCssPath.toLowerCase().endsWith(".css")) {
      log.warn(
          "ThemeController: WKS_THEME_CSS_PATH='{}' does not end with .css — returning empty CSS",
          themeCssPath);
      return emptyCss();
    }

    Path path = Path.of(themeCssPath);
    if (!Files.exists(path) || !Files.isReadable(path)) {
      return emptyCss();
    }

    // Guard I1b: cap at 1 MiB
    long size = Files.size(path);
    if (size > MAX_CSS_BYTES) {
      log.warn(
          "ThemeController: theme file '{}' is {} bytes (> 1 MiB limit) — returning empty CSS",
          themeCssPath,
          size);
      return emptyCss();
    }

    byte[] content = Files.readAllBytes(path);
    return ResponseEntity.ok()
        .header("Content-Type", "text/css; charset=UTF-8")
        .header("Cache-Control", "public, max-age=300") // I2: 5-min browser cache
        .body(new ByteArrayResource(content));
  }

  private static ResponseEntity<Resource> emptyCss() {
    return ResponseEntity.ok()
        .header("Content-Type", "text/css; charset=UTF-8")
        .body(new ByteArrayResource(new byte[0]));
  }
}
