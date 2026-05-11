package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.DefaultFieldEditability;
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
import com.wkspower.platform.infrastructure.persistence.entity.RoleEntity;
import com.wkspower.platform.infrastructure.persistence.repository.RoleEntityRepository;
import java.time.Instant;
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
 * Story 5.6 AC2 / AC4 — controller-level integration tests for per-field edit permissions.
 *
 * <ul>
 *   <li>Submit with required role → 200.
 *   <li>Submit without required role → 422 + WKS-AUTHZ-001.
 *   <li>Draft PUT does NOT enforce field permissions (working state).
 *   <li>locked-by-default with no editableBy + role-less actor → 422 + WKS-AUTHZ-001.
 * </ul>
 *
 * <p>Postgres-IT parity status: H2-only in this story (matches the pre-existing {@code
 * FormSubmitIT} pattern). Full Postgres-IT migration is carried forward.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:perfieldperm;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false"
    })
class PerFieldEditPermissionIT {

  private static final String UNDERWRITER_EMAIL = "underwriter-it@wkspower.local";
  private static final String OFFICER_EMAIL = "officer-it@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "per-field-perm-fixture";
  private static final String LOCKED_CASE_TYPE_ID = "per-field-perm-locked-fixture";
  private static final String FORM_ID = "intake-form";

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private ObjectMapper json;
  @Autowired private RoleEntityRepository roleRepo;

  @BeforeEach
  void setup() {
    // Seed the 'underwriter' and 'officer' roles. The 'admin' role is seeded by Flyway.
    Instant now = Instant.now();
    if (roleRepo.findByName("underwriter").isEmpty()) {
      roleRepo.save(new RoleEntity(UUID.randomUUID(), "underwriter", now, now));
    }
    if (roleRepo.findByName("officer").isEmpty()) {
      roleRepo.save(new RoleEntity(UUID.randomUUID(), "officer", now, now));
    }
    if (users.findByEmail(UNDERWRITER_EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), UNDERWRITER_EMAIL, Set.of("admin", "underwriter"), true),
          encoder.encode(PASSWORD));
    }
    if (users.findByEmail(OFFICER_EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), OFFICER_EMAIL, Set.of("admin", "officer"), true),
          encoder.encode(PASSWORD));
    }
    registry.register(editableByCaseType());
    registry.register(lockedByDefaultCaseType());
  }

  @Test
  void submitWithRequiredRoleReturns200() throws Exception {
    String cookie = login(UNDERWRITER_EMAIL);
    String caseId = createCase(cookie, CASE_TYPE_ID);

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\",\"amount\":5000}");

    assertThat(resp.getStatusCode())
        .as("underwriter has the required role; got body=%s", resp.getBody())
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void submitWithoutRequiredRoleReturns422AndWksAuthzField() throws Exception {
    String cookie = login(OFFICER_EMAIL);
    String caseId = createCase(cookie, CASE_TYPE_ID);

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\",\"amount\":5000}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(resp.getBody()).contains("WKS-AUTHZ-001");
    JsonNode body = json.readTree(resp.getBody());
    JsonNode errors = body.path("error").path("errors");
    assertThat(errors.isArray()).isTrue();
    assertThat(errors.size()).isGreaterThanOrEqualTo(1);
    boolean amountFieldRejected = false;
    for (JsonNode e : errors) {
      if ("WKS-AUTHZ-001".equals(e.path("code").asText())
          && "fields.amount".equals(e.path("field").asText())) {
        amountFieldRejected = true;
        break;
      }
    }
    assertThat(amountFieldRejected).as("expected amount field rejection in: %s", errors).isTrue();
  }

  @Test
  void draftPutDoesNotEnforceFieldPermissions() throws Exception {
    // Officer (lacks 'underwriter' role) saves a draft of the protected 'amount' field.
    String cookie = login(OFFICER_EMAIL);
    String caseId = createCase(cookie, CASE_TYPE_ID);

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft",
            HttpMethod.PUT,
            cookie,
            "{\"payload\":{\"amount\":12345},\"scrollY\":0,\"sectionExpanded\":{},"
                + "\"caseTypeVersionAtSave\":1}");

    assertThat(resp.getStatusCode())
        .as("draft path must not enforce per-field permission; got: %s", resp.getBody())
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void lockedByDefaultRejectsEditOfFieldWithoutEditableBy() throws Exception {
    String cookie = login(OFFICER_EMAIL);
    String caseId = createCase(cookie, LOCKED_CASE_TYPE_ID);

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Bob\",\"amount\":99}");

    // applicant changes from "Initial" to "Bob" — locked-by-default rejects the change.
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    assertThat(resp.getBody()).contains("WKS-AUTHZ-001");
  }

  // ---- helpers ---------------------------------------------------------------

  private String login(String email) {
    ResponseEntity<String> resp =
        rest.postForEntity("/api/auth/login", new LoginRequest(email, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).as("login for %s", email).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    int semi = setCookie.indexOf(';');
    return semi > 0 ? setCookie.substring(0, semi) : setCookie;
  }

  private String createCase(String cookie, String caseTypeId) throws Exception {
    ResponseEntity<String> resp =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + caseTypeId + "\",\"data\":{\"applicant\":\"Initial\"}}");
    assertThat(resp.getStatusCode())
        .as("create case body=%s", resp.getBody())
        .isEqualTo(HttpStatus.CREATED);
    return json.readTree(resp.getBody()).path("data").path("id").asText();
  }

  private ResponseEntity<String> exchange(
      String path, HttpMethod method, String cookie, String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, cookie);
    return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
  }

  private static CaseTypeConfig editableByCaseType() {
    // 'amount' is restricted to underwriter; 'applicant' is unrestricted.
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition(
                "applicant",
                "Applicant",
                FieldType.TEXT,
                true,
                true,
                0,
                List.of(),
                null,
                List.of()),
            new FieldDefinition(
                "amount",
                "Amount",
                FieldType.NUMBER,
                false,
                false,
                1,
                List.of(),
                null,
                List.of("role:underwriter")));
    FormDefinition intakeForm =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields);
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Per-Field Permission Fixture",
        1,
        null,
        null,
        fields,
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("applicant"),
        List.of(
            new RoleDefinition(
                "admin", List.of(Permission.CREATE, Permission.EDIT, Permission.VIEW)),
            new RoleDefinition("underwriter", List.of(Permission.EDIT, Permission.VIEW)),
            new RoleDefinition("officer", List.of(Permission.VIEW))),
        List.of(),
        List.of(intakeForm),
        DefaultFieldEditability.EDITABLE_BY_DEFAULT);
  }

  private static CaseTypeConfig lockedByDefaultCaseType() {
    // No editableBy declared on either field — locked-by-default rejects any change.
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition(
                "applicant",
                "Applicant",
                FieldType.TEXT,
                true,
                true,
                0,
                List.of(),
                null,
                List.of()),
            new FieldDefinition(
                "amount", "Amount", FieldType.NUMBER, false, false, 1, List.of(), null, List.of()));
    FormDefinition intakeForm =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields);
    return new CaseTypeConfig(
        LOCKED_CASE_TYPE_ID,
        "Locked Default Fixture",
        1,
        null,
        null,
        fields,
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("applicant"),
        List.of(
            new RoleDefinition(
                "admin", List.of(Permission.CREATE, Permission.EDIT, Permission.VIEW)),
            new RoleDefinition("officer", List.of(Permission.VIEW))),
        List.of(),
        List.of(intakeForm),
        DefaultFieldEditability.LOCKED_BY_DEFAULT);
  }
}
