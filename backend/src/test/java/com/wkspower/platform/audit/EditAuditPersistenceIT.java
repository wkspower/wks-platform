package com.wkspower.platform.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.event.CaseDataEdited;
import com.wkspower.platform.domain.model.AuditSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Story 9-3 — H2 smoke IT mirroring the {@link EditAuditPersistencePostgresIT} core round-trip. The
 * load-bearing parity proof lives on the Postgres-IT; this class guards the inner dev loop ({@code
 * -Pfast-it}) so a JSON-column regression on H2 fails fast without spinning up Testcontainers.
 *
 * <p>Covers the 4 AuditSource variants and verifies they each round-trip through H2's portable
 * {@code JSON} column type. Concurrency + REQUIRES_NEW rollback isolation are NOT replicated here —
 * those live exclusively on the Postgres-IT per parity-gap doctrine.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class EditAuditPersistenceIT {

  @Autowired private AuditEventRepository auditEventRepository;
  @Autowired private DataSource dataSource;
  @Autowired private PlatformTransactionManager txManager;

  private JdbcTemplate jdbc;
  private TransactionTemplate freshTx;
  private UUID caseId;

  @BeforeEach
  void setUp() {
    jdbc = new JdbcTemplate(dataSource);
    freshTx = new TransactionTemplate(txManager);

    UUID adminId =
        jdbc.queryForObject(
            "SELECT id FROM users WHERE email = ?", UUID.class, "admin@wkspower.local");
    caseId = insertMinimalCase(adminId);
  }

  @AfterEach
  void wipe() {
    jdbc.update("DELETE FROM audit_events");
    jdbc.update("DELETE FROM cases WHERE case_type_id = ?", "audit-it");
  }

  @Test
  void allFourVariantsRoundTripThroughH2Json() {
    UUID actorId = UUID.randomUUID();
    AuditEvent user = applied(new AuditSource.User(actorId));
    AuditEvent auto = applied(new AuditSource.AutoRule("rule-fast-path"));
    AuditEvent backend = applied(new AuditSource.Backend("bpmn"));
    AuditEvent execUn = applied(new AuditSource.ExecutionUnmapped("camunda"));

    auditEventRepository.insert(user);
    auditEventRepository.insert(auto);
    auditEventRepository.insert(backend);
    auditEventRepository.insert(execUn);

    List<AuditEvent> rows = freshTx.execute(s -> auditEventRepository.findByCaseId(caseId, 10));
    assertThat(rows).hasSize(4);
    assertThat(rows)
        .extracting(AuditEvent::source)
        .containsExactlyInAnyOrder(user.source(), auto.source(), backend.source(), execUn.source());

    // Each row's createdAt is DB-stamped, not caller-provided.
    rows.forEach(r -> assertThat(r.createdAt()).isNotNull());
  }

  private AuditEvent applied(AuditSource source) {
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

  private UUID insertMinimalCase(UUID createdBy) {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    jdbc.update(
        "INSERT INTO cases (id, case_type_id, case_type_version, status, data, created_by,"
            + " version, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
