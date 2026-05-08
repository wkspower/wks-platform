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
 * Story 2.5 AC9 — full JWT round-trip for {@code /api/case-types}. A user with the {@code officer}
 * role gets {@code view} on {@code loan-application} only; the second case type ({@code
 * hr-onboarding}) grants {@code view} to a different role. Asserts:
 *
 * <ul>
 *   <li>Anonymous {@code GET /api/case-types} → 401.
 *   <li>Authenticated officer sees one entry on the list (filtered to verb-holders).
 *   <li>{@code GET /api/case-types/loan-application} → 200; full {@link
 *       com.wkspower.platform.api.dto.response.CaseTypeViewDto} echoed.
 *   <li>{@code GET /api/case-types/hr-onboarding} → 403 (verb missing).
 *   <li>{@code GET /api/case-types/missing} → 404.
 * </ul>
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:casetypeit;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
class CaseTypeControllerIT {

  private static final String EMAIL = "case-types-it-officer@wkspower.local";
  private static final String PASSWORD = "admin";

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
    registry.register(loanTypeForOfficer());
    registry.register(hrTypeForHr());
  }

  @Test
  void anonymousListReturns401() {
    ResponseEntity<String> resp =
        rest.exchange(
            "/api/case-types", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void officerSeesOnlyCaseTypesGrantingViewVerb() {
    String cookie = login(EMAIL);

    ResponseEntity<String> resp = exchange("/api/case-types", HttpMethod.GET, cookie);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getBody())
        .contains("\"id\":\"loan-application\"")
        .doesNotContain("\"id\":\"hr-onboarding\"");
  }

  @Test
  void officerCanReadGrantedCaseTypeDetail() {
    String cookie = login(EMAIL);

    ResponseEntity<String> resp =
        exchange("/api/case-types/loan-application", HttpMethod.GET, cookie);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getBody())
        .contains("\"id\":\"loan-application\"")
        .contains("\"displayName\":\"Loan Application\"")
        .contains("\"fields\":[")
        .contains("\"statuses\":[")
        .contains("\"listColumns\":[")
        .doesNotContain("\"roles\":");
  }

  @Test
  void officerForbiddenOnUngrantedCaseType() {
    String cookie = login(EMAIL);

    ResponseEntity<String> resp = exchange("/api/case-types/hr-onboarding", HttpMethod.GET, cookie);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void summaryIncludesPermissionsForCaller() {
    // P28 / AC9 — the case-types list response must include the caller's verbs per case-type so
    // the frontend dropdown can filter to creatable types client-side without a second round-trip.
    String cookie = login(EMAIL);

    ResponseEntity<String> resp = exchange("/api/case-types", HttpMethod.GET, cookie);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String body = resp.getBody();
    assertThat(body).isNotNull();
    assertThat(body)
        .contains("\"id\":\"loan-application\"")
        .contains("\"permissions\":[\"view\"]")
        .doesNotContain("\"hr-onboarding\"");
  }

  @Test
  void unknownCaseTypeReturns404() {
    String cookie = login(EMAIL);

    ResponseEntity<String> resp = exchange("/api/case-types/missing", HttpMethod.GET, cookie);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(resp.getBody()).contains("WKS-API-404");
  }

  // ---- helpers ----------------------------------------------------------

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

  private static CaseTypeConfig loanTypeForOfficer() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("admin", List.of(Permission.VIEW))),
        List.of(),
        List.of());
  }

  private static CaseTypeConfig hrTypeForHr() {
    return new CaseTypeConfig(
        "hr-onboarding",
        "HR Onboarding",
        1,
        null,
        new WorkflowRef("hr-onboarding.bpmn"),
        List.of(
            new FieldDefinition("employee", "Employee", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("draft", "Draft", StatusColor.ZINC)),
        List.of("employee"),
        List.of(new RoleDefinition("hr", List.of(Permission.VIEW))),
        List.of(),
        List.of());
  }
}
