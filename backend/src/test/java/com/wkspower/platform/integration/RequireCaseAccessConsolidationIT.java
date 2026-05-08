package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * Story 5.6 AC-0 — proves that {@code POST /api/cases/{id}/forms/{f}/submit} AND {@code PUT
 * /api/cases/{id}/forms/{f}/draft} both go through {@link
 * com.wkspower.platform.domain.service.CaseService#requireCaseAccess(java.util.UUID,
 * java.util.UUID, java.util.Set)}: missing case yields the same {@code WksNotFoundException}
 * envelope on both endpoints.
 *
 * <p>Sprint 8 retro Action 2 — single source of truth for case-level access on write-side
 * controllers. Postgres-IT parity status: H2-only in this story (matches the pre-existing {@code
 * FormSubmitIT} pattern); full Postgres-IT migration is carried forward (memory {@code
 * project_postgres_it_parity_gap.md}).
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md}: production-validation disabled (this
 * IT does not exercise the validator).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:requireaccessit;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false"
    })
class RequireCaseAccessConsolidationIT {

  private static final String EMAIL = "require-access-it@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "require-access-fixture";
  private static final String FORM_ID = "intake-form";

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private ObjectMapper json;

  @BeforeEach
  void setup() {
    if (users.findByEmail(EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
    registry.register(caseTypeWithForm());
  }

  @Test
  void formSubmitOnMissingCaseReturns404() {
    String cookie = login();
    UUID missing = UUID.randomUUID();

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + missing + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\"}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(resp.getBody()).contains("WKS-API-404");
    assertThat(resp.getBody()).contains(missing.toString());
  }

  @Test
  void formDraftPutOnMissingCaseReturns404() {
    String cookie = login();
    UUID missing = UUID.randomUUID();

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + missing + "/forms/" + FORM_ID + "/draft",
            HttpMethod.PUT,
            cookie,
            "{\"payload\":{},\"scrollY\":0,\"sectionExpanded\":{},\"caseTypeVersionAtSave\":1}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(resp.getBody()).contains("WKS-API-404");
    assertThat(resp.getBody()).contains(missing.toString());
  }

  // ---- helpers ---------------------------------------------------------------

  private String login() {
    ResponseEntity<String> resp =
        rest.postForEntity("/api/auth/login", new LoginRequest(EMAIL, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).as("login for %s", EMAIL).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookie).isNotNull();
    int semi = setCookie.indexOf(';');
    return semi > 0 ? setCookie.substring(0, semi) : setCookie;
  }

  private ResponseEntity<String> exchange(
      String path, HttpMethod method, String cookie, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
  }

  private static CaseTypeConfig caseTypeWithForm() {
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition(
                "applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null));
    FormDefinition intakeForm =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields);
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Require Access Fixture",
        1,
        null,
        null,
        fields,
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("applicant"),
        List.of(
            new RoleDefinition(
                "admin", List.of(Permission.CREATE, Permission.EDIT, Permission.VIEW))),
        List.of(),
        List.of(intakeForm));
  }
}
