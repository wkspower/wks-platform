package com.wkspower.platform.engine.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wkspower.platform.domain.port.BackendSignal;
import com.wkspower.platform.domain.port.BackendSignalKind;
import com.wkspower.platform.engine.BpmnBackendAdapter;
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
 * the {@link BackendSignal} it forwards to the {@link BpmnBackendAdapter}.
 */
class CaseStatusListenerTest {

  private static final UUID CASE = UUID.randomUUID();
  private static final String CASE_TYPE_ID = "ct-x";
  private static final String CASE_TYPE_VERSION = "1";

  private final BpmnBackendAdapter adapter =
      new BpmnBackendAdapter() {
        @Override
        public void emit(BackendSignal signal) {
          captured = signal;
        }

        @Override
        public com.wkspower.platform.domain.port.BackendSignalSubscription onBackendSignal(
            com.wkspower.platform.domain.port.BackendSignalHandler handler) {
          return () -> {};
        }
      };
  private BackendSignal captured;
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
    assertThat(captured.kind()).isEqualTo(BackendSignalKind.END_EVENT);
    assertThat(captured.adapterName()).isEqualTo(BpmnBackendAdapter.ADAPTER_NAME);
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

    assertThat(captured.kind()).isEqualTo(BackendSignalKind.END_EVENT);
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

    assertThat(captured.kind()).isEqualTo(BackendSignalKind.USER_TASK_STATUS);
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

    assertThat(captured.kind()).isEqualTo(BackendSignalKind.USER_TASK_COMPLETE);
    assertThat(captured.source()).isEqualTo("draft");
    assertThat(captured.payload()).isEmpty();
  }

  @Test
  void noFallbackWhenUserTaskHasParallelActiveSiblings() {
    // Story 4.4a AC5 — the legacy fallback (peek getActiveActivityIds → first non-self) is
    // GONE. A userTask without explicit <camunda:property name="status"> simply emits
    // USER_TASK_COMPLETE; the router is the deciding party (no in-listener mutation, no engine
    // peek). Parallel-gateway non-determinism is rejected at deploy time by BpmnValidator
    // (WKS-CFG-024) on stage-scoped CaseTypes.
    UserTask userTask = mock(UserTask.class);
    when(userTask.getExtensionElements()).thenReturn(null);

    DelegateExecution exec = baseExec("draft");
    when(exec.getBpmnModelElementInstance()).thenReturn(userTask);

    listener.notify(exec);

    assertThat(captured.kind()).isEqualTo(BackendSignalKind.USER_TASK_COMPLETE);
    // The listener never queries getProcessEngineServices — verify by ensuring no interaction
    // (mock returns null which would NPE inside the legacy resolveNewStatus path).
    verify(exec, never()).getProcessEngineServices();
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
