package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * End-to-end auth flow over HTTP with a real Flyway schema and in-memory H2. Exercises the full
 * cookie round-trip: login → protected call → logout → re-check.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:authflow;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=validate",
      // Deterministic 32-byte Base64 secret (trivial; test-only).
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
class AuthFlowIT {

  private static final String EMAIL = "admin@wkspower.local";
  private static final String PASSWORD = "admin";

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;

  @BeforeEach
  void ensureAdmin() {
    if (!users.existsWithRole("admin")) {
      users.save(
          new User(UUID.randomUUID(), EMAIL, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
  }

  @Test
  void loginMeLogoutRoundTrip() {
    // --- Login ---
    ResponseEntity<String> login =
        rest.postForEntity("/api/auth/login", new LoginRequest(EMAIL, PASSWORD), String.class);

    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    String setCookie = login.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookie).isNotNull();
    assertThat(setCookie).contains("WKS_SESSION=");
    assertThat(setCookie).contains("HttpOnly");
    assertThat(setCookie).contains("SameSite=Lax");

    String sessionCookie = cookieHeaderValue(setCookie);

    // --- /api/auth/me (authenticated) ---
    ResponseEntity<String> me = exchange("/api/auth/me", HttpMethod.GET, sessionCookie, null);
    assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(me.getBody()).contains(EMAIL);

    // --- A protected unmapped endpoint: filter chain must 401 before 404 ---
    ResponseEntity<String> unauth =
        exchange("/api/cases", HttpMethod.GET, /* no cookie */ null, null);
    assertThat(unauth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(unauth.getBody()).contains("WKS-API-401");

    // --- Logout ---
    ResponseEntity<String> logout =
        exchange("/api/auth/logout", HttpMethod.POST, sessionCookie, null);
    assertThat(logout.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    String clearCookie = logout.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(clearCookie).contains("Max-Age=0");

    // --- Re-call /me with the cookie that the server just asked us to drop: 401 ---
    ResponseEntity<String> afterLogout =
        exchange("/api/auth/me", HttpMethod.GET, cookieHeaderValue(clearCookie), null);
    assertThat(afterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void loginFailureReturns401WithWksCode() {
    ResponseEntity<String> resp =
        rest.postForEntity(
            "/api/auth/login", new LoginRequest(EMAIL, "wrong-password"), String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(resp.getBody()).contains("WKS-API-401");
  }

  private ResponseEntity<String> exchange(
      String path, HttpMethod method, String cookie, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (cookie != null) {
      headers.add(HttpHeaders.COOKIE, cookie);
    }
    return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
  }

  private static String cookieHeaderValue(String setCookieHeader) {
    int semi = setCookieHeader.indexOf(';');
    return semi > 0 ? setCookieHeader.substring(0, semi) : setCookieHeader;
  }
}
