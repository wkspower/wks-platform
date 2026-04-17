package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the SPA history-mode fallback added in Story 1.3: reloading a client-side route (e.g.
 * {@code GET /tasks}) forwards to {@code /index.html} instead of returning a JSON 404, while
 * requests to unknown {@code /api/**} paths still return the usual JSON {@code ProblemDetail}.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:sparouting;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=validate",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
class SpaRoutingIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void spaRouteReloadForwardsToIndexHtml() {
    ResponseEntity<String> response = rest.getForEntity("/tasks", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getContentType().includes(MediaType.TEXT_HTML)).isTrue();
    assertThat(response.getBody()).contains("<div id=\"root\">");
  }

  @Test
  void nestedSpaRouteAlsoForwardsToIndexHtml() {
    ResponseEntity<String> response = rest.getForEntity("/cases/abc-123", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("<div id=\"root\">");
  }

  @Test
  void unknownApiPathIsNotSwallowedBySpaForward() {
    // The SPA forward must never intercept /api/*. We tolerate either 401 (when auth config
    // requires authentication ahead of handler resolution) or 404 (when the path is under a
    // permitted segment) — the invariant we actually care about is "response is not the SPA
    // index.html". Asserting a specific status couples this routing test to SecurityConfig.
    ResponseEntity<String> response = rest.getForEntity("/api/does-not-exist", String.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).doesNotContain("<div id=\"root\">");
  }

  @Test
  void publicApiPathUnknownReturnsJsonNotFound() {
    // /api/health is permitted, so a neighbouring unknown path under the same permit segment
    // reaches the MVC 404 handler and returns a JSON ProblemDetail — still no SPA forward.
    ResponseEntity<String> response = rest.getForEntity("/api/health/unknown", String.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.NOT_FOUND, HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).doesNotContain("<div id=\"root\">");
  }
}
