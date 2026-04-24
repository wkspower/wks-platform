package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.infrastructure.persistence.entity.UserEntity;
import com.wkspower.platform.infrastructure.persistence.repository.UserEntityRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regression trip-wire (AC10) — not a benchmark. Asserts a 1000-row {@code findAll} round-trip on
 * H2 completes in under 200 ms on the CI runner after a 100-run warm-up. Tagged {@code
 * perf-guardrail} so it can be disabled locally via Maven Surefire/Failsafe group filters when a
 * dev machine is noisy, but runs in CI.
 *
 * <p>The 200 ms threshold is deliberately generous. It catches regressions from ~10 ms to seconds
 * (e.g. accidentally N+1 queries, bogus fetch joins) — it does not measure the PRD's "10 %
 * overhead" figure, which is architectural intent rather than a measurable gate (see Dev Notes /
 * Detected Variances in the story).
 */
@SpringBootTest
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:perfguardrail;DB_CLOSE_DELAY=-1",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.jpa.properties.hibernate.jdbc.batch_size=50",
      "wks.jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ="
    })
@Tag("perf-guardrail")
class UserRepositoryPerformanceIT {

  private static final int ROWS = 1000;
  private static final int WARMUP_RUNS = 100;
  private static final long THRESHOLD_MS = 200;

  @Autowired private UserEntityRepository users;

  @Test
  @Transactional
  void findAllThousandRowsUnderThresholdAfterWarmup() {
    // AdminUserSeeder may have pre-populated one admin row — use a minimum bound.
    int baseline = users.findAll().size();
    seedUsers();
    int expectedMinimum = ROWS + baseline;

    // Warm-up: JIT + Hibernate 2nd-level cache + JDBC connection ramp.
    for (int i = 0; i < WARMUP_RUNS; i++) {
      List<UserEntity> rows = users.findAll();
      assertThat(rows).hasSizeGreaterThanOrEqualTo(expectedMinimum);
    }

    long start = System.nanoTime();
    List<UserEntity> rows = users.findAll();
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;

    assertThat(rows).hasSizeGreaterThanOrEqualTo(expectedMinimum);
    assertThat(elapsedMs)
        .as(
            "findAll of %d rows on H2 exceeded the %d ms regression trip-wire. "
                + "Before raising the threshold, confirm the slowdown is infrastructure (cold "
                + "Testcontainers start, shared CI runner) and not a real regression (new N+1 "
                + "query, unintended EAGER fetch, etc.).",
            ROWS, THRESHOLD_MS)
        .isLessThan(THRESHOLD_MS);
  }

  private void seedUsers() {
    List<UserEntity> batch = new ArrayList<>(ROWS);
    Instant now = Instant.now();
    for (int i = 0; i < ROWS; i++) {
      batch.add(
          new UserEntity(
              UUID.randomUUID(),
              "perf-" + i + "@wkspower.local",
              "hash",
              true,
              now,
              now,
              new HashSet<>()));
    }
    users.saveAll(batch);
    users.flush();
  }
}
