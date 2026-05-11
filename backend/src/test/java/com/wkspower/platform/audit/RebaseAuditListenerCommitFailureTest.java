package com.wkspower.platform.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.wkspower.platform.domain.event.RebaseApplied;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Story 3.9 review remediation — proves that when the surrounding transaction is rolled back, the
 * {@link RebaseAuditListener} does NOT emit the {@code event=admin.case.rebase} audit line. The
 * test registers a {@link TransactionSynchronization} that throws in {@code beforeCommit}, forcing
 * a rollback after the {@code RebaseApplied} event has been published inside the tx. A Logback
 * {@link ListAppender} attached to the listener captures any audit lines so absence can be
 * asserted positively.
 */
@SpringBootTest
@ActiveProfiles("dev")
class RebaseAuditListenerCommitFailureTest {

  @org.junit.jupiter.api.io.TempDir static java.nio.file.Path dbDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) {
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("commit-fail-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("wks.case-types.dir", () -> "");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
    reg.add("wks.jwt.secret", () -> "dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ=");
  }

  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private PlatformTransactionManager txManager;

  private ListAppender<ILoggingEvent> appender;
  private Logger listenerLogger;

  @BeforeEach
  void attachAppender() {
    listenerLogger = (Logger) LoggerFactory.getLogger(RebaseAuditListener.class);
    appender = new ListAppender<>();
    appender.start();
    listenerLogger.addAppender(appender);
  }

  @AfterEach
  void detachAppender() {
    if (listenerLogger != null && appender != null) {
      listenerLogger.detachAppender(appender);
    }
  }

  @Test
  void rollback_doesNotEmitAuditLine() {
    TransactionTemplate tx = new TransactionTemplate(txManager);

    assertThatThrownBy(
            () ->
                tx.execute(
                    new TransactionCallbackWithoutResult() {
                      @Override
                      protected void doInTransactionWithoutResult(TransactionStatus status) {
                        // Publish the RebaseApplied event inside the tx. With
                        // @TransactionalEventListener(AFTER_COMMIT), delivery depends on commit.
                        publisher.publishEvent(
                            new RebaseApplied(
                                UUID.randomUUID(),
                                "test-ct",
                                1,
                                2,
                                "migration-rebase",
                                "migration-rebase",
                                "operator@wkspower.local",
                                "req-rollback-test",
                                Instant.now()));

                        // Register a synchronization that throws in beforeCommit → forces rollback.
                        TransactionSynchronizationManager.registerSynchronization(
                            new TransactionSynchronization() {
                              @Override
                              public void beforeCommit(boolean readOnly) {
                                throw new RuntimeException(
                                    "forced rollback (commit-failure proof)");
                              }
                            });
                      }
                    }))
        .isInstanceOf(RuntimeException.class);

    boolean anyAuditLine =
        appender.list.stream()
            .filter(e -> e.getLevel() == Level.INFO)
            .map(ILoggingEvent::getFormattedMessage)
            .anyMatch(m -> m.contains("event=admin.case.rebase"));
    assertThat(anyAuditLine)
        .as("audit line MUST NOT fire when the surrounding transaction rolled back")
        .isFalse();
  }
}
