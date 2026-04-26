package com.wkspower.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Under the {@code production} profile with {@code WKS_OPENAPI_ENABLED} unset, {@code /v3/api-docs}
 * must 404 — unauthenticated API-docs in production is a recurring CVE vector.
 *
 * <p>Datasource + auth env vars are injected via {@link TestPropertySource} so the production
 * profile boots without a live Postgres. H2 is used under the covers; what matters for this test is
 * the springdoc disablement, not the storage engine.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("production")
@TestPropertySource(
    properties = {
      "WKS_DB_URL=jdbc:h2:mem:openapi-prod;DB_CLOSE_DELAY=-1",
      "WKS_DB_USER=sa",
      "WKS_DB_PASSWORD=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=validate",
      // Override Flyway locations: production profile defaults to common/+postgresql/, but this
      // test boots production with an H2 datasource. postgresql/ migrations (V202604260002 JSONB
      // upgrade) are not H2-compatible, so we narrow to common/+h2/ here.
      "spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/h2",
      "WKS_CORS_ORIGINS=http://localhost:5173",
      "WKS_ADMIN_EMAIL=admin@wkspower.local",
      "WKS_ADMIN_PASSWORD=admin-test-only",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
class OpenApiDisabledInProductionIT {

  @Autowired private TestRestTemplate rest;

  @Test
  void apiDocsReturns404UnderProductionByDefault() {
    ResponseEntity<String> resp = rest.getForEntity("/v3/api-docs", String.class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
