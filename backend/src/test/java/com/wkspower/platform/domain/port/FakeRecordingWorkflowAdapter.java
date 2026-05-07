package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.service.WorkflowAdapterBinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test-only minimal in-memory {@link WorkflowAdapter}. Records every call and lets tests push
 * synthetic {@link ExecutionSignal}s through to subscribed handlers so compliance tests 5 and 6 are
 * non-trivial.
 *
 * <p>This fake is unrelated to Story 4.9's productionizable in-memory state-machine adapter — it
 * lives in {@code src/test/java}, is not a Spring component, and has a deliberately trivial scope.
 */
public final class FakeRecordingWorkflowAdapter implements WorkflowAdapter {

  public static final String ADAPTER_NAME = "fake-recording";

  private final WorkflowAdapterBinder binder;
  private final List<ExecutionSignalHandler> handlers = new CopyOnWriteArrayList<>();
  private final List<AttachCall> attachCalls = new ArrayList<>();
  private final List<CaseTypeRef> detachCalls = new ArrayList<>();
  private final List<CaseInstanceRef> cancelCalls = new ArrayList<>();
  private final Map<java.util.UUID, String> startedInstances = new HashMap<>();

  public FakeRecordingWorkflowAdapter(WorkflowAdapterBinder binder) {
    this.binder = Objects.requireNonNull(binder, "binder");
  }

  @Override
  public synchronized void attach(CaseTypeRef caseType, AttachmentScope scope) {
    Objects.requireNonNull(caseType, "caseType");
    Objects.requireNonNull(scope, "scope");
    AttachCall call = new AttachCall(caseType, scope);
    if (!attachCalls.contains(call)) {
      attachCalls.add(call);
    }
    binder.register(caseType, this);
  }

  @Override
  public synchronized void detach(CaseTypeRef caseType) {
    Objects.requireNonNull(caseType, "caseType");
    detachCalls.add(caseType);
    binder.unregister(caseType);
  }

  @Override
  public ExecutionSignalSubscription onExecutionSignal(ExecutionSignalHandler handler) {
    Objects.requireNonNull(handler, "handler");
    handlers.add(handler);
    return () -> handlers.remove(handler);
  }

  @Override
  public synchronized String start(CaseInstanceRef caseInstance) {
    Objects.requireNonNull(caseInstance, "caseInstance");
    return startedInstances.computeIfAbsent(caseInstance.id(), id -> "fake:" + id);
  }

  @Override
  public synchronized void cancel(CaseInstanceRef caseInstance) {
    Objects.requireNonNull(caseInstance, "caseInstance");
    cancelCalls.add(caseInstance);
    startedInstances.remove(caseInstance.id());
  }

  // --- test-side push API ---

  /** Push a signal to every currently-subscribed handler, preserving subscription order. */
  public void emit(ExecutionSignal signal) {
    Objects.requireNonNull(signal, "signal");
    for (ExecutionSignalHandler h : handlers) {
      h.onSignal(signal);
    }
  }

  public List<AttachCall> attachCalls() {
    return List.copyOf(attachCalls);
  }

  public List<CaseTypeRef> detachCalls() {
    return List.copyOf(detachCalls);
  }

  public List<CaseInstanceRef> cancelCalls() {
    return List.copyOf(cancelCalls);
  }

  public record AttachCall(CaseTypeRef caseType, AttachmentScope scope) {}
}
