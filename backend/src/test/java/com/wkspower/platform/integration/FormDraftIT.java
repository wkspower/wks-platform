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
import com.wkspower.platform.domain.event.FormDraftExpired;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.FormDraftService;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import com.wkspower.platform.infrastructure.persistence.entity.FormDraftEntity;
import com.wkspower.platform.infrastructure.persistence.repository.FormDraftJpaRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * Story 5.4 — end-to-end integration coverage for the form draft surface (AC1, AC4, AC5, AC6). Uses
 * an H2 in-memory DB; the stage primitives that need Postgres parity (memory {@code
 * project_postgres_it_parity_gap.md}) are unaffected here because the draft expiration query is a
 * simple {@code updated_at < ?} predicate that behaves the same on both databases (no timezone
 * arithmetic, no SSI gotchas).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(FormDraftIT.TestBeans.class)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:formdraftit;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false"
    })
class FormDraftIT {

  private static final String EMAIL_A = "draft-it-a@wkspower.local";
  private static final String EMAIL_B = "draft-it-b@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "form-draft-fixture";
  private static final String FORM_ID = "intake-form";

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private ObjectMapper json;
  @Autowired private FormDraftService draftService;
  @Autowired private FormDraftJpaRepository draftRepo;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private ExpirationListener listener;

  @BeforeEach
  void setup() {
    if (users.findByEmail(EMAIL_A).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL_A, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
    if (users.findByEmail(EMAIL_B).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL_B, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
    registry.register(caseTypeWithForm());
    listener.last.set(null);
  }

  // ---- AC1 — CRUD via REST -----------------------------------------------------

  @Test
  void putGetDeleteRoundtripWorks() throws Exception {
    String cookie = login(EMAIL_A);
    String caseId = createCase(cookie);

    // GET 404 when no draft exists yet
    ResponseEntity<String> empty =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft", HttpMethod.GET, cookie, null);
    assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    // PUT — save draft
    String payload =
        "{\"payload\":{\"applicant\":\"Alice\"},\"scrollY\":42,\"sectionExpanded\":null,\"caseTypeVersionAtSave\":1}";
    ResponseEntity<String> put =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft",
            HttpMethod.PUT,
            cookie,
            payload);
    assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(json.readTree(put.getBody()).path("data").path("scrollY").asInt()).isEqualTo(42);

    // GET 200 with the saved draft
    ResponseEntity<String> got =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft", HttpMethod.GET, cookie, null);
    assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = json.readTree(got.getBody()).path("data");
    assertThat(body.path("payload").path("applicant").asText()).isEqualTo("Alice");

    // PUT again — upsert in place
    String payload2 =
        "{\"payload\":{\"applicant\":\"Bob\"},\"scrollY\":99,\"sectionExpanded\":null,\"caseTypeVersionAtSave\":1}";
    exchange(
        "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft", HttpMethod.PUT, cookie, payload2);
    ResponseEntity<String> got2 =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft", HttpMethod.GET, cookie, null);
    JsonNode body2 = json.readTree(got2.getBody()).path("data");
    assertThat(body2.path("payload").path("applicant").asText()).isEqualTo("Bob");
    assertThat(body2.path("scrollY").asInt()).isEqualTo(99);

    // DELETE — 204 + GET returns 404
    ResponseEntity<String> del =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft",
            HttpMethod.DELETE,
            cookie,
            null);
    assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    ResponseEntity<String> after =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft", HttpMethod.GET, cookie, null);
    assertThat(after.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  // ---- AC5 — cross-user isolation ----------------------------------------------

  @Test
  void userBCannotReadUserADraft() throws Exception {
    String cookieA = login(EMAIL_A);
    String caseId = createCase(cookieA);
    exchange(
        "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft",
        HttpMethod.PUT,
        cookieA,
        "{\"payload\":{\"applicant\":\"Alice\"},\"scrollY\":0,\"sectionExpanded\":null,\"caseTypeVersionAtSave\":1}");

    String cookieB = login(EMAIL_B);
    ResponseEntity<String> b =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft", HttpMethod.GET, cookieB, null);
    assertThat(b.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  // ---- AC6 — submit deletes draft, validation failure preserves it -------------

  @Test
  void successfulSubmitDeletesDraft() throws Exception {
    String cookie = login(EMAIL_A);
    String caseId = createCase(cookie);
    exchange(
        "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft",
        HttpMethod.PUT,
        cookie,
        "{\"payload\":{\"applicant\":\"Alice\"},\"scrollY\":0,\"sectionExpanded\":null,\"caseTypeVersionAtSave\":1}");

    ResponseEntity<String> submit =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"applicant\":\"Alice\",\"amount\":5000}");
    assertThat(submit.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<String> after =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft", HttpMethod.GET, cookie, null);
    assertThat(after.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void validationFailureOnSubmitPreservesDraft() throws Exception {
    String cookie = login(EMAIL_A);
    String caseId = createCase(cookie);
    exchange(
        "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft",
        HttpMethod.PUT,
        cookie,
        "{\"payload\":{\"applicant\":\"Alice\"},\"scrollY\":0,\"sectionExpanded\":null,\"caseTypeVersionAtSave\":1}");

    // Submit missing the required "applicant" field — 422
    ResponseEntity<String> submit =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/submit",
            HttpMethod.POST,
            cookie,
            "{\"amount\":5000}");
    assertThat(submit.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

    // Draft survives the rolled-back transaction.
    ResponseEntity<String> after =
        exchange(
            "/api/cases/" + caseId + "/forms/" + FORM_ID + "/draft", HttpMethod.GET, cookie, null);
    assertThat(after.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  // ---- AC4 — expiration --------------------------------------------------------

  @Test
  void expireOlderThanDeletesAndEmitsEvent() throws Exception {
    String cookie = login(EMAIL_A);
    String caseId = createCase(cookie);
    UUID caseUuid = UUID.fromString(caseId);
    UUID userUuid = users.findByEmail(EMAIL_A).orElseThrow().id();

    // Save a draft via the service, then back-date its updated_at via the repo.
    draftService.saveDraft(caseUuid, "f1", userUuid, Map.of("k", "v"), 0, null, 1);
    FormDraftEntity stored =
        draftRepo.findByCaseIdAndFormIdAndUserId(caseUuid, "f1", userUuid).orElseThrow();
    Instant longAgo = Instant.now().minus(45, ChronoUnit.DAYS);
    // Bypass @PreUpdate by going direct via JdbcTemplate.
    jdbc.update(
        "UPDATE form_drafts SET updated_at = ? WHERE id = ?",
        java.sql.Timestamp.from(longAgo),
        stored.getId());

    int n = draftService.expireOlderThan(Instant.now().minus(30, ChronoUnit.DAYS));

    assertThat(n).isGreaterThanOrEqualTo(1);
    assertThat(draftRepo.findById(stored.getId())).isEmpty();
    assertThat(listener.last.get()).isNotNull();
    assertThat(listener.last.get().formId()).isEqualTo("f1");
  }

  // ---- helpers -----------------------------------------------------------------

  static class ExpirationListener {
    final AtomicReference<FormDraftExpired> last = new AtomicReference<>();

    @EventListener
    public void on(FormDraftExpired e) {
      last.set(e);
    }
  }

  @TestConfiguration
  static class TestBeans {
    @Bean
    ExpirationListener expirationListener() {
      return new ExpirationListener();
    }
  }

  private String login(String email) {
    ResponseEntity<String> resp =
        rest.postForEntity("/api/auth/login", new LoginRequest(email, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).as("login for %s", email).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
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
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields);
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Form Draft Fixture",
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
