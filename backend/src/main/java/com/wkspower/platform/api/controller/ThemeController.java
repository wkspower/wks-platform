package com.wkspower.platform.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * The frontend loads it via {@code <link rel="stylesheet" href="/api/theme.css">} in {@code
 * index.html}. Because the link tag is placed after the Tailwind-bridged tokens, any SI-provided
 * {@code :root} overrides cascade over the defaults declared in {@code tokens.css}.
 *
 * <p>Returns {@code 204 No Content} when no theme file is configured — the browser ignores an empty
 * stylesheet link gracefully.
 *
 * <p>This endpoint is unauthenticated (it serves static CSS — no sensitive data) and is registered
 * as {@code permitAll} in {@link com.wkspower.platform.security.SecurityConfig}.
 */
@RestController
@RequestMapping("/api")
public class ThemeController {

  private final String themeCssPath;

  public ThemeController(@Value("${wks.theme.css-path:}") String themeCssPath) {
    this.themeCssPath = themeCssPath;
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
    Path path = Path.of(themeCssPath);
    if (!Files.exists(path) || !Files.isReadable(path)) {
      return ResponseEntity.noContent().build();
    }
    byte[] content = Files.readAllBytes(path);
    return ResponseEntity.ok()
        .header("Content-Type", "text/css; charset=UTF-8")
        .body(new ByteArrayResource(content));
  }
}
