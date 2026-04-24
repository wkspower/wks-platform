package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the generated OpenAPI spec under the default (dev) profile: reachable without auth,
 * documents every existing controller operation, and declares the {@code cookieAuth} scheme
 * reflecting AD-6.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:openapidocs;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=validate",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=",
      // Pin springdoc ON so an ambient SPRING_PROFILES_ACTIVE=production in CI cannot silently
      // flip this test's premise (dev profile = docs enabled). The sibling
      // OpenApiDisabledInProductionIT covers the production-disabled path explicitly.
      "springdoc.api-docs.enabled=true",
      "springdoc.swagger-ui.enabled=true"
    })
class OpenApiDocsIT {

  @Autowired private TestRestTemplate rest;
  @Autowired private ObjectMapper mapper;

  @Test
  void apiDocsIsReachableUnauthenticatedAndDocumentsKnownOperations() throws Exception {
    ResponseEntity<String> resp = rest.getForEntity("/v3/api-docs", String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode spec = mapper.readTree(resp.getBody());

    // OpenAPI 3.x — springdoc 2.x generates 3.0.x by default, compatible with 3.1 consumers.
    assertThat(spec.path("openapi").asText()).startsWith("3.");

    JsonNode paths = spec.path("paths");
    assertThat(paths.has("/api/health")).as("health endpoint documented").isTrue();
    assertThat(paths.has("/api/auth/login")).as("login endpoint documented").isTrue();
    assertThat(paths.has("/api/auth/logout")).as("logout endpoint documented").isTrue();
    assertThat(paths.has("/api/auth/me")).as("me endpoint documented").isTrue();

    JsonNode cookieAuth = spec.path("components").path("securitySchemes").path("cookieAuth");
    assertThat(cookieAuth.path("type").asText()).isEqualTo("apiKey");
    assertThat(cookieAuth.path("in").asText()).isEqualTo("cookie");
    assertThat(cookieAuth.path("name").asText()).isEqualTo("WKS_SESSION");
  }

  @Test
  void swaggerUiIsReachableUnauthenticated() {
    ResponseEntity<String> resp = rest.getForEntity("/swagger-ui/index.html", String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resp.getBody()).contains("Swagger UI");
  }
}
