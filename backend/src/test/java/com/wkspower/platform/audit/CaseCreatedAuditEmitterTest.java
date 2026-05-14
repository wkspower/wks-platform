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
import com.wkspower.platform.domain.event.CaseCreated;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.slf4j.LoggerFactory;

class CaseCreatedAuditEmitterTest {

  private AuditEventWriter repository;
  private CaseCreatedAuditEmitter emitter;
  private Logger logbackLogger;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUp() {
    repository = mock(AuditEventWriter.class);
    emitter = new CaseCreatedAuditEmitter(repository);
    logbackLogger = (Logger) LoggerFactory.getLogger(CaseCreatedAuditEmitter.class);
    appender = new ListAppender<>();
    appender.start();
    logbackLogger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logbackLogger.detachAppender(appender);
  }

  @Test
  void event_persistsThenLogsWireLine() {
    CaseCreated event = createdEvent();
    doNothing().when(repository).insert(any());

    emitter.onCaseCreated(event);

    InOrder order = inOrder(repository);
    order.verify(repository).insert(any(AuditEvent.class));

    assertThat(appender.list).hasSize(1);
    ILoggingEvent line = appender.list.get(0);
    assertThat(line.getLevel()).isEqualTo(Level.INFO);
    assertThat(line.getFormattedMessage())
        .contains("event=case.created")
        .contains("caseId=" + event.caseId())
        .contains("caseTypeId=vendor-onboarding")
        .contains("caseTypeVersion=1");
  }

  @Test
  void insertFailure_logsWarnAndStillEmitsWireLine() {
    CaseCreated event = createdEvent();
    doThrow(new RuntimeException("Connection refused")).when(repository).insert(any());

    emitter.onCaseCreated(event);

    assertThat(appender.list).hasSize(2);

    ILoggingEvent warn = appender.list.get(0);
    assertThat(warn.getLevel()).isEqualTo(Level.WARN);
    assertThat(warn.getFormattedMessage())
        .contains("event=audit.persist.failed")
        .contains("reason=java.lang.RuntimeException: Connection refused")
        .contains("caseId=" + event.caseId());

    ILoggingEvent info = appender.list.get(1);
    assertThat(info.getLevel()).isEqualTo(Level.INFO);
    assertThat(info.getFormattedMessage())
        .as("slf4j wire-contract line must STILL fire on persist failure")
        .contains("event=case.created");
  }

  private CaseCreated createdEvent() {
    return new CaseCreated(
        UUID.fromString("c1c1c1c1-1111-2222-3333-444444444444"),
        "vendor-onboarding",
        1,
        UUID.fromString("a1a1a1a1-1111-2222-3333-444444444444"),
        Instant.parse("2026-05-14T10:00:00Z"));
  }
}
