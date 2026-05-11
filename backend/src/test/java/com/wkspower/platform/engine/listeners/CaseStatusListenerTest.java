package com.wkspower.platform.engine.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.engine.BpmnWorkflowAdapter;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.model.bpmn.instance.EndEvent;
import org.cibseven.bpm.model.bpmn.instance.ExtensionElements;
import org.cibseven.bpm.model.bpmn.instance.UserTask;
import org.cibseven.bpm.model.bpmn.instance.cibseven.CamundaProperties;
import org.cibseven.bpm.model.bpmn.instance.cibseven.CamundaProperty;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Story 4.4a — listener-side coverage. The listener no longer mutates state; every assertion pins
 * the {@link ExecutionSignal} it forwards to the {@link BpmnWorkflowAdapter}.
 */
class CaseStatusListenerTest {

  private static final UUID CASE = UUID.randomUUID();
  private static final String CASE_TYPE_ID = "ct-x";
  private static final String CASE_TYPE_VERSION = "1";

  private final BpmnWorkflowAdapter adapter =
      new BpmnWorkflowAdapter() {
        @Override
        public void emit(ExecutionSignal signal) {
          captured = signal;
        }

        @Override
        public com.wkspower.platform.domain.port.ExecutionSignalSubscription onExecutionSignal(
            com.wkspower.platform.domain.port.ExecutionSignalHandler handler) {
          return () -> {};
        }
      };
  private ExecutionSignal captured;
  private final CaseStatusListener listener = new CaseStatusListener(adapter);

  @Test
  void noOpWhenCaseIdVariableMissing() {
    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn(null);

    listener.notify(exec);

    assertThat(captured).isNull();
  }

  @Test
  void noOpWhenCaseTypeVariablesMissing() {
    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn(CASE.toString());
    when(exec.getVariable("caseTypeId")).thenReturn(null);

    listener.notify(exec);

    assertThat(captured).isNull();
  }

  @Test
  void noOpWhenCaseIdNotUuid() {
    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn("not-a-uuid");

    listener.notify(exec);

    assertThat(captured).isNull();
  }

  @Test
  void endEventWithoutPropertyEmitsEndEventSignalWithElementId() {
    EndEvent endEvent = mock(EndEvent.class);
    when(endEvent.getExtensionElements()).thenReturn(null);

    DelegateExecution exec = baseExec("approved");
    when(exec.getBpmnModelElementInstance()).thenReturn(endEvent);

    listener.notify(exec);

    assertThat(captured).isNotNull();
    assertThat(captured.kind()).isEqualTo(ExecutionSignalKind.STAGE_TRANSITION);
    assertThat(captured.adapterName()).isEqualTo(BpmnWorkflowAdapter.ADAPTER_NAME);
    assertThat(captured.source()).isEqualTo("approved");
  }

  @Test
  void endEventWithStatusPropertyEmitsValueAsSourceAndPayload() {
    EndEvent endEvent = mock(EndEvent.class);
    ExtensionElements ext = extensionWith("status", "resolved");
    when(endEvent.getExtensionElements()).thenReturn(ext);

    DelegateExecution exec = baseExec("end");
    when(exec.getBpmnModelElementInstance()).thenReturn(endEvent);

    listener.notify(exec);

    assertThat(captured.kind()).isEqualTo(ExecutionSignalKind.STAGE_TRANSITION);
    assertThat(captured.source()).isEqualTo("resolved");
    assertThat(captured.payload()).containsEntry("value", "resolved");
  }

  @Test
  void userTaskWithStatusPropertyEmitsUserTaskStatus() {
    UserTask userTask = mock(UserTask.class);
    ExtensionElements userTaskExt = extensionWith("status", "in-review");
    when(userTask.getExtensionElements()).thenReturn(userTaskExt);

    DelegateExecution exec = baseExec("review");
    when(exec.getBpmnModelElementInstance()).thenReturn(userTask);

    listener.notify(exec);

    assertThat(captured.kind()).isEqualTo(ExecutionSignalKind.TASK_STATUS_CHANGED);
    assertThat(captured.source()).isEqualTo("review");
    assertThat(captured.payload()).containsEntry("value", "in-review");
  }

  @Test
  void userTaskWithoutStatusPropertyEmitsUserTaskComplete() {
    UserTask userTask = mock(UserTask.class);
    when(userTask.getExtensionElements()).thenReturn(null);

    DelegateExecution exec = baseExec("draft");
    when(exec.getBpmnModelElementInstance()).thenReturn(userTask);

    listener.notify(exec);

    assertThat(captured.kind()).isEqualTo(ExecutionSignalKind.TASK_COMPLETED);
    assertThat(captured.source()).isEqualTo("draft");
    assertThat(captured.payload()).isEmpty();
  }

  @Test
  void noFallbackWhenUserTaskHasParallelActiveSiblings() {
    // Story 4.4a AC5 — the legacy fallback (peek getActiveActivityIds → first non-self) is
    // GONE. A userTask without explicit <camunda:property name="status"> simply emits
    // TASK_COMPLETED; the router is the deciding party (no in-listener mutation, no engine
    // peek). Parallel-gateway non-determinism is rejected at deploy time by BpmnValidator
    // (WKS-CFG-024) on stage-scoped CaseTypes.
    UserTask userTask = mock(UserTask.class);
    when(userTask.getExtensionElements()).thenReturn(null);

    DelegateExecution exec = baseExec("draft");
    when(exec.getBpmnModelElementInstance()).thenReturn(userTask);

    listener.notify(exec);

    assertThat(captured.kind()).isEqualTo(ExecutionSignalKind.TASK_COMPLETED);
    // The listener never queries getProcessEngineServices — verify by ensuring no interaction
    // (mock returns null which would NPE inside the legacy resolveNewStatus path).
    verify(exec, never()).getProcessEngineServices();
  }

  // Story 6.2 — WKS-ROUTE-001: outcome shadows status property.
  @Test
  void userTaskWithBothOutcomeAndStatus_emitsOutcomeAndLogsRoute001Warning() {
    // Wire a logback list-appender to capture the WARN with wksErrorCode=WKS-ROUTE-001.
    ch.qos.logback.classic.Logger listenerLogger =
        (ch.qos.logback.classic.Logger)
            org.slf4j.LoggerFactory.getLogger(CaseStatusListener.class);
    ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender =
        new ch.qos.logback.core.read.ListAppender<>();
    appender.start();
    listenerLogger.addAppender(appender);

    try {
      UserTask userTask = mock(UserTask.class);
      ExtensionElements userTaskExt = extensionWith("status", "in-review");
      when(userTask.getExtensionElements()).thenReturn(userTaskExt);

      DelegateExecution exec = baseExec("review-claim");
      when(exec.getVariable("outcome")).thenReturn("approve");
      when(exec.getBpmnModelElementInstance()).thenReturn(userTask);

      listener.notify(exec);

      // Outcome wins — OUTCOME signal emitted, NOT TASK_STATUS_CHANGED.
      assertThat(captured).isNotNull();
      assertThat(captured.kind()).isEqualTo(ExecutionSignalKind.OUTCOME);
      assertThat(captured.payload()).containsEntry("outcome", "approve");

      // Warn was logged with WKS-ROUTE-001.
      assertThat(appender.list)
          .as("expected WKS-ROUTE-001 warn for outcome-shadows-status")
          .anyMatch(
              e ->
                  e.getLevel() == ch.qos.logback.classic.Level.WARN
                      && e.getKeyValuePairs() != null
                      && e.getKeyValuePairs().stream()
                          .anyMatch(
                              kv ->
                                  "wksErrorCode".equals(kv.key) && "WKS-ROUTE-001".equals(kv.value)));
    } finally {
      listenerLogger.detachAppender(appender);
    }
  }

  @Test
  void userTaskWithOutcomeButNoStatusProperty_doesNotEmitRoute001() {
    ch.qos.logback.classic.Logger listenerLogger =
        (ch.qos.logback.classic.Logger)
            org.slf4j.LoggerFactory.getLogger(CaseStatusListener.class);
    ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender =
        new ch.qos.logback.core.read.ListAppender<>();
    appender.start();
    listenerLogger.addAppender(appender);

    try {
      UserTask userTask = mock(UserTask.class);
      when(userTask.getExtensionElements()).thenReturn(null);

      DelegateExecution exec = baseExec("review-claim");
      when(exec.getVariable("outcome")).thenReturn("approve");
      when(exec.getBpmnModelElementInstance()).thenReturn(userTask);

      listener.notify(exec);

      assertThat(captured.kind()).isEqualTo(ExecutionSignalKind.OUTCOME);
      assertThat(appender.list)
          .noneMatch(
              e ->
                  e.getKeyValuePairs() != null
                      && e.getKeyValuePairs().stream()
                          .anyMatch(
                              kv ->
                                  "wksErrorCode".equals(kv.key) && "WKS-ROUTE-001".equals(kv.value)));
    } finally {
      listenerLogger.detachAppender(appender);
    }
  }

  private DelegateExecution baseExec(String currentActivityId) {
    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn(CASE.toString());
    when(exec.getVariable("caseTypeId")).thenReturn(CASE_TYPE_ID);
    when(exec.getVariable("caseTypeVersion")).thenReturn(CASE_TYPE_VERSION);
    when(exec.getCurrentActivityId()).thenReturn(currentActivityId);
    when(exec.getProcessInstanceId()).thenReturn("pi-1");
    return exec;
  }

  private static ExtensionElements extensionWith(String name, String value) {
    ExtensionElements ext = mock(ExtensionElements.class);
    CamundaProperty p = mock(CamundaProperty.class);
    when(p.getCamundaName()).thenReturn(name);
    when(p.getCamundaValue()).thenReturn(value);
    CamundaProperties block = mock(CamundaProperties.class);
    when(block.getCamundaProperties()).thenReturn(java.util.List.of(p));
    Collection<CamundaProperties> blocks = java.util.List.of(block);
    // doReturn avoids the generic-bound resolution path on getChildElementsByType (its return
    // type is `<C extends ModelElementInstance> Collection<C>` which trips Mockito's stubber
    // chain when nested inside another mock-call expression).
    org.mockito.Mockito.doReturn(blocks).when(ext).getChildElementsByType(CamundaProperties.class);
    return ext;
  }

  // suppress unused-warnings for ArgumentCaptor / Collections imports if optimised away
  @SuppressWarnings("unused")
  private void unused() {
    ArgumentCaptor.forClass(Object.class);
    Collections.emptyList();
  }
}
