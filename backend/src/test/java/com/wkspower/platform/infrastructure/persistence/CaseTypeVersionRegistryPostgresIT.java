package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
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
 * Story 3.4.1 AC2 — Postgres-backed integration test for {@link CaseTypeVersionRegistry}.
 *
 * <p>Discharges the {@code project_postgres_it_parity_gap} memory for the version-registry surface.
 * The H2-only IT in {@code CaseTypeVersionRegistryIT} cannot expose the Postgres SSI {@code 40001
 * serialization_failure} that finding C3 surfaced — H2's SERIALIZABLE serialises rather than
 * aborts, so the {@link org.springframework.dao.ConcurrencyFailureException}-vs- {@link
 * org.springframework.dao.DataIntegrityViolationException} sibling-not-subclass distinction is
 * invisible. AC1 dropped SERIALIZABLE in favour of {@code READ COMMITTED + PK + unique-by-hash};
 * this test proves correctness on the real Postgres engine.
 *
 * <p>Skipped automatically when Docker is unavailable.
 */
// Story 14.1.1: ProductionBootstrapValidator runs on ApplicationReadyEvent under
// activeProfiles=["production"] and enforces WKS-API-055 (every secret rotated +
// non-empty). This test only sets the DB-shape properties Testcontainers needs;
// disable the validator here so we exercise the registry surface, not the boot
// invariant. Boot-invariant coverage lives in production-profile-smoke (CI) +
// the validator's own unit tests.
@SpringBootTest(properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class CaseTypeVersionRegistryPostgresIT {

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

  private static final String J9_BASE =
      "id: j9-zero-zero\n"
          + "displayName: \"[J9] Zero Stages Zero Process\"\n"
          + "version: 1\n"
          + "roles:\n"
          + "  - name: admin\n"
          + "    permissions: [view, create, edit, transition]\n";

  @Autowired private CaseTypeVersionRegistry registry;
  @Autowired private CaseTypeVersionJpaRepository repo;

  @AfterEach
  void wipe() {
    repo.deleteAll();
  }

  // ---- AC2 (a) Concurrent register with same hash → exactly one row, all threads idempotent ----
  @Test
  void concurrentRegisterSameHashProducesExactlyOneRow() throws Exception {
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    @SuppressWarnings("unchecked")
    Future<CaseTypeVersionRegistration>[] futures = new Future[threads];
    try {
      for (int i = 0; i < threads; i++) {
        futures[i] =
            pool.submit(
                () -> {
                  start.await(2, TimeUnit.SECONDS);
                  return registry.register(
                      "j9-zero-zero", J9_BASE.getBytes(StandardCharsets.UTF_8), "system:startup");
                });
      }
      start.countDown();
      int successes = 0;
      for (Future<CaseTypeVersionRegistration> f : futures) {
        try {
          CaseTypeVersionRegistration r = f.get(15, TimeUnit.SECONDS);
          assertThat(r.version())
              .as("every winner thread observes v1 (the only legal version for this hash)")
              .isEqualTo(1);
          successes++;
        } catch (java.util.concurrent.ExecutionException ex) {
          // AC1 / option-b: with SERIALIZABLE dropped + PK + unique-by-hash,
          // every thread should ultimately return idempotent (or registered for the winner).
          // The adapter catches DataIntegrityViolationException AND ConcurrencyFailureException
          // (sibling, not subclass — the C3 finding) and re-reads by hash. Re-throw with cause to
          // make Postgres-divergence regressions loud.
          throw new AssertionError(
              "Concurrent same-hash register raced past the adapter's catch — finding C3 regressed",
              ex);
        }
      }
      assertThat(successes)
          .as("all threads must succeed (idempotent or registered) under same-hash concurrency")
          .isEqualTo(threads);
    } finally {
      pool.shutdown();
    }
    assertThat(repo.findAll())
        .as("AC2 (a) — exactly one row at v1; no duplicate inserts under concurrent same-hash")
        .hasSize(1);
    assertThat(repo.findAll().get(0).getVersion()).isEqualTo(1);
  }

  // ---- AC2 (b) Concurrent register with DIFFERENT hashes → exactly one row at v1, loser
  // documented ----
  @Test
  void concurrentRegisterDifferentHashesProducesOneRowAtV1() throws Exception {
    String yamlA = J9_BASE; // hashes one way
    String yamlB =
        J9_BASE.replace(
            "  - name: admin\n    permissions: [view, create, edit, transition]\n",
            "  - name: admin\n    permissions: [view, create, edit, transition]\n"
                + "  - name: viewer\n    permissions: [view]\n"); // hashes the other way

    int threadsPerHash = 3;
    int total = threadsPerHash * 2;
    ExecutorService pool = Executors.newFixedThreadPool(total);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<CaseTypeVersionRegistration>> futures = new ArrayList<>();
    try {
      for (int i = 0; i < threadsPerHash; i++) {
        futures.add(
            pool.submit(
                () -> {
                  start.await(2, TimeUnit.SECONDS);
                  return registry.register(
                      "j9-zero-zero", yamlA.getBytes(StandardCharsets.UTF_8), "system:startup");
                }));
        futures.add(
            pool.submit(
                () -> {
                  start.await(2, TimeUnit.SECONDS);
                  return registry.register(
                      "j9-zero-zero", yamlB.getBytes(StandardCharsets.UTF_8), "system:startup");
                }));
      }
      start.countDown();

      int v1 = 0;
      int v2 = 0;
      int losers = 0;
      for (Future<CaseTypeVersionRegistration> f : futures) {
        try {
          CaseTypeVersionRegistration r = f.get(15, TimeUnit.SECONDS);
          if (r.version() == 1) {
            v1++;
          } else if (r.version() == 2) {
            v2++;
          } else {
            throw new AssertionError(
                "Unexpected version " + r.version() + " — only 1 and 2 are reachable");
          }
        } catch (java.util.concurrent.ExecutionException ex) {
          // Documented loser-thread outcome (AC2 b): under READ COMMITTED + the unique-by-hash
          // index, the loser of a race for the same nextVersion may surface a
          // DataIntegrityViolationException whose hash *does not* match the winner's row (because
          // the loser's hash is from yamlB but the winner registered yamlA's hash at v1). The
          // adapter's re-read by (id, hash) returns empty and the exception propagates. This is
          // the documented loser semantic — at-least-one writer succeeds, AT-LEAST-ONE row exists,
          // and operators retrying see the second hash land at v2.
          losers++;
        }
      }
      // Either: one hash won at v1, the other landed at v2 (interleaved success — happy path), OR
      // one hash won and the other lost an integrity-violation race that the operator must retry.
      // Both outcomes satisfy "exactly one row per (id, version)".
      assertThat(v1).as("at least one thread succeeds at v1").isGreaterThanOrEqualTo(1);
      assertThat(v1 + v2 + losers).isEqualTo(total);
    } finally {
      pool.shutdown();
    }

    var rows = repo.findAll();
    assertThat(rows)
        .as("AC2 (b) — distinct (id, version) rows; PK + unique-by-hash enforced by Postgres")
        .hasSizeBetween(1, 2);
    // Every row must have a unique version for this caseTypeId.
    assertThat(rows.stream().map(e -> e.getVersion()).distinct().count()).isEqualTo(rows.size());
  }

  // ---- AC2 sanity: the production-profile flyway picks up the v202605060001 migration ----
  @Test
  void caseTypeVersionsTableExistsAndIndexIsEnforced() {
    // First insert lands.
    CaseTypeVersionRegistration r1 =
        registry.register(
            "j9-zero-zero", J9_BASE.getBytes(StandardCharsets.UTF_8), "system:startup");
    assertThat(r1.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.REGISTERED);
    assertThat(r1.version()).isEqualTo(1);

    // Byte-identical re-deploy short-circuits via the unique-by-hash idempotent path.
    CaseTypeVersionRegistration r2 =
        registry.register(
            "j9-zero-zero", J9_BASE.getBytes(StandardCharsets.UTF_8), "system:startup");
    assertThat(r2.outcome()).isEqualTo(CaseTypeVersionRegistration.Outcome.IDEMPOTENT);
    assertThat(r2.version()).isEqualTo(1);
    assertThat(repo.findAll()).hasSize(1);
  }
}
