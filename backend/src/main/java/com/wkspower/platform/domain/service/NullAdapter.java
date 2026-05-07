package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.port.ExecutionSignalSubscription;
import com.wkspower.platform.domain.port.WorkflowAdapter;
import java.util.Objects;

/**
 * No-op {@link WorkflowAdapter} bound by default for any CaseType with zero attachments.
 * Architecture Decision 22 — a CaseType with no attached process Just Works because the binder
 * resolves to this adapter.
 *
 * <p>Story 4.1 AC3:
 *
 * <ul>
 *   <li>{@code attach} / {@code detach} — no-ops, return immediately.
 *   <li>{@code onExecutionSignal} — registers the handler but NEVER invokes it (NullAdapter emits
 *       no signals, ever); returns a closeable that is itself a no-op on close.
 *   <li>{@code start} — returns a deterministic synthetic id of the form {@code
 *       "null:<caseInstance.id()>"} so audit / observability can distinguish a null start from a
 *       real one without special-casing.
 *   <li>{@code cancel} — no-op.
 * </ul>
 *
 * <p>Zero external dependencies — no engine imports, no repository, no infrastructure types, not
 * even Spring. Wired into the Spring context via {@code
 * infrastructure.config.WorkflowAdapterConfig} to honour the project-wide rule that {@code domain/}
 * stays framework-free (NFR36, enforced by {@link
 * com.wkspower.platform.architecture.ArchitectureTest}). The story spec said {@code @Component},
 * but the standing architectural rule wins — see Dev Notes.
 *
 * <p>Isolation is also enforced by ArchUnit ({@code WorkflowAdapterPortIsolationTest}).
 */
public final class NullAdapter implements WorkflowAdapter {

  /** Adapter identity reported on any signal — kept as a constant so router tests can match. */
  public static final String ADAPTER_NAME = "null";

  @Override
  public void attach(CaseTypeRef caseType, AttachmentScope scope) {
    // no-op
  }

  @Override
  public void detach(CaseTypeRef caseType) {
    // no-op
  }

  @Override
  public ExecutionSignalSubscription onExecutionSignal(ExecutionSignalHandler handler) {
    Objects.requireNonNull(handler, "handler");
    // Handler is intentionally never invoked — NullAdapter emits no signals.
    return () -> {
      /* no-op close */
    };
  }

  @Override
  public String start(CaseInstanceRef caseInstance) {
    Objects.requireNonNull(caseInstance, "caseInstance");
    return "null:" + caseInstance.id();
  }

  @Override
  public void cancel(CaseInstanceRef caseInstance) {
    // no-op
  }
}
