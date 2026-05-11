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
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import java.util.List;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Story 5.5 AC-3 — Postgres-IT parity for {@link FormSubmitIT}.
 *
 * <p>CF#2 from Sprint 9 retro: the H2 FormSubmitIT siblings must be re-verified on a real Postgres
 * at PR-open to guard against schema drift (H2 and Postgres differ on JSONB semantics for the
 * {@code cases.data} column). This IT mirrors the four key scenarios from {@link FormSubmitIT}
 * against a real Postgres instance provided by Testcontainers.
 *
 * <p>Skipped automatically when Docker is unavailable.
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md}: production-validation disabled — only
 * exercises the form-submit surface, not the boot-invariant.
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class FormSubmitPostgresIT {

  private static final String EMAIL = "form-submit-pg-it@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "form-submit-pg-fixture";
  private static final String FORM_ID = "intake-form";

  @Container
  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("wks")
          .withUsername("wks")
          .withPassword("wks");

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry reg) {
    reg.add("WKS_DB_URL", POSTGRES::getJdbcUrl);
    reg.add("WKS_DB_USER", POSTGRES::getUsername);
    reg.add("WKS_DB_PASSWORD", POSTGRES::getPassword);
    reg.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    reg.add("WKS_ADMIN_EMAIL", () -> EMAIL);
    reg.add("WKS_ADMIN_PASSWORD", () -> PASSWORD);
    reg.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
    reg.add("WKS_CORS_ORIGINS", () -> "http://localhost:5173");
    reg.add(
        "camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
    reg.add("wks.case-types.dir", () -> "");
  }

  @Autowired private TestRestTemplate rest;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private ObjectMapper json;

  @BeforeEach
  void setup() {
    registry.register(caseTypeWithForm());
  }

  @Test
  void happyPathReturns200WithUpdatedCaseDataOnPostgres() throws Exception {
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
  void unknownFormIdReturns404OnPostgres() throws Exception {
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
  void missingRequiredFieldReturns422OnPostgres() throws Exception {
    String cookie = login();
    String caseId = createCase(cookie);

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
  void emptyBodyReturns400OnPostgres() throws Exception {
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
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null));

    FormDefinition intakeForm =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields, List.of(), null);

    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Form Submit PG Fixture",
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
