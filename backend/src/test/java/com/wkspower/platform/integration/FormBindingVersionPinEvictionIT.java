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
 * Story 5.5 AC-6 (c) — Review remediation: when the pinned CaseTypeVersion has been evicted from
 * the by-version cache AND the underlying YAML stub fails re-hydration validation, the strict
 * findVersion path in {@code CaseService.update}/{@code submitForm} must surface {@code
 * WKS-VER-002} as HTTP 422 — not silently fall back to the latest version's schema.
 *
 * <p>Forces eviction by setting {@code wks.registry.cache.max-size=1} and registering enough
 * versions to push v1 out of the access-ordered LRU. The test-bypass register() path writes a
 * minimal stub YAML to the version registry, which the validator rejects on re-hydration — so
 * eviction yields {@code Optional.empty()} on findVersion(id, 1), and the service must throw.
 *
 * <p>Lives in its own class to scope the cache-size property to this Spring context only — the
 * sibling {@link FormBindingVersionPinIT} relies on the default cache size for AC-5/AC-7.
 *
 * <p>Memory {@code feedback_production_validator_opt_out.md}: production-validation disabled.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:formbindingversionpin-eviction;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false",
      "wks.registry.cache.max-size=1"
    })
class FormBindingVersionPinEvictionIT {

  private static final String EMAIL = "formbind-pin-evict-it@wkspower.local";
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
   * AC-6 (c) — v1 evicted: PUT against v1-pinned case → 422 WKS-VER-002.
   *
   * <p>Sequence:
   *
   * <ol>
   *   <li>Register v1, create a case (pinned to v1).
   *   <li>Register v2 and v3 with cache-size=1 → v1 evicted from the by-version LRU.
   *   <li>PUT /api/cases/{id} with v1 body — service-side findVersion(id, 1) returns empty after
   *       failed re-hydration of stub YAML, orElseThrow fires WKS-VER-002 → HTTP 422.
   * </ol>
   */
  @Test
  void putAgainstEvictedPinnedVersionReturns422WksVer002() throws Exception {
    String caseTypeId = "fvp-evict-" + UUID.randomUUID().toString().substring(0, 8);
    registry.register(caseTypeAt(caseTypeId, 1, false));

    String cookie = login();
    String caseId = createCase(cookie, caseTypeId);

    // Capture optimistic-lock version BEFORE eviction (case still readable via byId path).
    ResponseEntity<String> getResp = exchange("/api/cases/" + caseId, HttpMethod.GET, cookie, null);
    long caseVersion = json.readTree(getResp.getBody()).path("data").path("version").asLong();

    // Force eviction: register v2 and v3 with cache-size=1, pushing v1 out of byVersion.
    registry.register(caseTypeAt(caseTypeId, 2, false));
    registry.register(caseTypeAt(caseTypeId, 3, false));

    // PUT against v1-pinned case — pinned version no longer in cache; stub YAML re-hydration
    // fails validation → findVersion returns empty → service throws WKS-VER-002.
    ResponseEntity<String> putResp =
        exchange(
            "/api/cases/" + caseId,
            HttpMethod.PUT,
            cookie,
            "{\"data\":{\"applicant\":\"Bob\"},\"version\":" + caseVersion + "}");

    assertThat(putResp.getStatusCode())
        .as("v1 evicted → PUT must return 422 WKS-VER-002, not silent fallback to latest")
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    JsonNode body = json.readTree(putResp.getBody());
    assertThat(body.path("error").path("code").asText())
        .as("error code on PUT against evicted pinned version")
        .isEqualTo("WKS-VER-002");
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

  // ---- fixtures --------------------------------------------------------------

  private static CaseTypeConfig caseTypeAt(String caseTypeId, int version, boolean extraRequired) {
    List<FieldDefinition> fields =
        extraRequired
            ? List.of(
                new FieldDefinition(
                    "applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
                new FieldDefinition(
                    "email", "Email", FieldType.TEXT, true, 1, List.of(), null))
            : List.of(
                new FieldDefinition(
                    "applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
                new FieldDefinition(
                    "amount", "Amount", FieldType.NUMBER, false, 1, List.of(), null));
    FormDefinition form =
        new FormDefinition(FORM_ID, "single", "monolithic", "single-page", fields, List.of(), null);
    return new CaseTypeConfig(
        caseTypeId,
        "FormBind Pin Eviction Fixture",
        version,
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
