package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.BackendAdapter;
import com.wkspower.platform.domain.port.BackendSignalHandler;
import com.wkspower.platform.domain.port.BackendSignalSubscription;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseTypeRef;
import java.util.Objects;

/**
 * No-op {@link BackendAdapter} bound by default for any CaseType with zero attachments.
 * Architecture Decision 22 — a CaseType with no attached process Just Works because the binder
 * resolves to this adapter.
 *
 * <p>Story 4.1 AC3:
 *
 * <ul>
 *   <li>{@code attach} / {@code detach} — no-ops, return immediately.
 *   <li>{@code onBackendSignal} — registers the handler but NEVER invokes it (NullAdapter emits no
 *       signals, ever); returns a closeable that is itself a no-op on close.
 *   <li>{@code start} — returns a deterministic synthetic id of the form {@code
 *       "null:<caseInstance.id()>"} so audit / observability can distinguish a null start from a
 *       real one without special-casing.
 *   <li>{@code cancel} — no-op.
 * </ul>
 *
 * <p>Zero external dependencies — no engine imports, no repository, no infrastructure types, not
 * even Spring. Wired into the Spring context via {@code infrastructure.config.BackendAdapterConfig}
 * to honour the project-wide rule that {@code domain/} stays framework-free (NFR36, enforced by
 * {@link com.wkspower.platform.architecture.ArchitectureTest}). The story spec said
 * {@code @Component}, but the standing architectural rule wins — see Dev Notes.
 *
 * <p>Isolation is also enforced by ArchUnit ({@code BackendAdapterPortIsolationTest}).
 */
public final class NullAdapter implements BackendAdapter {

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
  public BackendSignalSubscription onBackendSignal(BackendSignalHandler handler) {
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
