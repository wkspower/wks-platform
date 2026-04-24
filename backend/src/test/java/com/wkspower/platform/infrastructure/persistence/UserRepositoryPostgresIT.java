package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Testcontainers-backed integration test proving the JPA stack boots against real PostgreSQL, runs
 * all committed Flyway migrations to completion, and round-trips a {@code UserEntity} through
 * {@code UserRepositoryAdapter.save + findByEmail}.
 *
 * <p>Skipped automatically when Docker is unavailable — Testcontainers' own {@code
 * DockerClientFactory.isDockerAvailable()} check is performed when the {@code @Container} boots; in
 * CI the {@code CI} env var is set and Docker is present so the test always runs there.
 *
 * <p>Production-profile properties are injected via {@link DynamicPropertySource} so the context
 * reads from {@code WKS_DB_*} exactly as it would in deployment, and the dev H2 defaults do not
 * leak in.
 */
@SpringBootTest
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class UserRepositoryPostgresIT {

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
    // Ensure driver resolution under production profile's URL-inferred setup.
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    // Test-only admin + JWT material so the context boots without external env vars.
    registry.add("WKS_ADMIN_EMAIL", () -> "admin@wkspower.local");
    registry.add("WKS_ADMIN_PASSWORD", () -> "admin");
    registry.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
    registry.add("WKS_CORS_ORIGINS", () -> "http://localhost:5173");
  }

  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder encoder;
  @Autowired private DataSource dataSource;

  @Test
  void migrationsRanToCompletionAndUserRoundTrips() throws Exception {
    // 1. Every committed migration landed in flyway_schema_history with success=true.
    try (var conn = dataSource.getConnection();
        var stmt = conn.createStatement();
        var rs =
            stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE")) {
      assertThat(rs.next()).isTrue();
      int count = rs.getInt(1);
      assertThat(count)
          .as("all committed Flyway migrations should have run to success")
          .isGreaterThanOrEqualTo(3);
    }

    // 2. Round-trip a user through the adapter on the real Postgres schema.
    String email = "roundtrip-" + UUID.randomUUID() + "@wkspower.local";
    User created =
        users.save(
            new User(UUID.randomUUID(), email, Set.of("admin"), true),
            encoder.encode("not-used-in-this-test"));

    assertThat(users.findByEmail(email))
        .hasValueSatisfying(
            found -> {
              assertThat(found.id()).isEqualTo(created.id());
              assertThat(found.email()).isEqualTo(email);
              assertThat(found.roles()).containsExactly("admin");
              assertThat(found.active()).isTrue();
            });
  }
}
