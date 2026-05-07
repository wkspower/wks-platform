package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.infrastructure.config.CaseTypeRegistry;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Story 14.2 AC1/AC2/AC3 — Document upload, list, download, and preview endpoints over HTTP. Boots
 * the full Spring context with an H2 in-memory datasource and a TempDir-backed LocalDocumentStore.
 * Validates the happy path (upload → list → download → preview) and the main error paths (size
 * limit, bad MIME, 404 on missing document).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:docit;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "wks.case-types.dir=",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      "wks.bootstrap.production-validation.enabled=false",
      "wks.documents.max-size-mb=1",
      "wks.storage.endpoint="
    })
class DocumentControllerIT {

  private static final String EMAIL = "doc-it-admin@wkspower.local";
  private static final String PASSWORD = "pass1234";
  private static final String CASE_TYPE_ID = "doc-it-loans";

  @TempDir Path tempDir;

  @Autowired private TestRestTemplate rest;
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private CaseTypeRegistry registry;
  @Autowired private ObjectMapper objectMapper;

  private String sessionCookie;
  private UUID caseId;

  @BeforeEach
  void setup() throws Exception {
    // Set TempDir on the LocalDocumentStore — override the auto-configured one.
    System.setProperty("wks.documents.local-store-path", tempDir.toString());

    // Seed user.
    if (users.findByEmail(EMAIL).isEmpty()) {
      users.save(
          new User(UUID.randomUUID(), EMAIL, Set.of("admin"), true), encoder.encode(PASSWORD));
    }

    // Register a minimal case type granting admin all verbs.
    registry.register(minimalCaseType());

    // Login and grab session cookie.
    sessionCookie = login();

    // Create a case to attach documents to.
    caseId = createCase();
  }

  // --- AC1: Upload ---

  @Test
  void upload_validPdf_returns201WithMetadata() throws Exception {
    ResponseEntity<String> resp = uploadFile("test.pdf", "application/pdf", pdfBytes());

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode doc = objectMapper.readTree(resp.getBody()).at("/data");
    assertThat(doc.at("/fileName").asText()).isEqualTo("test.pdf");
    assertThat(doc.at("/contentType").asText()).isEqualTo("application/pdf");
    assertThat(doc.at("/id").asText()).isNotBlank();
  }

  @Test
  void upload_fileTooLarge_returns422WithWksDoc001() throws Exception {
    // max-size-mb=1 in test properties → 1_048_576 bytes
    byte[] oversized = new byte[2 * 1024 * 1024]; // 2 MB
    ResponseEntity<String> resp = uploadFile("large.pdf", "application/pdf", oversized);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    String code = objectMapper.readTree(resp.getBody()).at("/error/code").asText();
    assertThat(code).isEqualTo("WKS-DOC-001");
  }

  @Test
  void upload_disallowedMime_returns422WithWksDoc002() throws Exception {
    ResponseEntity<String> resp =
        uploadFile("evil.bin", "application/octet-stream", new byte[] {1, 2, 3});

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    String code = objectMapper.readTree(resp.getBody()).at("/error/code").asText();
    assertThat(code).isEqualTo("WKS-DOC-002");
  }

  @Test
  void upload_executableExtension_returns422WithWksDoc003() throws Exception {
    ResponseEntity<String> resp = uploadFile("script.sh", "text/plain", "#!/bin/bash".getBytes());

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    String code = objectMapper.readTree(resp.getBody()).at("/error/code").asText();
    assertThat(code).isEqualTo("WKS-DOC-003");
  }

  // --- AC2: List + Download ---

  @Test
  void list_afterUpload_containsDocument() throws Exception {
    uploadFile("sample.pdf", "application/pdf", pdfBytes());

    HttpHeaders headers = authHeaders();
    ResponseEntity<String> resp =
        rest.exchange(
            "/api/cases/" + caseId + "/documents",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode list = objectMapper.readTree(resp.getBody()).at("/data");
    assertThat(list.isArray()).isTrue();
    assertThat(list.size()).isGreaterThanOrEqualTo(1);
    assertThat(list.get(0).at("/fileName").asText()).isEqualTo("sample.pdf");
  }

  @Test
  void download_afterUpload_streamsBytes() throws Exception {
    byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);
    ResponseEntity<String> uploadResp = uploadFile("note.txt", "text/plain", content);
    String docId = objectMapper.readTree(uploadResp.getBody()).at("/data/id").asText();

    HttpHeaders headers = authHeaders();
    ResponseEntity<byte[]> downloadResp =
        rest.exchange(
            "/api/documents/" + docId + "/download",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            byte[].class);

    assertThat(downloadResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(downloadResp.getBody()).isEqualTo(content);
  }

  @Test
  void download_unknownId_returns404WithWksDoc004() throws Exception {
    HttpHeaders headers = authHeaders();
    ResponseEntity<String> resp =
        rest.exchange(
            "/api/documents/" + UUID.randomUUID() + "/download",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    String code = objectMapper.readTree(resp.getBody()).at("/error/code").asText();
    assertThat(code).isEqualTo("WKS-DOC-004");
  }

  // --- AC3: Preview ---

  @Test
  void preview_pdfDocument_returnsPreviewableWithUrl() throws Exception {
    ResponseEntity<String> uploadResp = uploadFile("doc.pdf", "application/pdf", pdfBytes());
    String docId = objectMapper.readTree(uploadResp.getBody()).at("/data/id").asText();

    HttpHeaders headers = authHeaders();
    ResponseEntity<String> resp =
        rest.exchange(
            "/api/documents/" + docId + "/preview",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode data = objectMapper.readTree(resp.getBody()).at("/data");
    assertThat(data.at("/previewable").asBoolean()).isTrue();
    assertThat(data.at("/url").asText()).contains("/api/documents/" + docId + "/download");
  }

  @Test
  void preview_wordDocument_returnsNotPreviewable() throws Exception {
    ResponseEntity<String> uploadResp =
        uploadFile(
            "doc.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            new byte[] {0x50, 0x4B, 0x03, 0x04}); // minimal ZIP header (docx is a zip)
    String docId = objectMapper.readTree(uploadResp.getBody()).at("/data/id").asText();

    HttpHeaders headers = authHeaders();
    ResponseEntity<String> resp =
        rest.exchange(
            "/api/documents/" + docId + "/preview",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode data = objectMapper.readTree(resp.getBody()).at("/data");
    assertThat(data.at("/previewable").asBoolean()).isFalse();
    assertThat(data.at("/url").asText()).contains("/api/documents/" + docId + "/download");
  }

  // --- helpers ---

  private ResponseEntity<String> uploadFile(String fileName, String contentType, byte[] bytes) {
    HttpHeaders headers = authHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    ByteArrayResource resource =
        new ByteArrayResource(bytes) {
          @Override
          public String getFilename() {
            return fileName;
          }
        };

    HttpHeaders partHeaders = new HttpHeaders();
    partHeaders.setContentType(MediaType.parseMediaType(contentType));
    HttpEntity<ByteArrayResource> part = new HttpEntity<>(resource, partHeaders);
    body.add("file", part);

    return rest.exchange(
        "/api/cases/" + caseId + "/documents",
        HttpMethod.POST,
        new HttpEntity<>(body, headers),
        String.class);
  }

  private String login() {
    ResponseEntity<String> resp =
        rest.postForEntity("/api/auth/login", new LoginRequest(EMAIL, PASSWORD), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    String setCookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookie).isNotNull();
    int semi = setCookie.indexOf(';');
    return semi > 0 ? setCookie.substring(0, semi) : setCookie;
  }

  private UUID createCase() throws Exception {
    HttpHeaders headers = authHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String body = "{\"caseTypeId\":\"" + CASE_TYPE_ID + "\",\"data\":{}}";
    ResponseEntity<String> resp =
        rest.exchange("/api/cases", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String id = objectMapper.readTree(resp.getBody()).at("/data/id").asText();
    return UUID.fromString(id);
  }

  private HttpHeaders authHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.COOKIE, sessionCookie);
    return headers;
  }

  private static byte[] pdfBytes() {
    return "%PDF-1.4 minimal".getBytes(StandardCharsets.UTF_8);
  }

  private static CaseTypeConfig minimalCaseType() {
    // Null workflow (no BPMN process) — CaseService.create supports process-less paths per
    // Decision 19 (Story 3.2 extension). The engine is not required for document IT tests.
    return new CaseTypeConfig(
        CASE_TYPE_ID,
        "Doc IT Loans",
        1,
        null,
        null, // no workflow
        List.of(new FieldDefinition("amount", "Amount", FieldType.TEXT, false, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("amount"),
        List.of(
            new RoleDefinition(
                "admin",
                List.of(
                    Permission.CREATE, Permission.VIEW, Permission.EDIT, Permission.TRANSITION))));
  }
}
