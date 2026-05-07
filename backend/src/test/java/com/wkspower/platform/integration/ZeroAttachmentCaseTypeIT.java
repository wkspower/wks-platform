package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.event.BackendSignalRouted;
import com.wkspower.platform.domain.event.CaseStatusChanged;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.BackendAdapterBinder;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.domain.service.NullAdapter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * Story 4.7 — full-stack integration test exercising the zero-attachment CaseType path end-to-end.
 *
 * <p>Verifies four properties that unit-level tests cannot pin:
 *
 * <ol>
 *   <li>({@code processInstanceId} is {@code null} on a created Case.
 *   <li>Status transition succeeds on the zero-process path; no {@link BackendSignalRouted} event
 *       is published (confirming the Mapping Layer router was bypassed entirely).
 *   <li>{@code bpmn_content_hash} and {@code mapping_hash} are {@code null} in the {@code
 *       case_type_versions} row (fingerprint null-safety per Story 4.5).
 *   <li>{@link BackendAdapterBinder#resolve(CaseTypeRef)} returns the Spring-wired {@link
 *       NullAdapter} singleton for a zero-attachment CaseType.
 * </ol>
 *
 * <p>Pattern: mirrors {@link CaseFlowIT} — {@code @SpringBootTest(webEnvironment = RANDOM_PORT)},
 * {@code @ActiveProfiles("dev")}, {@link TestRestTemplate}, login via {@code POST /api/auth/login}
 * cookie session. No BPMN engine involvement; in-memory H2 is sufficient.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.ActiveProfiles("dev")
@Import(ZeroAttachmentCaseTypeIT.RecorderConfig.class)
// In-memory H2 is safe here: the zero-attachment path never deploys BPMN, so the engine
// BLOB columns that require file-mode H2 (as in CaseFlowIT) are never exercised.
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:zero-attach-it;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
class ZeroAttachmentCaseTypeIT {

  private static final String EMAIL = "zero-attach-admin@wkspower.local";
  private static final String PASSWORD = "admin";
  private static final String CASE_TYPE_ID = "zero-attach-ct";

  /**
   * Zero-attachment YAML fixture — no {@code workflow:} key, no {@code attachments:} key. Uses two
   * statuses so the transition test has a reachable terminal status ({@code closed}) to target.
   *
   * <p>The {@code terminal: true} flag on {@code closed} is intentional — it mirrors the
   * zero-process type contract (Decision 22: a zero-attachment CaseType can still declare a
   * terminal status).
   */
  private static final String ZERO_ATTACH_YAML =
      "id: zero-attach-ct\n"
          + "displayName: \"Zero Attachment\"\n"
          + "version: 1\n"
          + "statuses:\n"
          + "  - id: open\n"
          + "    displayName: Open\n"
          + "    color: blue\n"
          + "  - id: closed\n"
          + "    displayName: Closed\n"
          + "    color: zinc\n"
          + "    terminal: true\n"
          + "roles:\n"
          + "  - name: admin\n"
          + "    permissions: [view, create, edit, transition]\n";

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private ConfigService configService;
  @Autowired private CaseTypeVersionRegistry versionRegistry;
  @Autowired private BackendAdapterBinder binder;
  @Autowired private ObjectMapper json;
  @Autowired private RecorderConfig recorder;

  @BeforeEach
  void setup() {
    // Seed the admin user once (idempotent).
    if (users.findByEmail(EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL, Set.of("admin"), true), encoder.encode(PASSWORD));
    }
    // Register the zero-attachment YAML. ConfigService.validateAndRegister writes the
    // case_type_versions row (with null fingerprints) and populates CaseTypeRegistry +
    // MappingRegistry with an empty MappingDefinition. No BPMN bytes needed.
    configService.validateAndRegister(
        "zero-attach-it.yaml", ZERO_ATTACH_YAML.getBytes(StandardCharsets.UTF_8), "system:test");
    // Clear recorded events so each test starts with a clean slate.
    recorder.statusChanges.clear();
    recorder.routedSignals.clear();
  }

  // ---- AC1 — processInstanceId is null on a zero-attachment Case ----

  /**
   * AC1 — POST /api/cases with a zero-attachment CaseType must return HTTP 201 (or 200 on some
   * controller versions) with {@code data.processInstanceId} absent or null. The BPMN engine's
   * {@code startProcessInstance} must never be called.
   */
  @Test
  void createCase_onZeroAttachmentType_processInstanceIdIsNull() throws Exception {
    String cookie = login();

    ResponseEntity<String> created =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + CASE_TYPE_ID + "\",\"data\":{}}");

    assertThat(created.getStatusCode().is2xxSuccessful())
        .as("POST /api/cases must succeed for zero-attachment CaseType")
        .isTrue();
    JsonNode body = json.readTree(created.getBody());
    JsonNode pidNode = body.path("data").path("processInstanceId");
    // processInstanceId must be absent (MissingNode) or explicitly null.
    assertThat(pidNode.isNull() || pidNode.isMissingNode())
        .as("processInstanceId must be null/absent for zero-attachment CaseType")
        .isTrue();
    // The case must still get a real id.
    assertThat(body.path("data").path("id").asText()).isNotBlank();
  }

  // ---- AC2 — transition succeeds; no BackendSignalRouted event (router bypassed) ----

  /**
   * AC2 — POST /api/cases/{id}/transition on a zero-attachment Case must:
   *
   * <ol>
   *   <li>Return HTTP 200 with {@code data.status == "closed"}.
   *   <li>NOT publish any {@link BackendSignalRouted} event — the Mapping Layer router is bypassed
   *       entirely on the zero-process path ({@code CaseService.transition} calls {@code
   *       CaseStatusUpdater} directly).
   * </ol>
   *
   * <p>The absence of any {@link BackendSignalRouted} event confirms that the Mapping Layer router
   * was bypassed (those events carry {@code AuditSource.Backend}; no routed event = router not
   * entered). A direct positive assertion that the audit row carries {@code source = "manual"} is
   * deferred to a future test once the audit-row read API is available.
   */
  @Test
  void transitionStatus_onZeroAttachmentCase_succeedsWithManualAuditSource() throws Exception {
    String cookie = login();

    // Create the case first.
    ResponseEntity<String> created =
        exchange(
            "/api/cases",
            HttpMethod.POST,
            cookie,
            "{\"caseTypeId\":\"" + CASE_TYPE_ID + "\",\"data\":{}}");
    assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
    String caseId = json.readTree(created.getBody()).path("data").path("id").asText();
    assertThat(caseId).isNotBlank();

    // Clear recorder state after create (we only want transition events).
    recorder.statusChanges.clear();
    recorder.routedSignals.clear();

    // Transition to "closed".
    ResponseEntity<String> tx =
        exchange(
            "/api/cases/" + caseId + "/transition",
            HttpMethod.POST,
            cookie,
            "{\"action\":\"closed\",\"variables\":{}}");

    assertThat(tx.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode txBody = json.readTree(tx.getBody());
    assertThat(txBody.path("data").path("status").asText())
        .as("status must be updated to 'closed' after zero-process transition")
        .isEqualTo("closed");

    // AC2 audit-source proof: no BackendSignalRouted events were published.
    // BackendSignalRouted carries AuditSource.Backend (wire: "backend(...)").
    // Zero-process path bypasses the router → no such event.
    assertThat(recorder.routedSignals)
        .as(
            "BackendSignalRouted must NOT be published for zero-attachment transition (router bypassed)")
        .isEmpty();
    // CaseStatusChanged is only published by the BPMN engine listener (CaseStatusListener).
    // Zero-process transitions call CaseStatusAdapter directly — no CaseStatusChanged published.
    assertThat(recorder.statusChanges)
        .as(
            "CaseStatusChanged must NOT be published on zero-process path (BPMN listener not invoked)")
        .isEmpty();
  }

  // ---- AC3 — zero-attachment deploy stores null fingerprints ----

  /**
   * AC3 — After {@code ConfigService.validateAndRegister} for a zero-attachment YAML, the {@code
   * case_type_versions} row must have {@code bpmn_content_hash IS NULL} and {@code mapping_hash IS
   * NULL}. This confirms the null-safety path introduced in Story 4.5 holds end-to-end when no BPMN
   * bytes are present.
   */
  @Test
  void zeroAttachmentDeploy_storesNullFingerprints() {
    // ConfigService.validateAndRegister was called in @BeforeEach.
    // Look up the current version for the zero-attachment CaseType.
    int version =
        versionRegistry
            .currentVersion(CASE_TYPE_ID)
            .orElseThrow(
                () ->
                    new AssertionError(
                        "CaseType " + CASE_TYPE_ID + " has no registered version — setup failed"));

    var versionRecord =
        versionRegistry
            .findVersion(CASE_TYPE_ID, version)
            .orElseThrow(
                () ->
                    new AssertionError(
                        "case_type_versions row for "
                            + CASE_TYPE_ID
                            + " v"
                            + version
                            + " not found"));

    assertThat(versionRecord.bpmnContentHash())
        .as("bpmn_content_hash must be null for a zero-attachment CaseType")
        .isNull();
    assertThat(versionRecord.mappingHash())
        .as("mapping_hash must be null for a zero-attachment CaseType")
        .isNull();
  }

  // ---- AC4 — BackendAdapterBinder.resolve() returns NullAdapter ----

  /**
   * AC4 — {@link BackendAdapterBinder#resolve(CaseTypeRef)} for a zero-attachment CaseType must
   * return the Spring-context-wired {@link NullAdapter} singleton. No BPMN adapter attachment
   * occurs for zero-attachment types.
   */
  @Test
  void binder_resolve_returnsNullAdapter_forZeroAttachmentCaseType() {
    int version =
        versionRegistry
            .currentVersion(CASE_TYPE_ID)
            .orElseThrow(
                () ->
                    new AssertionError(
                        "CaseType " + CASE_TYPE_ID + " has no registered version — setup failed"));

    CaseTypeRef ref = new CaseTypeRef(CASE_TYPE_ID, String.valueOf(version));
    var resolved = binder.resolve(ref);

    assertThat(resolved)
        .as("BackendAdapterBinder.resolve must return NullAdapter for zero-attachment CaseType")
        .isInstanceOf(NullAdapter.class);
  }

  // ---- helpers ----

  private String login() {
    ResponseEntity<String> resp =
        rest.postForEntity("/api/auth/login", new LoginRequest(EMAIL, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
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

  /**
   * Captures domain events so tests can assert their presence or absence. Records both {@link
   * CaseStatusChanged} (BPMN-engine path) and {@link BackendSignalRouted} (router audit path). For
   * zero-attachment transitions, both lists must remain empty — confirming neither the BPMN
   * listener nor the Mapping Layer router was invoked.
   */
  @TestConfiguration
  static class RecorderConfig {

    final List<CaseStatusChanged> statusChanges = new CopyOnWriteArrayList<>();
    final List<BackendSignalRouted> routedSignals = new CopyOnWriteArrayList<>();

    @EventListener
    void onStatusChanged(CaseStatusChanged e) {
      statusChanges.add(e);
    }

    @EventListener
    void onRouted(BackendSignalRouted e) {
      routedSignals.add(e);
    }
  }
}
