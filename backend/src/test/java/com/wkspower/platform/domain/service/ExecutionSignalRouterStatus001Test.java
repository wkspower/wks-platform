package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.OutcomeMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 6.2 — focused unit coverage for {@link ExecutionSignalRouter#resetStatusForAdvancedStage}
 * WKS-STATUS-001 warn path. When stage advance succeeds but the next stage's status set is empty
 * (no stage-scoped statuses, no top-level fallback), the router must log WARN with
 * wksErrorCode=WKS-STATUS-001 and leave the case status untouched.
 */
class ExecutionSignalRouterStatus001Test {

  private static final UUID CASE_ID = UUID.randomUUID();
  private static final String CASE_TYPE_ID = "ct-status001";
  private static final Instant NOW = Instant.parse("2026-05-11T10:00:00Z");

  @Test
  void resetStatusForAdvancedStage_warnsWhenNextStageHasNoStatuses() {
    // ---- arrange: case-type whose `review` stage declares NO statuses AND top-level statuses
    // are empty. This forces statusesFor("review") to return an empty list, triggering the
    // WKS-STATUS-001 warn branch.
    CaseTypeConfig caseType =
        CaseTypeConfig.builder()
            .id(CASE_TYPE_ID)
            .displayName("Stale Status Type")
            .version(1)
            .explicitTopLevelStatuses(false)
            // No top-level statuses; both stages are bare (no stage-scoped statuses either).
            .stages(
                List.of(
                    new StageDefinition("intake", "Intake", 0),
                    new StageDefinition("review", "Review", 1)))
            .build();
    // sanity — statusesFor must return empty for both stages.
    assertThat(caseType.statusesFor("review")).isEmpty();

    CaseRepository repo = mock(CaseRepository.class);
    CaseStatusUpdater statusUpdater = mock(CaseStatusUpdater.class);
    EventPublisher publisher = mock(EventPublisher.class);
    Clock clock = () -> NOW;
    CaseTypeReader reader = mock(CaseTypeReader.class);
    WksStageAdvancer advancer = mock(WksStageAdvancer.class);
    MappingRegistry mappingRegistry = mock(MappingRegistry.class);

    Case caseRow =
        new Case(
            CASE_ID,
            CASE_TYPE_ID,
            1,
            "drafting", // existing status that should remain stale
            UUID.randomUUID(),
            Map.of(),
            null,
            NOW,
            UUID.randomUUID(),
            NOW,
            0L);
    when(repo.findById(CASE_ID)).thenReturn(Optional.of(caseRow));
    when(reader.findVersion(CASE_TYPE_ID, 1)).thenReturn(Optional.of(caseType));

    CaseTypeRef caseTypeRef = new CaseTypeRef(CASE_TYPE_ID, "1");
    MappingDefinition mapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "x.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of("approve", new OutcomeMapping("intake -> review")))));
    when(mappingRegistry.resolve(any(CaseTypeRef.class), anyString()))
        .thenReturn(Optional.of(mapping));

    ExecutionSignalRouter router =
        new ExecutionSignalRouter(
            mappingRegistry, advancer, statusUpdater, repo, publisher, clock, reader);

    // Wire a logback list-appender on the router's logger.
    ch.qos.logback.classic.Logger routerLogger =
        (ch.qos.logback.classic.Logger)
            org.slf4j.LoggerFactory.getLogger(ExecutionSignalRouter.class);
    ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender =
        new ch.qos.logback.core.read.ListAppender<>();
    appender.start();
    routerLogger.addAppender(appender);

    try {
      // ---- act: dispatch an OUTCOME signal that resolves to "intake -> review".
      router.onSignal(
          new ExecutionSignal(
              ExecutionSignalKind.OUTCOME,
              "formOutcome",
              new CaseInstanceRef(CASE_ID, caseTypeRef),
              "intake-task",
              Map.of("outcome", "approve")));

      // ---- assert: warn was emitted with WKS-STATUS-001.
      assertThat(appender.list)
          .as("expected WKS-STATUS-001 warn")
          .anyMatch(
              e ->
                  e.getLevel() == ch.qos.logback.classic.Level.WARN
                      && e.getKeyValuePairs() != null
                      && e.getKeyValuePairs().stream()
                          .anyMatch(
                              kv ->
                                  "wksErrorCode".equals(kv.key)
                                      && "WKS-STATUS-001".equals(kv.value)));

      // statusUpdater must NOT have been called (no reset since next stage has no statuses).
      verify(statusUpdater, never()).updateStatus(any(UUID.class), anyString());
    } finally {
      routerLogger.detachAppender(appender);
    }
  }
}
