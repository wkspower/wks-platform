package com.wkspower.platform.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Story 7-6 AC-1 — White-label theme gating integration test.
 *
 * <p>Verifies that {@code GET /api/theme.css} returns the custom CSS override when the {@code
 * white-label} feature is enabled, and returns {@code 404 Not Found} (NOT 403) when the feature is
 * disabled.
 *
 * <p>Uses a temporary theme.css file created per-test-class to avoid classpath coupling.
 *
 * <p>AC-4 compliance: imports {@code SecurityConfig} transitively via {@code @SpringBootTest};
 * {@code @MockitoBean LicenseService} is applied to this class.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:whitelabelgating;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false"
    })
class WhiteLabelGatingIT {

  /** Temporary CSS file shared across all tests in this class. */
  private static Path tempThemeCssFile;

  @Autowired private TestRestTemplate rest;

  /** AC-4: @MockitoBean LicenseService on every SecurityConfig-importing slice. */
  @MockitoBean private LicenseService licenseService;

  @BeforeAll
  static void createTempThemeFile() throws IOException {
    tempThemeCssFile = Files.createTempFile("wks-test-theme-", ".css");
    Files.writeString(tempThemeCssFile, ":root { --primary: #123456; }");
  }

  @AfterAll
  static void deleteTempThemeFile() throws IOException {
    if (tempThemeCssFile != null) {
      Files.deleteIfExists(tempThemeCssFile);
    }
  }

  @DynamicPropertySource
  static void registerThemeCssPath(DynamicPropertyRegistry registry) {
    registry.add(
        "wks.theme.css-path", () -> tempThemeCssFile != null ? tempThemeCssFile.toString() : "");
  }

  // ---------------------------------------------------------------------------
  // AC-1: customTheme_served_whenFeatureEnabled_404_whenDisabled
  // (single test method exercising both positive and negative gate paths)
  // ---------------------------------------------------------------------------

  @Test
  void customTheme_served_whenFeatureEnabled_404_whenDisabled() {
    // --- Positive path: feature enabled → custom CSS served ---
    when(licenseService.isFeatureEnabled(WksFeature.WHITE_LABEL)).thenReturn(true);

    ResponseEntity<String> enabledResponse = rest.getForEntity("/api/theme.css", String.class);
    assertThat(enabledResponse.getStatusCode())
        .as("white-label enabled: expect 200 OK with CSS body")
        .isEqualTo(HttpStatus.OK);
    assertThat(enabledResponse.getBody())
        .as("white-label enabled: CSS body should contain the override content")
        .contains("--primary: #123456");
    assertThat(enabledResponse.getHeaders().getFirst("Content-Type"))
        .as("white-label enabled: content-type should be text/css")
        .contains("text/css");

    // --- Negative path: feature disabled → 404 (NOT 403) ---
    when(licenseService.isFeatureEnabled(WksFeature.WHITE_LABEL)).thenReturn(false);

    ResponseEntity<String> disabledResponse = rest.getForEntity("/api/theme.css", String.class);
    assertThat(disabledResponse.getStatusCode())
        .as(
            "white-label disabled: expect 404 Not Found (resource doesn't exist for this tier;"
                + " NOT 403 which would imply auth failure)")
        .isEqualTo(HttpStatus.NOT_FOUND);
  }
}
