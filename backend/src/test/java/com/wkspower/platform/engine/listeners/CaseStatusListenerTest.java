package com.wkspower.platform.engine.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wkspower.platform.domain.event.CaseStatusChanged;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.cibseven.bpm.engine.ProcessEngineServices;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.delegate.DelegateExecution;
import org.cibseven.bpm.model.bpmn.instance.EndEvent;
import org.cibseven.bpm.model.bpmn.instance.UserTask;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CaseStatusListenerTest {

  private static final Instant NOW = Instant.parse("2026-04-26T10:00:00Z");
  private static final UUID CASE = UUID.randomUUID();

  private final CaseStatusUpdater updater = mock(CaseStatusUpdater.class);
  private final EventPublisher publisher = mock(EventPublisher.class);
  private final Clock clock = () -> NOW;
  private final CaseStatusListener listener = new CaseStatusListener(updater, publisher, clock);

  @Test
  void noOpWhenCaseIdVariableMissing() {
    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn(null);

    listener.notify(exec);

    verify(updater, never()).updateStatus(any(), anyString());
    verify(publisher, never()).publish(any());
  }

  @Test
  void noOpWhenCaseIdNotUuid() {
    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn("not-a-uuid");

    listener.notify(exec);

    verify(updater, never()).updateStatus(any(), anyString());
  }

  @Test
  void endEventUsesElementIdWhenNoStatusProperty() {
    EndEvent endEvent = mock(EndEvent.class);
    when(endEvent.getExtensionElements()).thenReturn(null);

    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn(CASE.toString());
    when(exec.getCurrentActivityId()).thenReturn("approved");
    when(exec.getProcessInstanceId()).thenReturn("pi-1");
    when(exec.getBpmnModelElementInstance()).thenReturn(endEvent);
    when(updater.updateStatus(eq(CASE), eq("approved"))).thenReturn(Optional.of("review"));

    listener.notify(exec);

    verify(updater).updateStatus(CASE, "approved");
    ArgumentCaptor<Object> evt = ArgumentCaptor.forClass(Object.class);
    verify(publisher).publish(evt.capture());
    CaseStatusChanged published = (CaseStatusChanged) evt.getValue();
    assertThat(published.caseId()).isEqualTo(CASE);
    assertThat(published.oldStatus()).isEqualTo("review");
    assertThat(published.newStatus()).isEqualTo("approved");
    assertThat(published.processInstanceId()).isEqualTo("pi-1");
    assertThat(published.timestamp()).isEqualTo(NOW);
  }

  @Test
  void userTaskEndPicksNextActiveActivity() {
    UserTask userTask = mock(UserTask.class);

    RuntimeService runtimeService = mock(RuntimeService.class);
    when(runtimeService.getActiveActivityIds("pi-1")).thenReturn(List.of("draft", "review"));

    ProcessEngineServices services = mock(ProcessEngineServices.class);
    when(services.getRuntimeService()).thenReturn(runtimeService);

    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn(CASE.toString());
    when(exec.getCurrentActivityId()).thenReturn("draft");
    when(exec.getProcessInstanceId()).thenReturn("pi-1");
    when(exec.getBpmnModelElementInstance()).thenReturn(userTask);
    when(exec.getProcessEngineServices()).thenReturn(services);
    when(updater.updateStatus(CASE, "review")).thenReturn(Optional.of("draft"));

    listener.notify(exec);

    verify(updater).updateStatus(CASE, "review");
    verify(publisher).publish(any(CaseStatusChanged.class));
  }

  @Test
  void noEventPublishedWhenCaseRowMissing() {
    // Story 2.4 review — listener fires for a process whose case row never persisted (engine-first
    // create with DB write that didn't commit). Should skip event publication so subscribers don't
    // see CaseStatusChanged for a non-existent case.
    EndEvent endEvent = mock(EndEvent.class);
    when(endEvent.getExtensionElements()).thenReturn(null);

    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn(CASE.toString());
    when(exec.getCurrentActivityId()).thenReturn("approved");
    when(exec.getProcessInstanceId()).thenReturn("pi-1");
    when(exec.getBpmnModelElementInstance()).thenReturn(endEvent);
    when(updater.updateStatus(CASE, "approved")).thenReturn(Optional.empty());

    listener.notify(exec);

    verify(updater).updateStatus(CASE, "approved");
    verify(publisher, never()).publish(any());
  }

  @Test
  void userTaskEndWithNoOtherActiveActivityIsNoOp() {
    UserTask userTask = mock(UserTask.class);

    RuntimeService runtimeService = mock(RuntimeService.class);
    when(runtimeService.getActiveActivityIds("pi-1")).thenReturn(List.of("draft"));

    ProcessEngineServices services = mock(ProcessEngineServices.class);
    when(services.getRuntimeService()).thenReturn(runtimeService);

    DelegateExecution exec = mock(DelegateExecution.class);
    when(exec.getVariable("caseId")).thenReturn(CASE.toString());
    when(exec.getCurrentActivityId()).thenReturn("draft");
    when(exec.getProcessInstanceId()).thenReturn("pi-1");
    when(exec.getBpmnModelElementInstance()).thenReturn(userTask);
    when(exec.getProcessEngineServices()).thenReturn(services);

    listener.notify(exec);

    verify(updater, never()).updateStatus(any(), anyString());
  }
}
