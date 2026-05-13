package com.wkspower.platform.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.domain.event.CaseDataEdited;
import com.wkspower.platform.domain.model.AuditSource;
import com.wkspower.platform.infrastructure.persistence.AuditEventRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Story 9-3 AC6 — Postgres-IT proving the canonical tx/audit surface for the new {@code
 * audit_events} append-only table. This is the load-bearing parity test — the entire reason the
 * Postgres-IT parity rule exists per project memory.
 *
 * <p>Cases covered:
 *
 * <ul>
 *   <li>APPLIED roundtrip for each of the four {@link AuditSource} sealed variants (User / AutoRule
 *       / Backend / ExecutionUnmapped).
 *   <li>BLOCKED row carries open_task_id and form_id.
 *   <li>{@code REQUIRES_NEW} commit: the {@link AuditEventRepository#insert} commits even when
 *       called from inside a caller transaction that subsequently rolls back. (The real production
 *       path goes through {@code AFTER_COMMIT}, but {@code REQUIRES_NEW} is the load-bearing
 *       contract preventing audit rows from being silently rolled back when a future caller
 *       composes the insert into an existing transaction.)
 *   <li>Concurrent inserts: 2 threads × 100 events each, all 200 rows committed, no duplicate-id
 *       collisions.
 * </ul>
 *
 * <p>Test discipline:
 *
 * <ul>
 *   <li>NOT {@code @Transactional} per {@code feedback_postgres_it_committed_read} — every read
 *       goes through a fresh {@link TransactionTemplate} so we observe only committed state.
 *   <li>{@code wks.bootstrap.production-validation.enabled=false} per {@code
 *       feedback_production_validator_opt_out}.
 *   <li>Cases rows are inserted via plain JDBC so this IT does NOT depend on the case domain
 *       services — keeps the audit IT focused on the audit surface alone.
 * </ul>
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.NONE,
    properties = "wks.bootstrap.production-validation.enabled=false")
@ActiveProfiles("production")
@Testcontainers(disabledWithoutDocker = true)
class EditAuditPersistencePostgresIT {

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

  @Autowired private AuditEventRepository auditEventRepository;
  @Autowired private DataSource dataSource;
  @Autowired private PlatformTransactionManager txManager;
  @Autowired private ObjectMapper objectMapper;

  private JdbcTemplate jdbc;
  private TransactionTemplate freshTx;
  private UUID caseId;
  private UUID adminId;

  @BeforeEach
  void setUp() {
    jdbc = new JdbcTemplate(dataSource);
    freshTx = new TransactionTemplate(txManager);

    adminId =
        jdbc.queryForObject(
            "SELECT id FROM users WHERE email = ?", UUID.class, "admin@wkspower.local");
    caseId = insertMinimalCase(adminId);
  }

  @AfterEach
  void wipe() {
    // Order matters: audit_events FK to cases is ON DELETE RESTRICT, so audit first. Filter
    // cases by case_type_id so other tests' seed data survives if this IT class ever shares a
    // Testcontainer.
    jdbc.update(
        "DELETE FROM audit_events WHERE case_id IN (SELECT id FROM cases WHERE"
            + " case_type_id = ?)",
        "audit-it");
    jdbc.update("DELETE FROM cases WHERE case_type_id = ?", "audit-it");
  }

  // ============================================================ AC4 — variant roundtrips

  @Test
  void userVariantRoundTripsThroughPostgres() {
    UUID actorId = UUID.randomUUID();
    AuditEvent event = appliedEvent(new AuditSource.User(actorId));

    auditEventRepository.insert(event);

    List<AuditEvent> rows = freshTx.execute(s -> auditEventRepository.findByCaseId(caseId, 10));
    assertThat(rows).hasSize(1);
    AuditEvent persisted = rows.get(0);
    assertThat(persisted.source()).isInstanceOf(AuditSource.User.class);
    assertThat(((AuditSource.User) persisted.source()).actorId()).isEqualTo(actorId);
    assertThat(persisted.result()).isEqualTo("APPLIED");
    assertThat(persisted.eventType()).isEqualTo(AuditEvent.EVENT_TYPE_CASE_DATA_EDIT);
    assertThat(persisted.createdAt()).isNotNull(); // DB DEFAULT stamps it.

    // Also confirm the raw source_type column matches the wire-stable string.
    String rawType =
        freshTx.execute(
            s ->
                jdbc.queryForObject(
                    "SELECT source_type FROM audit_events WHERE id = ?", String.class, event.id()));
    assertThat(rawType).isEqualTo("USER");
  }

  @Test
  void autoRuleVariantRoundTripsThroughPostgres() {
    AuditEvent event = appliedEvent(new AuditSource.AutoRule("rule-onboard"));
    auditEventRepository.insert(event);

    AuditEvent persisted = readOnly(event.id());
    assertThat(persisted.source()).isInstanceOf(AuditSource.AutoRule.class);
    assertThat(((AuditSource.AutoRule) persisted.source()).ruleId()).isEqualTo("rule-onboard");
    String rawType = rawSourceType(event.id());
    assertThat(rawType).isEqualTo("AUTO_RULE");
  }

  @Test
  void backendVariantRoundTripsThroughPostgres() {
    AuditEvent event = appliedEvent(new AuditSource.Backend("bpmn"));
    auditEventRepository.insert(event);

    AuditEvent persisted = readOnly(event.id());
    assertThat(persisted.source()).isInstanceOf(AuditSource.Backend.class);
    assertThat(((AuditSource.Backend) persisted.source()).adapterName()).isEqualTo("bpmn");
    assertThat(rawSourceType(event.id())).isEqualTo("BACKEND");
  }

  @Test
  void executionUnmappedVariantRoundTripsThroughPostgres() {
    AuditEvent event = appliedEvent(new AuditSource.ExecutionUnmapped("camunda"));
    auditEventRepository.insert(event);

    AuditEvent persisted = readOnly(event.id());
    assertThat(persisted.source()).isInstanceOf(AuditSource.ExecutionUnmapped.class);
    assertThat(((AuditSource.ExecutionUnmapped) persisted.source()).originAdapter())
        .isEqualTo("camunda");
    assertThat(rawSourceType(event.id())).isEqualTo("EXECUTION_UNMAPPED");
  }

  // ============================================================ AC1/AC3 — BLOCKED row shape

  @Test
  void blockedEventPersistsRowWithTaskAndForm() {
    AuditEvent event =
        new AuditEvent(
            UUID.randomUUID(),
            caseId,
            AuditEvent.EVENT_TYPE_CASE_DATA_EDIT,
            new AuditSource.User(adminId),
            CaseDataEdited.Result.BLOCKED.name(),
            "customerEmail",
            "task-42",
            "form-onboard",
            Instant.parse("2026-05-14T10:00:00Z"),
            null);

    auditEventRepository.insert(event);

    AuditEvent persisted = readOnly(event.id());
    assertThat(persisted.result()).isEqualTo("BLOCKED");
    assertThat(persisted.fieldId()).isEqualTo("customerEmail");
    assertThat(persisted.openTaskId()).isEqualTo("task-42");
    assertThat(persisted.formId()).isEqualTo("form-onboard");
  }

  // ============================================================ AC5 — REQUIRES_NEW commit
  // isolation

  @Test
  void requiresNewCommitsEvenWhenCallerRollsBack() {
    // Drive the insert from inside an outer transaction that ROLLS BACK afterwards. Because the
    // repository declares REQUIRES_NEW, the audit row must survive the outer rollback. This is
    // the load-bearing isolation contract for any future call site that persists an audit row
    // mid-transaction (not via AFTER_COMMIT).
    AuditEvent event = appliedEvent(new AuditSource.User(adminId));

    try {
      freshTx.execute(
          s -> {
            auditEventRepository.insert(event);
            s.setRollbackOnly();
            return null;
          });
    } catch (UnexpectedRollbackException expected) {
      // Outer transaction rolled back as requested. The inner REQUIRES_NEW transaction is
      // independent and already committed before the rollback fires.
    }

    AuditEvent persisted = readOnly(event.id());
    assertThat(persisted).as("REQUIRES_NEW audit insert must survive outer rollback").isNotNull();
    assertThat(persisted.id()).isEqualTo(event.id());
  }

  // ============================================================ AC6 — concurrent inserts

  @Test
  void concurrentInsertsPersistEveryRowWithNoCollisions() throws Exception {
    int threads = 2;
    int perThread = 100;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threads);
    List<Exception> failures = new ArrayList<>();

    for (int t = 0; t < threads; t++) {
      pool.submit(
          () -> {
            try {
              start.await();
              for (int i = 0; i < perThread; i++) {
                auditEventRepository.insert(appliedEvent(new AuditSource.AutoRule("rule-conc")));
              }
            } catch (Exception e) {
              synchronized (failures) {
                failures.add(e);
              }
            } finally {
              done.countDown();
            }
          });
    }

    start.countDown();
    assertThat(done.await(60, TimeUnit.SECONDS))
        .as("concurrent insert pool should complete inside 60s")
        .isTrue();
    pool.shutdownNow();

    assertThat(failures).as("no thread should have thrown").isEmpty();

    Long total =
        freshTx.execute(
            s ->
                jdbc.queryForObject(
                    "SELECT COUNT(*) FROM audit_events WHERE case_id = ?", Long.class, caseId));
    assertThat(total).isEqualTo((long) threads * perThread);

    Long distinct =
        freshTx.execute(
            s ->
                jdbc.queryForObject(
                    "SELECT COUNT(DISTINCT id) FROM audit_events WHERE case_id = ?",
                    Long.class,
                    caseId));
    // This test exercises pool/connection capacity under contention, not uniqueness — the IDs are
    // random UUIDs so collisions are statistically impossible regardless. Real uniqueness lives at
    // the PK constraint.
    assertThat(distinct).as("connection pool handles concurrent inserts").isEqualTo(total);
  }

  // ============================================================ AC2 — append-only surface guard

  @Test
  void repositorySurfaceExposesOnlyInsertAndFindByCaseId() {
    // Defense-in-depth surface check (AC2 fallback per Story 9-3 spec). Verifies via reflection
    // that no save/update/delete/deleteAll/findAll methods exist on AuditEventRepository.
    var methods = AuditEventRepository.class.getDeclaredMethods();
    var publicNames =
        java.util.Arrays.stream(methods)
            .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
            .map(java.lang.reflect.Method::getName)
            .sorted()
            .toList();

    assertThat(publicNames).containsExactlyInAnyOrder("insert", "findByCaseId");
    assertThat(publicNames)
        .as("append-only invariant: no mutation methods allowed")
        .doesNotContain("save", "update", "delete", "deleteAll", "findAll", "deleteById");
  }

  // ============================================================ helpers

  private AuditEvent appliedEvent(AuditSource source) {
    return new AuditEvent(
        UUID.randomUUID(),
        caseId,
        AuditEvent.EVENT_TYPE_CASE_DATA_EDIT,
        source,
        CaseDataEdited.Result.APPLIED.name(),
        "customerName",
        null,
        null,
        Instant.parse("2026-05-14T10:00:00Z"),
        null);
  }

  /** Fresh-transaction read for a single audit row by id. Returns null if absent. */
  private AuditEvent readOnly(UUID id) {
    return freshTx.execute(
        s ->
            auditEventRepository.findByCaseId(caseId, 1000).stream()
                .filter(e -> e.id().equals(id))
                .findFirst()
                .orElse(null));
  }

  private String rawSourceType(UUID id) {
    return freshTx.execute(
        s ->
            jdbc.queryForObject(
                "SELECT source_type FROM audit_events WHERE id = ?", String.class, id));
  }

  /**
   * Inserts a minimal {@code cases} row sufficient to satisfy the {@code audit_events.case_id} FK.
   * Uses raw JDBC to keep this IT independent of the case domain services.
   */
  private UUID insertMinimalCase(UUID createdBy) {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    jdbc.update(
        "INSERT INTO cases (id, case_type_id, case_type_version, status, data, created_by,"
            + " version, created_at, updated_at) VALUES (?, ?, ?, ?, ?::json, ?, ?, ?, ?)",
        id,
        "audit-it",
        1,
        "open",
        "{}",
        createdBy,
        0L,
        Timestamp.from(now),
        Timestamp.from(now));
    return id;
  }
}
