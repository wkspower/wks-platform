package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

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
 * Story 2.5 AC11 #2 — locks in the {@code idx_cases_status} contract on Postgres.
 *
 * <p>The index was created in {@code V202604260001__create_cases_table.sql} (Story 2.3); 2.5's
 * frontend lets users sort the case list by {@code status} so this guardrail keeps the index from
 * silently disappearing in a future migration. H2 plan output differs and is not the production
 * path — the H2 IT skips the EXPLAIN check and only the schema metadata side-pins the index name.
 *
 * <p>Postgres-only: skipped automatically when Docker is unavailable.
 */
@SpringBootTest
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class CaseStatusIndexIT {

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
  }

  @Autowired private DataSource dataSource;

  @Test
  void casesStatusIndexExists() throws Exception {
    try (var conn = dataSource.getConnection();
        var stmt = conn.createStatement();
        var rs =
            stmt.executeQuery(
                "SELECT indexname FROM pg_indexes "
                    + "WHERE tablename = 'cases' AND indexname = 'idx_cases_status'")) {
      assertThat(rs.next())
          .as("idx_cases_status must exist on the cases table — Story 2.5 frontend sorts by status")
          .isTrue();
    }
  }

  @Test
  void plannerUsesStatusIndexWhenSeqScanDisabled() throws Exception {
    try (var conn = dataSource.getConnection();
        var stmt = conn.createStatement()) {
      // With enable_seqscan disabled the planner is forced onto the cheapest non-seq path —
      // proves idx_cases_status is functional, not just declared. We don't assert "the planner
      // ALWAYS picks the index" because at low row counts a seq scan is genuinely cheaper.
      stmt.execute("SET LOCAL enable_seqscan = off");
      try (var rs =
          stmt.executeQuery("EXPLAIN SELECT id FROM cases WHERE status = 'open' ORDER BY status")) {
        StringBuilder plan = new StringBuilder();
        while (rs.next()) {
          plan.append(rs.getString(1)).append('\n');
        }
        assertThat(plan.toString())
            .as("EXPLAIN should reference idx_cases_status when seqscan is off")
            .contains("idx_cases_status");
      }
    }
  }
}
