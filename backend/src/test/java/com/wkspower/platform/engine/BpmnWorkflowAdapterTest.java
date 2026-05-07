package com.wkspower.platform.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.domain.port.ExecutionSignalSubscription;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 4.4a — direct unit coverage of {@link BpmnWorkflowAdapter}'s emit/subscribe contract. The
 * adapter is the BPMN-side {@code WorkflowAdapter} implementation; AC1 / AC2 require it to forward
 * every {@link ExecutionSignal} to the registered (single) handler.
 */
class BpmnWorkflowAdapterTest {

  private final BpmnWorkflowAdapter adapter = new BpmnWorkflowAdapter();

  @Test
  void emitForwardsSignalToRegisteredHandler() {
    List<ExecutionSignal> received = new ArrayList<>();
    adapter.onExecutionSignal(received::add);

    ExecutionSignal signal = sampleSignal(ExecutionSignalKind.STAGE_TRANSITION);
    adapter.emit(signal);

    assertThat(received).containsExactly(signal);
  }

  @Test
  void singleSubscriberInvariantRejectsSecondHandler() {
    adapter.onExecutionSignal(s -> {});
    assertThatThrownBy(() -> adapter.onExecutionSignal(s -> {}))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("single-subscriber");
  }

  @Test
  void closingSubscriptionAllowsReSubscribeForTestRotation() {
    ExecutionSignalSubscription first = adapter.onExecutionSignal(s -> {});
    first.close();
    // After close the slot is free — re-registering is allowed (test cleanup pattern).
    ExecutionSignalSubscription second = adapter.onExecutionSignal(s -> {});
    second.close();
  }

  @Test
  void emitWithoutHandlerIsSilentDrop() {
    // No handler registered — emit should not throw.
    adapter.emit(sampleSignal(ExecutionSignalKind.TASK_COMPLETED));
  }

  @Test
  void startReturnsSyntheticBpmnPendingId() {
    UUID caseId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    CaseInstanceRef ref = new CaseInstanceRef(caseId, new CaseTypeRef("ct-x", "1"));
    assertThat(adapter.start(ref)).isEqualTo("bpmn:pending:" + caseId);
  }

  private static ExecutionSignal sampleSignal(ExecutionSignalKind kind) {
    CaseInstanceRef ref = new CaseInstanceRef(UUID.randomUUID(), new CaseTypeRef("ct-x", "1"));
    return new ExecutionSignal(kind, BpmnWorkflowAdapter.ADAPTER_NAME, ref, "elt-1", Map.of());
  }
}
