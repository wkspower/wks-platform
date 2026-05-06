package com.wkspower.platform.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.port.BackendSignal;
import com.wkspower.platform.domain.port.BackendSignalKind;
import com.wkspower.platform.domain.port.BackendSignalSubscription;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseTypeRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 4.4a — direct unit coverage of {@link BpmnBackendAdapter}'s emit/subscribe contract. The
 * adapter is the BPMN-side {@code BackendAdapter} implementation; AC1 / AC2 require it to forward
 * every {@link BackendSignal} to the registered (single) handler.
 */
class BpmnBackendAdapterTest {

  private final BpmnBackendAdapter adapter = new BpmnBackendAdapter();

  @Test
  void emitForwardsSignalToRegisteredHandler() {
    List<BackendSignal> received = new ArrayList<>();
    adapter.onBackendSignal(received::add);

    BackendSignal signal = sampleSignal(BackendSignalKind.END_EVENT);
    adapter.emit(signal);

    assertThat(received).containsExactly(signal);
  }

  @Test
  void singleSubscriberInvariantRejectsSecondHandler() {
    adapter.onBackendSignal(s -> {});
    assertThatThrownBy(() -> adapter.onBackendSignal(s -> {}))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("single-subscriber");
  }

  @Test
  void closingSubscriptionAllowsReSubscribeForTestRotation() {
    BackendSignalSubscription first = adapter.onBackendSignal(s -> {});
    first.close();
    // After close the slot is free — re-registering is allowed (test cleanup pattern).
    BackendSignalSubscription second = adapter.onBackendSignal(s -> {});
    second.close();
  }

  @Test
  void emitWithoutHandlerIsSilentDrop() {
    // No handler registered — emit should not throw.
    adapter.emit(sampleSignal(BackendSignalKind.USER_TASK_COMPLETE));
  }

  @Test
  void startReturnsSyntheticBpmnPendingId() {
    UUID caseId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    CaseInstanceRef ref = new CaseInstanceRef(caseId, new CaseTypeRef("ct-x", "1"));
    assertThat(adapter.start(ref)).isEqualTo("bpmn:pending:" + caseId);
  }

  private static BackendSignal sampleSignal(BackendSignalKind kind) {
    CaseInstanceRef ref = new CaseInstanceRef(UUID.randomUUID(), new CaseTypeRef("ct-x", "1"));
    return new BackendSignal(kind, BpmnBackendAdapter.ADAPTER_NAME, ref, "elt-1", Map.of());
  }
}
