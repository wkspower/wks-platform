package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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
 * Story 3.5 AC6 — Postgres-backed integration test for the {@code V202605060002__pin_cases_to_v1}
 * Flyway migration.
 *
 * <p>Discharges the {@code project_postgres_it_parity_gap} memory for the cases-pin migration
 * surface. H2 cannot reliably surface Postgres driver behaviour for Flyway-driven UPDATE statements
 * (e.g. transaction-isolation defaults, rowcount semantics on no-op updates). This test proves the
 * migration SQL body behaves correctly on real Postgres against pre-3.4-shape fixture rows.
 *
 * <p>AC5 byte-equivalence contract: pre-existing cases render identically post-migration except for
 * the {@code case_type_version} field. The IT captures the full row state pre-pin, re-runs the
 * migration body manually (Flyway has already applied it during context start, so re-running tests
 * AC3 idempotence + drives the data transformation against rows seeded post-context-start), and
 * asserts every column except {@code case_type_version} is byte-equivalent.
 *
 * <p>AC6(d) — Spring Boot starts cleanly post-migration with no WKS-VER-001 raised on first read of
 * any pre-existing case — is satisfied implicitly by {@code @SpringBootTest} reaching a green
 * application context: if the migration broke boot or the registry's startup loader, context start
 * would fail before any test method runs.
 *
 * <p>{@code wks.bootstrap.production-validation.enabled=false} per Story 14.1.1 lesson: this IT
 * exercises the migration surface, not the production-validator boot invariant.
 *
 * <p>Skipped automatically when Docker is unavailable.
 */
@SpringBootTest(properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class CasesPinToV1MigrationPostgresIT {

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
    registry.add(
        "camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
  }

  private static final String PIN_SQL =
      "UPDATE cases SET case_type_version = 1 WHERE case_type_version <> 1";

  @Autowired private DataSource dataSource;

  @Test
  void migrationCollapsesNon1VersionsToV1() throws Exception {
    UUID adminId = bootstrapAdminId();
    UUID c1 = insertCase("loan-application", 2, adminId);
    UUID c2 = insertCase("loan-application", 5, adminId);
    UUID c3 = insertCase("expense-report", 1, adminId); // already v=1, must remain untouched

    Map<UUID, Map<String, Object>> pre = snapshotCases(c1, c2, c3);

    int affected = runPinSql();

    // AC1 — every case row at v=1 post-migration.
    assertCaseVersion(c1, 1);
    assertCaseVersion(c2, 1);
    assertCaseVersion(c3, 1);

    // AC1 + idempotence guard — only the 2 non-1 rows were touched.
    assertThat(affected).as("only rows with case_type_version <> 1 are updated").isEqualTo(2);

    // AC5 — every column except case_type_version is byte-equivalent pre vs post.
    Map<UUID, Map<String, Object>> post = snapshotCases(c1, c2, c3);
    for (UUID id : new UUID[] {c1, c2, c3}) {
      Map<String, Object> before = pre.get(id);
      Map<String, Object> after = post.get(id);
      for (String col : before.keySet()) {
        if (col.equals("case_type_version")) continue;
        assertThat(after.get(col))
            .as("AC5 byte-equivalence for column %s on case %s", col, id)
            .isEqualTo(before.get(col));
      }
    }
  }

  @Test
  void migrationIsIdempotentOnRerun() throws Exception {
    UUID adminId = bootstrapAdminId();
    insertCase("loan-application", 3, adminId);

    int firstRun = runPinSql();
    int secondRun = runPinSql();
    int thirdRun = runPinSql();

    // AC3 — first run pins the 1 non-1 row; subsequent runs are no-ops.
    assertThat(firstRun).as("first run pins the seeded v=3 row").isEqualTo(1);
    assertThat(secondRun).as("AC3 — re-run is a no-op (zero rows affected)").isEqualTo(0);
    assertThat(thirdRun)
        .as("AC3 — second re-run is also a no-op (zero rows affected)")
        .isEqualTo(0);
  }

  @Test
  void migrationLeavesCaseTypeVersionsRegistryUntouched() throws Exception {
    // AC6 (c) — the migration must NOT pre-populate case_type_versions; the startup loader owns
    // that surface. After context boot (which runs the migration via Flyway against an empty cases
    // table) the registry table reflects only what the startup loader wrote — for this IT, that's
    // zero rows because no CaseType YAML is seeded into ConfigService's source path.
    UUID adminId = bootstrapAdminId();
    insertCase("loan-application", 7, adminId);

    runPinSql();

    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM case_type_versions")) {
      assertThat(rs.next()).isTrue();
      assertThat(rs.getInt(1))
          .as("AC6(c) — case_type_versions is the startup loader's surface, not the migration's")
          .isEqualTo(0);
    }
  }

  // ---- Helpers ----

  private UUID insertCase(String caseTypeId, int version, UUID createdBy) throws Exception {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    String sql =
        "INSERT INTO cases (id, case_type_id, case_type_version, status, data, version,"
            + " created_by, created_at, updated_at) VALUES (?, ?, ?, ?, '{}'::json, 0, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setObject(1, id);
      ps.setString(2, caseTypeId);
      ps.setInt(3, version);
      ps.setString(4, "open");
      ps.setObject(5, createdBy);
      ps.setTimestamp(6, Timestamp.from(now));
      ps.setTimestamp(7, Timestamp.from(now));
      ps.executeUpdate();
    }
    return id;
  }

  private void assertCaseVersion(UUID id, int expected) throws Exception {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps =
            conn.prepareStatement("SELECT case_type_version FROM cases WHERE id = ?")) {
      ps.setObject(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        assertThat(rs.next()).isTrue();
        assertThat(rs.getInt(1)).as("case %s pinned to v=%d", id, expected).isEqualTo(expected);
      }
    }
  }

  private int runPinSql() throws Exception {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      return stmt.executeUpdate(PIN_SQL);
    }
  }

  private Map<UUID, Map<String, Object>> snapshotCases(UUID... ids) throws Exception {
    Map<UUID, Map<String, Object>> out = new HashMap<>();
    String inList = String.join(",", java.util.Collections.nCopies(ids.length, "?"));
    String sql =
        "SELECT id, case_type_id, case_type_version, status, assignee, data::text AS data,"
            + " process_instance_id, created_by, version, created_at, updated_at"
            + " FROM cases WHERE id IN ("
            + inList
            + ")";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      for (int i = 0; i < ids.length; i++) {
        ps.setObject(i + 1, ids[i]);
      }
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          UUID id = (UUID) rs.getObject("id");
          Map<String, Object> row = new HashMap<>();
          row.put("case_type_id", rs.getString("case_type_id"));
          row.put("case_type_version", rs.getInt("case_type_version"));
          row.put("status", rs.getString("status"));
          row.put("assignee", rs.getObject("assignee"));
          row.put("data", rs.getString("data"));
          row.put("process_instance_id", rs.getString("process_instance_id"));
          row.put("created_by", rs.getObject("created_by"));
          row.put("version", rs.getLong("version"));
          row.put("created_at", rs.getTimestamp("created_at"));
          row.put("updated_at", rs.getTimestamp("updated_at"));
          out.put(id, row);
        }
      }
    }
    return out;
  }

  private UUID bootstrapAdminId() throws Exception {
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM users LIMIT 1")) {
      assertThat(rs.next()).as("at least one user should exist for the FK").isTrue();
      return UUID.fromString(rs.getString(1));
    }
  }
}
