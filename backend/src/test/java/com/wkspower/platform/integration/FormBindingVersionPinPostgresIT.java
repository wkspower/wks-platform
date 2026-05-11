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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Story 5.5 AC-3 + AC-5 — Postgres-IT parity for {@link FormBindingVersionPinIT}.
 *
 * <p>Proves the frozen-on-version guarantee on a real Postgres (JSONB semantics differ from H2 for
 * the {@code cases.data} column). The {@code case_type_versions} registry persistence and {@code
 * CaseTypeRegistry.findVersion} cache-miss path are both exercised on real Postgres.
 *
 * <p>Skipped automatically when Docker is unavailable.
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md}: production-validation disabled.
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class FormBindingVersionPinPostgresIT {

  private static final String EMAIL = "formbind-pin-pg-it@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "formbind-pin-pg-fixture";
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
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
    reg.add("wks.case-types.dir", () -> "");
  }

  @Autowired private TestRestTemplate rest;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private ObjectMapper json;

  @BeforeEach
  void setup() {
    registry.register(caseTypeV1());
  }

  /**
   * AC-5 on Postgres — In-flight case isolation with real JSONB column.
   *
   * <p>Verifies that a case created while v1 is current sees v1's form (2 fields) after v2 (3
   * fields, adds required "email") is deployed. The {@code case_type_versions} table on Postgres
   * must persist both versions and {@code CaseTypeRegistry.findVersion(id, 1)} must resolve v1.
   */
  @Test
  void inFlightCaseSeesV1FormAfterV2DeployOnPostgres() throws Exception {
    String cookie = login();
    String caseId = createCase(cookie);

    // Deploy v2 with extra required field
    registry.register(caseTypeV2());

    // GET the case — embedded CaseType must be v1
    ResponseEntity<String> getResp = exchange("/api/cases/" + caseId, HttpMethod.GET, cookie, null);
    assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode caseBody = json.readTree(getResp.getBody());
    JsonNode caseTypeNode = caseBody.path("data").path("caseType");
    assertThat(caseTypeNode.path("version").asInt())
        .as("GET response must embed pinned v1 on Postgres")
        .isEqualTo(1);
    JsonNode formFields = caseTypeNode.path("forms").elements().next().path("fields");
    assertThat(formFields.size()).as("pinned v1 form must have 2 fields on Postgres").isEqualTo(2);

    // Submit with v1 payload — must succeed (no "email" required at v1)
    ResponseEntity<String> submitResp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\",\"amount\":9000}");
    assertThat(submitResp.getStatusCode())
        .as("v1 submit must succeed on Postgres even though v2 is live")
        .isEqualTo(HttpStatus.OK);
    assertThat(
            json.readTree(submitResp.getBody())
                .path("data")
                .path("caseType")
                .path("version")
                .asInt())
        .as("submit response embeds pinned v1 on Postgres")
        .isEqualTo(1);
  }

  /**
   * AC-6 (a) Postgres parity — v1-pinned validates against v1 when a field was REMOVED in v2.
   *
   * <p>Uses a per-test unique id to avoid registry older-version rejection against the {@link
   * #CASE_TYPE_ID} v1 written by {@link #setup}. JSONB persistence path verified.
   */
  @Test
  void v1PinnedValidatesAgainstV1AfterFieldRemovedInV2OnPostgres() throws Exception {
    String caseTypeId = "fvp-pg-t3-" + UUID.randomUUID().toString().substring(0, 8);
    registry.register(caseTypeV1WithNote(caseTypeId));

    String cookie = login();
    String caseId = createCaseWithId(cookie, caseTypeId);

    registry.register(caseTypeV2NoteRemoved(caseTypeId));

    ResponseEntity<String> submitResp =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\",\"amount\":42,\"note\":\"v1-only field\"}");
    assertThat(submitResp.getStatusCode())
        .as("Postgres: v1-pinned submit with v1-only field must succeed")
        .isEqualTo(HttpStatus.OK);
  }

  /**
   * AC-6 (b) Postgres parity — v1-pinned PUT not subject to v2's new required field.
   *
   * <p>Reads optimistic-lock version in a fresh exchange (Postgres reads must see the committed
   * INSERT from createCase; memory {@code feedback_postgres_it_committed_read.md}). The PUT body
   * carries {@code version} per {@code UpdateCaseRequest}.
   */
  @Test
  void v1PinnedUpdateNotSubjectToV2NewRequiredFieldOnPostgres() throws Exception {
    String caseTypeId = "fvp-pg-t4-" + UUID.randomUUID().toString().substring(0, 8);
    registry.register(caseTypeV1WithId(caseTypeId));

    String cookie = login();
    String caseId = createCaseWithId(cookie, caseTypeId);

    ResponseEntity<String> getResp = exchange("/api/cases/" + caseId, HttpMethod.GET, cookie, null);
    long caseVersion = json.readTree(getResp.getBody()).path("data").path("version").asLong();

    registry.register(caseTypeV2WithId(caseTypeId));

    ResponseEntity<String> putResp =
        exchange(
            "/api/cases/" + caseId,
            HttpMethod.PUT,
            cookie,
            "{\"data\":{\"applicant\":\"Bob\",\"amount\":1},\"version\":" + caseVersion + "}");
    assertThat(putResp.getStatusCode())
        .as("Postgres: v1-pinned PUT with v1 body must succeed even though v2 demands email")
        .isEqualTo(HttpStatus.OK);
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
    return createCaseWithId(cookie, CASE_TYPE_ID);
  }

  private String createCaseWithId(String cookie, String caseTypeId) throws Exception {
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

  private static CaseTypeConfig caseTypeV1() {
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null));
    FormDefinition form =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields, List.of(), null);
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "FormBind Pin PG Fixture",
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

  // ---- AC-6 (a)+(b) per-test-id variants ------------------------------------

  /** v1 with explicit id, for AC-6 (b) test (per-test fresh id to dodge older-version guard). */
  private static CaseTypeConfig caseTypeV1WithId(String caseTypeId) {
    return rebuildAt(caseTypeV1(), caseTypeId);
  }

  /** v2 with explicit id, for AC-6 (b) test. */
  private static CaseTypeConfig caseTypeV2WithId(String caseTypeId) {
    return rebuildAt(caseTypeV2(), caseTypeId);
  }

  /** v1 with explicit id + optional "note" field, for AC-6 (a). */
  private static CaseTypeConfig caseTypeV1WithNote(String caseTypeId) {
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null),
            new FieldDefinition("note", "Note", FieldType.TEXT, false, 2, List.of(), null));
    FormDefinition form =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields, List.of(), null);
    return new CaseTypeConfig(
        caseTypeId,
        "FormBind Pin PG Fixture",
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

  /** v2 with explicit id, "note" REMOVED — for AC-6 (a). */
  private static CaseTypeConfig caseTypeV2NoteRemoved(String caseTypeId) {
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null));
    FormDefinition form =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields, List.of(), null);
    return new CaseTypeConfig(
        caseTypeId,
        "FormBind Pin PG Fixture",
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

  /** Rebuild a fixture with a different id (uses the 11-arg compat constructor). */
  private static CaseTypeConfig rebuildAt(CaseTypeConfig src, String caseTypeId) {
    return new CaseTypeConfig(
        caseTypeId,
        src.displayName(),
        src.version(),
        src.description(),
        src.workflow(),
        src.fields(),
        src.statuses(),
        src.listColumns(),
        src.roles(),
        src.stages(),
        src.forms());
  }

  private static CaseTypeConfig caseTypeV2() {
    List<FieldDefinition> fields =
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition("amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null),
            new FieldDefinition("email", "Email", FieldType.TEXT, true, 2, List.of(), null));
    FormDefinition form =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields, List.of(), null);
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "FormBind Pin PG Fixture",
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
