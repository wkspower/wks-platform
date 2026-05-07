package com.wkspower.platform.api.controller;

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
 * Serves an optional runtime CSS theme override file.
 *
 * <p>When {@code WKS_THEME_CSS_PATH} is set, the file at that path is served as {@code text/css}.
 * The frontend injects it via a script that appends a {@code <link>} tag after all Vite-bundled CSS
 * links, ensuring any SI-provided {@code :root} overrides cascade over the defaults in {@code
 * tokens.css}.
 *
 * <p>Returns {@code 204 No Content} when no theme file is configured — the browser ignores an empty
 * stylesheet link gracefully.
 *
 * <p>This endpoint is unauthenticated (it serves static CSS — no sensitive data) and is registered
 * as {@code permitAll} in {@link com.wkspower.platform.security.SecurityConfig}.
 *
 * <p>Guards (I1): the path must end with {@code .css} (case-insensitive) and the file must be at
 * most 1 MiB. Violations return 204 and are logged as warnings so operators can diagnose
 * misconfiguration.
 */
@RestController
@RequestMapping("/api")
public class ThemeController {

  private static final Logger log = LoggerFactory.getLogger(ThemeController.class);
  private static final long MAX_CSS_BYTES = 1_048_576L; // 1 MiB

  private final String themeCssPath;

  public ThemeController(@Value("${wks.theme.css-path:}") String themeCssPath) {
    this.themeCssPath = themeCssPath;
  }

  @PostConstruct
  void logResolvedPath() {
    if (themeCssPath == null || themeCssPath.isBlank()) {
      log.info("ThemeController: WKS_THEME_CSS_PATH not configured — /api/theme.css returns 204");
    } else {
      log.info("ThemeController: resolved theme CSS path = {}", themeCssPath);
    }
  }

  @GetMapping(value = "/theme.css", produces = "text/css")
  @Operation(
      summary = "Runtime CSS theme override",
      description =
          "Serves the SI-provided CSS override file (WKS_THEME_CSS_PATH). Returns 204 when not"
              + " configured.")
  @SecurityRequirements // public — no auth required
  public ResponseEntity<Resource> theme() throws IOException {
    if (themeCssPath == null || themeCssPath.isBlank()) {
      return ResponseEntity.noContent().build();
    }

    // Guard I1a: path must end with .css
    if (!themeCssPath.toLowerCase().endsWith(".css")) {
      log.warn(
          "ThemeController: WKS_THEME_CSS_PATH='{}' does not end with .css — returning 204",
          themeCssPath);
      return ResponseEntity.noContent().build();
    }

    Path path = Path.of(themeCssPath);
    if (!Files.exists(path) || !Files.isReadable(path)) {
      return ResponseEntity.noContent().build();
    }

    // Guard I1b: cap at 1 MiB
    long size = Files.size(path);
    if (size > MAX_CSS_BYTES) {
      log.warn(
          "ThemeController: theme file '{}' is {} bytes (> 1 MiB limit) — returning 204",
          themeCssPath,
          size);
      return ResponseEntity.noContent().build();
    }

    byte[] content = Files.readAllBytes(path);
    return ResponseEntity.ok()
        .header("Content-Type", "text/css; charset=UTF-8")
        .header("Cache-Control", "public, max-age=300") // I2: 5-min browser cache
        .body(new ByteArrayResource(content));
  }
}
