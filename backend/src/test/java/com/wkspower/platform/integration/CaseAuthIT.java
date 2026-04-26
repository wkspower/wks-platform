package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import java.util.List;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * Story 2.3 D5c — real-JWT integration coverage. Closes the Story 2.2 deferred entry "JWT
 * role-prefix integration coverage" by exercising the full filter chain ({@code
 * JwtAuthenticationFilter} → {@code WksUserPrincipal} → {@code CaseTypePermissionEvaluator})
 * end-to-end with a real session cookie.
 *
 * <p>Pinned scenarios:
 *
 * <ul>
 *   <li>Anonymous request → 401 with {@code WKS-API-401}.
 *   <li>Authenticated user with the {@code admin} role + matching YAML grant → 200 (the {@code
 *       ROLE_OFFICER}/case-folding bridge from Story 1.2 is exercised: filter stamps {@code
 *       ROLE_ADMIN}, evaluator matches against the lowercase {@code admin} YAML role name).
 *   <li>Authenticated user requesting an unknown case type → 403 (per code-review P4: the evaluator
 *       returns false for missing case-types rather than throwing from the gate).
 * </ul>
 *
 * <p>Multi-role-distinction scenarios (e.g. viewer-can't-create) are deferred until the seed
 * exposes a non-admin role; the slice-level {@code CaseTypePermissionEvaluatorTest} covers that
 * branch in unit form today.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:caseauth;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
class CaseAuthIT {

  private static final String EMAIL = "auth-it-admin@wkspower.local";
  private static final String PASSWORD = "pw-auth-it";
  private static final String CASE_TYPE_ID = "loan-application";

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;

  @BeforeEach
  void setup() {
    if (users.findByEmail(EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
    registry.register(loanType());
  }

  @Test
  void anonymousRequestReturns401WithWksCode() {
    ResponseEntity<String> resp =
        rest.exchange(
            "/api/cases?caseType=" + CASE_TYPE_ID,
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(resp.getBody()).contains("WKS-API-401");
  }

  @Test
  void authenticatedAdminWithGrantingYamlRolePasses() {
    String cookie = login(EMAIL);

    ResponseEntity<String> resp =
        exchange("/api/cases?caseType=" + CASE_TYPE_ID, HttpMethod.GET, cookie);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void authenticatedRequestForUnknownCaseTypeReturnsForbidden() {
    String cookie = login(EMAIL);

    // Per code-review P4: the evaluator returns false for missing case-types — the gate fires
    // before the service can 404, so callers see 403. Pin that contract here.
    ResponseEntity<String> resp =
        exchange("/api/cases?caseType=does-not-exist", HttpMethod.GET, cookie);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private String login(String email) {
    ResponseEntity<String> resp =
        rest.postForEntity("/api/auth/login", new LoginRequest(email, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).as("login for %s", email).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    int semi = setCookie.indexOf(';');
    return semi > 0 ? setCookie.substring(0, semi) : setCookie;
  }

  private ResponseEntity<String> exchange(String path, HttpMethod method, String cookie) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(path, method, new HttpEntity<>(headers), String.class);
  }

  private static CaseTypeConfig loanType() {
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Loan Application",
        1,
        null,
        new WorkflowRef(CASE_TYPE_ID + ".bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("admin", List.of(Permission.VIEW, Permission.CREATE))));
  }
}
