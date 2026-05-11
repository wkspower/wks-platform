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
 * Story 5.5 AC-5 — Decision D20 frozen-on-version: in-flight case isolation.
 *
 * <p>Proves that a case created under CaseType v1 continues to see the v1 form definition after
 * CaseType v2 (with a different form topology) is deployed. The response DTO embeds the
 * <em>pinned</em> CaseTypeConfig, not the latest registered version.
 *
 * <p>AC-5 scenario: create a case (v1 pinned), register v2 with an extra required field, GET the
 * case and submit the form — both must use v1's field set.
 *
 * <p>AC-7 scenario: the submit response DTO carries {@code caseTypeVersion = 1} (pinned), even
 * though v2 is live.
 *
 * <p>Each test uses a unique case-type ID to avoid cross-test registry pollution: the {@link
 * CaseTypeRegistry} is a shared singleton (Spring context reuse) and once v2 is registered for an
 * ID, v1 cannot be re-registered (older-version guard). Per-test IDs isolate the version lifecycle
 * entirely.
 *
 * <p>Uses H2 in-memory DB; companion Postgres-IT is {@code FormBindingVersionPinPostgresIT}.
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md}: production-validation disabled — no
 * production-profile boot here.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:formbindingversionpin;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false"
    })
class FormBindingVersionPinIT {

  private static final String EMAIL = "formbind-pin-it@wkspower.local";
  private static final String PASSWORD = "admin";
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
  }

  /**
   * AC-5 — In-flight case isolation.
   *
   * <p>Uses a unique case-type ID ({@code "fvp-t1-" + suffix}) so this test's v1/v2 lifecycle is
   * independent of other tests sharing the same Spring context.
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Register v1 and create a case → case.caseTypeVersion = 1.
   *   <li>Register v2 (adds a new required field "email").
   *   <li>GET the case — response DTO must embed v1's form (2 fields, no "email").
   *   <li>Submit the v1 form — must succeed (v1's required fields satisfied).
   * </ol>
   */
  @Test
  void inFlightCaseSeesV1FormAfterV2Deploy() throws Exception {
    // Per-test unique ID avoids registry older-version rejection across tests
    String caseTypeId = "fvp-t1-" + UUID.randomUUID().toString().substring(0, 8);
    registry.register(caseTypeV1(caseTypeId));

    String cookie = login();
    // Step 1: create case while v1 is live
    String caseId = createCase(cookie, caseTypeId);

    // Step 2: register v2 with an extra required field "email"
    registry.register(caseTypeV2(caseTypeId));

    // Step 3: GET the case — embedded CaseType must be v1 (no "email" field in forms)
    ResponseEntity<String> getResp = exchange("/api/cases/" + caseId, HttpMethod.GET, cookie, null);
    assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode caseBody = json.readTree(getResp.getBody());
    JsonNode caseTypeNode = caseBody.path("data").path("caseType");
    assertThat(caseTypeNode.path("version").asInt())
        .as("GET response must embed pinned v1")
        .isEqualTo(1);
    // v1 form has exactly 2 fields; v2 form has 3
    JsonNode formFields =
        caseTypeNode
            .path("forms")
            .elements()
            .next() // first (only) form
            .path("fields");
    assertThat(formFields.size()).as("pinned v1 form must have 2 fields, not 3").isEqualTo(2);

    // Step 4: submit the v1 form — required "applicant" satisfied; "email" not required at v1
    ResponseEntity<String> submitResp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\",\"amount\":9000}");
    assertThat(submitResp.getStatusCode())
        .as("submit with v1 payload must succeed even though v2 is live")
        .isEqualTo(HttpStatus.OK);
    JsonNode submitBody = json.readTree(submitResp.getBody());
    // Embedded CaseType in submit response must still be v1
    assertThat(submitBody.path("data").path("caseType").path("version").asInt())
        .as("submit response must embed pinned v1")
        .isEqualTo(1);
  }

  /**
   * AC-7 — submit response caseTypeVersion reflects pinned version.
   *
   * <p>The FormController embeds the pinned CaseType in the response DTO. After submitting,
   * caseDto.caseTypeVersion must equal the pinned version (1), not the latest (2).
   */
  @Test
  void submitResponseEmbedsPinnedCaseTypeVersion() throws Exception {
    String caseTypeId = "fvp-t2-" + UUID.randomUUID().toString().substring(0, 8);
    registry.register(caseTypeV1(caseTypeId));

    String cookie = login();
    String caseId = createCase(cookie, caseTypeId);

    // Deploy v2 before submit — should have no effect on the pinned case
    registry.register(caseTypeV2(caseTypeId));

    ResponseEntity<String> resp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Bob\",\"amount\":42}");

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = json.readTree(resp.getBody());
    // caseDto.caseTypeVersion is the case's pinned version (not caseType.version which is also 1
    // because we embed pinned — both must be 1)
    assertThat(body.path("data").path("caseTypeVersion").asInt())
        .as("caseDto.caseTypeVersion must be pinned v1")
        .isEqualTo(1);
    assertThat(body.path("data").path("caseType").path("version").asInt())
        .as("embedded caseType.version must be pinned v1")
        .isEqualTo(1);
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

  private String createCase(String cookie, String caseTypeId) throws Exception {
    ResponseEntity<String> resp =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + caseTypeId + "\",\"data\":{\"applicant\":\"Initial\"}}");
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

  // ---- CaseType fixtures -----------------------------------------------------

  /**
   * v1: "applicant" (required text) + "amount" (optional number). Form has 2 fields. Version = 1.
   */
  private static CaseTypeConfig caseTypeV1(String caseTypeId) {
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null));

    FormDefinition form =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields, List.of(), null);

    return new CaseTypeConfig(
        caseTypeId,
        "FormBind Pin Fixture",
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
        List.of(form));
  }

  /**
   * v2: adds a third required field "email". A case pinned to v1 must NOT be required to supply
   * "email" when submitting the intake-form.
   */
  private static CaseTypeConfig caseTypeV2(String caseTypeId) {
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null),
            new FieldDefinition("email", "Email", FieldType.TEXT, true, 2, List.of(), null));

    FormDefinition form =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields, List.of(), null);

    return new CaseTypeConfig(
        caseTypeId,
        "FormBind Pin Fixture",
        2,
        null,
        null,
        fields,
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("applicant"),
        List.of(
            new RoleDefinition(
                "admin", List.of(Permission.CREATE, Permission.EDIT, Permission.VIEW))),
        List.of(),
        List.of(form));
  }
}
