package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.CaseRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Story 2.3 D5a — Postgres-backed integration test for {@link CaseRepository}.
 *
 * <p>Proves the JSONB upgrade migration in {@code db/migration/postgresql/V202604260002} applies
 * cleanly on a real Postgres, that the {@code data} column is JSONB (not JSON) post-migration, and
 * that {@code save → findById → findDataByIds} round-trips a case through the real schema with the
 * dynamic JSON map intact.
 *
 * <p>Skipped automatically when Docker is unavailable.
 */
@SpringBootTest
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class CaseRepositoryPostgresIT {

  @Container
  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("wks")
          .withUsername("wks")
          .withPassword("wks");

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("WKS_DB_URL", POSTGRES::getJdbcUrl);
    registry.add("WKS_DB_USER", POSTGRES::getUsername);
    registry.add("WKS_DB_PASSWORD", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("WKS_ADMIN_EMAIL", () -> "admin@wkspower.local");
    registry.add("WKS_ADMIN_PASSWORD", () -> "admin");
    registry.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
    registry.add("WKS_CORS_ORIGINS", () -> "http://localhost:5173");
    // Story 14.1.1: opt out of ProductionBootstrapValidator's WKS-API-055 secret
    // rotation check. The Testcontainers fixture sets enough env vars for the JDBC
    // round-trip but does not populate WKS_STORAGE_KEY / WKS_MINIO_* (out of scope
    // for repository-layer ITs).
    registry.add("wks.bootstrap.production-validation.enabled", () -> "false");
  }

  @Autowired private CaseRepository cases;
  @Autowired private DataSource dataSource;

  @Test
  void dataColumnIsJsonbAfterPostgresqlMigration() throws Exception {
    try (var conn = dataSource.getConnection();
        var stmt = conn.createStatement();
        var rs =
            stmt.executeQuery(
                "SELECT data_type FROM information_schema.columns "
                    + "WHERE table_name = 'cases' AND column_name = 'data'")) {
      assertThat(rs.next()).as("cases.data column should exist").isTrue();
      assertThat(rs.getString(1).toLowerCase())
          .as("V202604260002 must upgrade cases.data from JSON to JSONB on Postgres")
          .isEqualTo("jsonb");
    }
  }

  @Test
  void roundTripCasePreservesJsonData() {
    UUID actorId = bootstrapAdminId();
    UUID caseId = UUID.randomUUID();
    Instant now = Instant.now();
    Map<String, Object> data =
        Map.of(
            "applicant_name",
            "Alice",
            "amount",
            12345,
            "metadata",
            Map.of("channel", "web", "tags", java.util.List.of("priority", "vip")));
    Case toSave =
        new Case(caseId, "loan-application", 1, "open", null, data, "pi-1", now, actorId, now, 0L);

    Case saved = cases.save(toSave);
    assertThat(saved.id()).isEqualTo(caseId);

    assertThat(cases.findById(caseId))
        .hasValueSatisfying(
            found -> {
              assertThat(found.data()).containsEntry("applicant_name", "Alice");
              assertThat(found.data()).containsEntry("amount", 12345);
              assertThat(found.data().get("metadata")).isInstanceOf(Map.class);
            });

    Map<UUID, Map<String, Object>> projected =
        cases.findDataByIds(java.util.List.of(caseId), Set.of("applicant_name", "amount"));
    assertThat(projected).containsKey(caseId);
    assertThat(projected.get(caseId)).containsOnlyKeys("applicant_name", "amount");
  }

  /** Read the seeded admin user id directly so we satisfy the {@code created_by} FK. */
  private UUID bootstrapAdminId() {
    try (var conn = dataSource.getConnection();
        var stmt = conn.createStatement();
        var rs = stmt.executeQuery("SELECT id FROM users LIMIT 1")) {
      assertThat(rs.next()).as("at least one user should exist for the FK").isTrue();
      return UUID.fromString(rs.getString(1));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
