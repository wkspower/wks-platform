package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
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
 * Story 5.2 P7 — integration tests for {@code POST /api/cases/{caseId}/forms/{formId}/submit}.
 *
 * <p>Covers the four key scenarios:
 *
 * <ul>
 *   <li>Happy path → 200 with updated case data.
 *   <li>Unknown form id → 404 (WKS-API-404).
 *   <li>Required field missing → 422 (WKS-FORM-002).
 *   <li>Empty body → 400 (WKS-FORM-003).
 * </ul>
 *
 * <p>Uses an H2 in-memory DB and no BPMN engine (no workflow declared on the case type) — the form
 * submit path does not require an active process instance.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:formsubmitit;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false"
    })
class FormSubmitIT {

  private static final String EMAIL = "form-submit-it@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "form-submit-fixture";
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
  void happyPathReturns200WithUpdatedCaseData() throws Exception {
    String cookie = login();
    String caseId = createCase(cookie);

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\",\"amount\":5000}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = json.readTree(resp.getBody());
    assertThat(body.path("data").path("data").path("applicant").asText()).isEqualTo("Alice");
  }

  @Test
  void unknownFormIdReturns404() throws Exception {
    String cookie = login();
    String caseId = createCase(cookie);

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/nonexistent-form/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\"}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(resp.getBody()).contains("WKS-API-404");
  }

  @Test
  void missingRequiredFieldReturns422WithWksForm002() throws Exception {
    String cookie = login();
    String caseId = createCase(cookie);

    // Omit the required "applicant" field — only supply the optional "amount"
    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"amount\":100}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(resp.getBody()).contains("WKS-FORM-002");
  }

  @Test
  void emptyBodyReturns400WithWksForm003() throws Exception {
    String cookie = login();
    String caseId = createCase(cookie);

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(resp.getBody()).contains("WKS-FORM-003");
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

  private String createCase(String cookie) throws Exception {
    ResponseEntity<String> resp =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + CASE_TYPE_ID + "\",\"data\":{\"applicant\":\"Initial\"}}");
    assertThat(resp.getStatusCode()).as("create case").isEqualTo(HttpStatus.CREATED);
    return json.readTree(resp.getBody()).path("data").path("id").asText();
  }

  private ResponseEntity<String> exchange(
      String path, HttpMethod method, String cookie, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
  }

  private static CaseTypeConfig caseTypeWithForm() {
    // Two fields on the case type: "applicant" (required text) and "amount" (optional number).
    // The intake-form references both so form validation covers both required and optional fields.
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null));

    FormDefinition intakeForm =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields);

    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Form Submit Fixture",
        1,
        null,
        null, // no BPMN — form submit does not require an active process instance
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
