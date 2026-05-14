package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.audit.AuditEvent;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.event.CaseDataEdited;
import com.wkspower.platform.domain.model.AuditSource;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import com.wkspower.platform.infrastructure.persistence.AuditEventRepository;
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
 * Regression guard for the {@code PUT /api/cases/{id}} audit-emission chain. Before the
 * {@code @Transactional} fix, the live HTTP path had no transaction around {@code
 * CaseService.update}; {@code EventPublisher.publishAfterCommit} fell through to a no-TX {@code
 * publishEvent}, and {@code EditAuditEmitter}'s {@code @TransactionalEventListener(AFTER_COMMIT)}
 * silently dropped the event. Result: {@code audit_events} stayed empty even though endpoints,
 * mapper, and frontend tab shipped correctly (Sprint 12 demo blocker, surfaced 2026-05-13).
 *
 * <p>Full HTTP round-trip via {@link TestRestTemplate} — the same shape as the live demo path —
 * because slice tests (@WebMvcTest) and direct-call unit tests both miss the missing-annotation
 * class of bug. Asserts via {@link AuditEventRepository#findByCaseId} that the listener fired.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:caseupdateauditchain;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
class CaseUpdateAuditChainIT {

  private static final String EMAIL = "case-update-audit-it@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "audit-chain-it";

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private AuditEventRepository auditEventRepository;

  private String cookie;
  private UUID actorId;

  @BeforeEach
  void setup() {
    if (users.findByEmail(EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
    actorId = users.findByEmail(EMAIL).orElseThrow().id();
    registry.register(auditChainCaseType());
    cookie = login(EMAIL);
  }

  @Test
  void putUpdate_emitsAuditEvent_endToEnd() {
    UUID caseId = createCase();

    HttpHeaders headers = jsonHeaders(cookie);
    String body = "{\"data\":{\"applicant\":\"edited\"},\"version\":0}";
    ResponseEntity<String> put =
        rest.exchange(
            "/api/cases/" + caseId, HttpMethod.PUT, new HttpEntity<>(body, headers), String.class);
    assertThat(put.getStatusCode()).as("PUT body=%s", put.getBody()).isEqualTo(HttpStatus.OK);

    // d4a5d3574 added a case.created emission alongside case.data.edit, so the chain now carries
    // both. Filter to case.data.edit to keep this test scoped to the EditAuditEmitter behaviour
    // it was written to cover (the create-emission has its own coverage).
    List<AuditEvent> rows =
        auditEventRepository.findByCaseId(caseId, 10).stream()
            .filter(e -> AuditEvent.EVENT_TYPE_CASE_DATA_EDIT.equals(e.eventType()))
            .toList();
    assertThat(rows).as("audit_events populated by EditAuditEmitter").hasSize(1);
    AuditEvent row = rows.get(0);
    assertThat(row.eventType()).isEqualTo(AuditEvent.EVENT_TYPE_CASE_DATA_EDIT);
    assertThat(row.result()).isEqualTo(CaseDataEdited.Result.APPLIED.name());
    assertThat(row.fieldId()).isEqualTo("applicant");
    assertThat(row.source()).isInstanceOf(AuditSource.User.class);
    assertThat(((AuditSource.User) row.source()).actorId()).isEqualTo(actorId);
  }

  private UUID createCase() {
    HttpHeaders headers = jsonHeaders(cookie);
    String body = "{\"caseTypeId\":\"" + CASE_TYPE_ID + "\",\"data\":{\"applicant\":\"initial\"}}";
    ResponseEntity<String> resp =
        rest.exchange("/api/cases", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    assertThat(resp.getStatusCode())
        .as("POST /api/cases body=%s", resp.getBody())
        .isEqualTo(HttpStatus.CREATED);
    // Parse the id from `{"data":{"id":"<uuid>",...}}` — minimal extraction to avoid pulling in a
    // JSON dep just for one field.
    String b = resp.getBody();
    int idx = b.indexOf("\"id\":\"");
    String id = b.substring(idx + 6, idx + 6 + 36);
    return UUID.fromString(id);
  }

  private String login(String email) {
    ResponseEntity<String> resp =
        rest.postForEntity("/api/auth/login", new LoginRequest(email, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).as("login for %s", email).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    int semi = setCookie.indexOf(';');
    return semi > 0 ? setCookie.substring(0, semi) : setCookie;
  }

  private static HttpHeaders jsonHeaders(String cookie) {
    HttpHeaders h = new HttpHeaders();
    h.add(HttpHeaders.COOKIE, cookie);
    h.add(HttpHeaders.CONTENT_TYPE, "application/json");
    return h;
  }

  private static CaseTypeConfig auditChainCaseType() {
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Audit Chain IT",
        1,
        null,
        null, // zero-process — no BPMN attachment required
        List.of(
            new FieldDefinition(
                "applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("applicant"),
        List.of(
            new RoleDefinition(
                "admin", List.of(Permission.VIEW, Permission.CREATE, Permission.EDIT))),
        List.of(),
        List.of());
  }
}
