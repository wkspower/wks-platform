package com.wkspower.platform.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.wkspower.platform.domain.event.CaseDataEdited;
import com.wkspower.platform.domain.model.AuditSource;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.slf4j.LoggerFactory;

/**
 * Story 9-3 AC3 unit guard — {@link EditAuditEmitter} persists the audit row FIRST then emits the
 * existing slf4j wire-contract line. If the insert throws, a WARN {@code
 * event=audit.persist.failed} line is emitted AND the original {@code event=case.data.edit} line
 * still fires (visibility wins over consistency for the audit channel).
 *
 * <p>Logback appender capture, not Spring context — fast unit. The Postgres-IT exercises the same
 * persist-then-log ordering on a real DB.
 */
class EditAuditEmitterTest {

  private AuditEventWriter repository;
  private EditAuditEmitter emitter;
  private Logger logbackLogger;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUp() {
    repository = mock(AuditEventWriter.class);
    emitter = new EditAuditEmitter(repository);
    logbackLogger = (Logger) LoggerFactory.getLogger(EditAuditEmitter.class);
    appender = new ListAppender<>();
    appender.start();
    logbackLogger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logbackLogger.detachAppender(appender);
  }

  @Test
  void appliedEvent_persistsThenLogs() {
    CaseDataEdited event = appliedEvent();
    doNothing().when(repository).insert(any());

    emitter.onCaseDataEdited(event);

    // Persist first, then log: assert ordering via Mockito InOrder + appender index.
    InOrder order = inOrder(repository);
    order.verify(repository).insert(any(AuditEvent.class));

    assertThat(appender.list).hasSize(1);
    ILoggingEvent line = appender.list.get(0);
    assertThat(line.getLevel()).isEqualTo(Level.INFO);
    assertThat(line.getFormattedMessage())
        .contains("event=case.data.edit")
        .contains("result=APPLIED")
        .contains("fieldId=customerName");
  }

  @Test
  void blockedEvent_persistsThenLogsWithTaskAndForm() {
    CaseDataEdited event = blockedEvent();
    doNothing().when(repository).insert(any());

    emitter.onCaseDataEdited(event);

    InOrder order = inOrder(repository);
    order.verify(repository).insert(any(AuditEvent.class));

    assertThat(appender.list).hasSize(1);
    ILoggingEvent line = appender.list.get(0);
    assertThat(line.getLevel()).isEqualTo(Level.INFO);
    assertThat(line.getFormattedMessage())
        .contains("event=case.data.edit")
        .contains("result=BLOCKED")
        .contains("openTaskId=task-42")
        .contains("formId=form-onboard");
  }

  @Test
  void insertFailure_logsWarnAndStillEmitsWireLine() {
    CaseDataEdited event = appliedEvent();
    doThrow(new RuntimeException("Connection refused")).when(repository).insert(any());

    emitter.onCaseDataEdited(event);

    // BOTH lines must fire: persist.failed WARN AND the case.data.edit INFO. Order: WARN first
    // because the catch block runs before falling through to the wire-contract log.
    assertThat(appender.list).hasSize(2);

    ILoggingEvent warn = appender.list.get(0);
    assertThat(warn.getLevel()).isEqualTo(Level.WARN);
    assertThat(warn.getFormattedMessage())
        .contains("event=audit.persist.failed")
        .contains("reason=Connection refused")
        .contains("caseId=" + event.caseId());

    ILoggingEvent info = appender.list.get(1);
    assertThat(info.getLevel()).isEqualTo(Level.INFO);
    assertThat(info.getFormattedMessage())
        .as("slf4j wire-contract line must STILL fire on persist failure")
        .contains("event=case.data.edit")
        .contains("result=APPLIED");
  }

  // -------------------- fixtures --------------------

  private CaseDataEdited appliedEvent() {
    return new CaseDataEdited(
        UUID.fromString("c1c1c1c1-1111-2222-3333-444444444444"),
        new AuditSource.User(UUID.fromString("a1a1a1a1-1111-2222-3333-444444444444")),
        CaseDataEdited.Result.APPLIED,
        "customerName",
        null,
        null,
        Instant.parse("2026-05-14T10:00:00Z"));
  }

  private CaseDataEdited blockedEvent() {
    return new CaseDataEdited(
        UUID.fromString("c1c1c1c1-1111-2222-3333-444444444444"),
        new AuditSource.User(UUID.fromString("a1a1a1a1-1111-2222-3333-444444444444")),
        CaseDataEdited.Result.BLOCKED,
        "customerEmail",
        "task-42",
        "form-onboard",
        Instant.parse("2026-05-14T10:00:00Z"));
  }
}
