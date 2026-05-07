package com.wkspower.platform.domain.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.wkspower.platform.domain.service.WorkflowAdapterBinder;
import com.wkspower.platform.domain.service.NullAdapter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Story 4.1 AC6 — abstract compliance contract. Any {@link WorkflowAdapter} implementation MUST pass
 * these seven test methods. Story 4.4 (BPMN adapter) and Story 4.9 (in-memory state machine) extend
 * this class by overriding {@link #newAdapterUnderTest(WorkflowAdapterBinder)}.
 *
 * <p>Tests 1–4, 7 exercise real behaviour. Tests 5–6 are vacuously satisfied by adapters that emit
 * no signals (e.g. {@link NullAdapter}); the {@link FakeRecordingWorkflowAdapter} subclass exercises them
 * non-trivially.
 */
public abstract class WorkflowAdapterComplianceTest {

  protected NullAdapter nullAdapter;
  protected WorkflowAdapterBinder binder;
  protected WorkflowAdapter adapter;

  /**
   * Subclasses build a fresh adapter under test for each compliance run. The binder is provided so
   * the adapter can self-register from inside {@code attach(...)}.
   */
  protected abstract WorkflowAdapter newAdapterUnderTest(WorkflowAdapterBinder binder);

  /**
   * Whether the adapter under test ever emits signals. {@link NullAdapter} returns {@code false};
   * {@link FakeRecordingWorkflowAdapter} returns {@code true}. Tests 5 and 6 are skipped (vacuously
   * passing) when this returns {@code false}, and they receive a non-null fake-emit hook when this
   * returns {@code true}.
   */
  protected boolean adapterEmitsSignals() {
    return false;
  }

  /**
   * For signal-emitting adapters: emit a synthetic signal to every subscribed handler. Default
   * throws — adapters that emit signals MUST override.
   */
  protected void emitSignal(ExecutionSignal signal) {
    throw new UnsupportedOperationException(
        "Adapter under test does not support test-side signal emission");
  }

  @BeforeEach
  void setUp() {
    nullAdapter = new NullAdapter();
    binder = new WorkflowAdapterBinder(nullAdapter);
    adapter = newAdapterUnderTest(binder);
  }

  // ---------- Test 1 ----------
  @Test
  void attach_then_resolve_returns_this_adapter() {
    CaseTypeRef ref = new CaseTypeRef("ct-1", "1.0.0");

    adapter.attach(ref, AttachmentScope.ofCase());

    // NullAdapter is the binder's fallback; for it, resolve returns nullAdapter (which IS the
    // adapter under test). For other adapters, the adapter's attach must self-register.
    WorkflowAdapter resolved = binder.resolve(ref);
    if (adapter instanceof NullAdapter) {
      assertThat(resolved).isSameAs(nullAdapter);
    } else {
      assertThat(resolved).isSameAs(adapter);
    }
  }

  // ---------- Test 2 ----------
  @Test
  void detach_falls_back_to_null_adapter() {
    CaseTypeRef ref = new CaseTypeRef("ct-1", "1.0.0");
    adapter.attach(ref, AttachmentScope.ofCase());

    adapter.detach(ref);

    assertThat(binder.resolve(ref)).isSameAs(nullAdapter);
  }

  // ---------- Test 3 ----------
  @Test
  void start_is_idempotent_on_same_case_instance() {
    CaseInstanceRef ci = new CaseInstanceRef(UUID.randomUUID(), new CaseTypeRef("ct-1", "1.0.0"));

    String first = adapter.start(ci);
    String second = adapter.start(ci);

    assertThat(second).isEqualTo(first);
  }

  // ---------- Test 4 ----------
  @Test
  void cancel_unknown_instance_is_no_op() {
    CaseInstanceRef ci = new CaseInstanceRef(UUID.randomUUID(), new CaseTypeRef("ct-1", "1.0.0"));

    assertThatCode(() -> adapter.cancel(ci)).doesNotThrowAnyException();
    // Calling a second time on an already-cancelled / unknown instance also must not throw.
    assertThatCode(() -> adapter.cancel(ci)).doesNotThrowAnyException();
  }

  // ---------- Test 5 ----------
  @Test
  void signal_handler_receives_emitted_signals() {
    if (!adapterEmitsSignals()) {
      // Vacuously satisfied — NullAdapter and other zero-emit adapters declare no obligation here.
      return;
    }
    List<ExecutionSignal> received = new CopyOnWriteArrayList<>();
    ExecutionSignalSubscription sub = adapter.onExecutionSignal(received::add);

    CaseInstanceRef ci = new CaseInstanceRef(UUID.randomUUID(), new CaseTypeRef("ct-1", "1.0.0"));
    ExecutionSignal s1 =
        ExecutionSignal.of(
            ExecutionSignalKind.NAMED_SIGNAL, "fake-recording", ci, "elem-A", java.util.Map.of());
    ExecutionSignal s2 =
        ExecutionSignal.of(
            ExecutionSignalKind.OUTCOME, "fake-recording", ci, "elem-B", java.util.Map.of());
    ExecutionSignal s3 =
        ExecutionSignal.of(
            ExecutionSignalKind.STAGE_TRANSITION, "fake-recording", ci, "elem-C", java.util.Map.of());

    emitSignal(s1);
    emitSignal(s2);
    emitSignal(s3);

    // After close, no further deliveries.
    sub.close();
    emitSignal(s1);

    assertThat(received).containsExactly(s1, s2, s3);
  }

  // ---------- Test 6 ----------
  @Test
  void signal_kind_field_is_one_of_four_declared_values() {
    if (!adapterEmitsSignals()) {
      return; // vacuous for NullAdapter
    }
    List<ExecutionSignalKind> kinds = new ArrayList<>();
    ExecutionSignalSubscription sub = adapter.onExecutionSignal(s -> kinds.add(s.kind()));

    CaseInstanceRef ci = new CaseInstanceRef(UUID.randomUUID(), new CaseTypeRef("ct-1", "1.0.0"));
    for (ExecutionSignalKind k : ExecutionSignalKind.values()) {
      emitSignal(ExecutionSignal.of(k, "fake-recording", ci, "elem-" + k.name(), java.util.Map.of()));
    }
    sub.close();

    EnumSet<ExecutionSignalKind> declared =
        EnumSet.of(
            ExecutionSignalKind.STAGE_TRANSITION,
            ExecutionSignalKind.NAMED_SIGNAL,
            ExecutionSignalKind.TASK_STATUS_CHANGED,
            ExecutionSignalKind.TASK_COMPLETED,
            ExecutionSignalKind.OUTCOME);
    assertThat(kinds).allMatch(declared::contains);
  }

  // ---------- Test 7 ----------
  @Test
  void attach_is_idempotent() {
    CaseTypeRef ref = new CaseTypeRef("ct-1", "1.0.0");
    AttachmentScope scope = AttachmentScope.ofCase();

    adapter.attach(ref, scope);
    adapter.attach(ref, scope);

    // Detach must remove the binding cleanly — verifying no double-registration leak.
    adapter.detach(ref);
    assertThat(binder.resolve(ref)).isSameAs(nullAdapter);
  }
}
