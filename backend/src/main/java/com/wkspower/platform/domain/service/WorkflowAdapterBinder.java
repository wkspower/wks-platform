package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.port.WorkflowAdapter;
import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.port.ExecutionSignalSubscription;
import com.wkspower.platform.domain.port.CaseTypeRef;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Resolves a {@link WorkflowAdapter} for a {@link CaseTypeRef} via DI. Architecture Decision 22 —
 * Mapping Layer is the only seam between WKS primitives and any backend.
 *
 * <p>Resolution rule (Story 4.1 AC4): if any registered adapter has called {@link
 * #register(CaseTypeRef, WorkflowAdapter)}, return the most-recently-registered adapter; else return
 * the singleton {@link NullAdapter}.
 *
 * <p>This binder is the ONLY way domain code obtains a {@link WorkflowAdapter} instance —
 * {@code @Autowired WorkflowAdapter} directly anywhere in {@code domain/} would defeat the rule.
 * Story 4.1 has zero call sites for this binder; Story 4.4 / 4.5 wire it up to call sites.
 *
 * <p>{@link #register(CaseTypeRef, WorkflowAdapter)} and {@link #unregister(CaseTypeRef)} are
 * package-private — only an adapter's own {@code attach(...)} / {@code detach(...)} implementation
 * (which lives in {@code domain/service} or in an engine package that depends on this one) reaches
 * into them. Domain services only call {@link #resolve(CaseTypeRef)}.
 *
 * <p>Pure-Java by design — no Spring annotations on the class itself. Wired into the Spring context
 * via {@code infrastructure.config.WorkflowAdapterConfig} to honour the standing rule that {@code
 * domain/} stays framework-free (NFR36).
 */
public class WorkflowAdapterBinder {

  private final ConcurrentMap<CaseTypeRef, WorkflowAdapter> registry = new ConcurrentHashMap<>();
  private final ConcurrentMap<WorkflowAdapter, ExecutionSignalSubscription> subscriptions =
      new ConcurrentHashMap<>();
  private final NullAdapter nullAdapter;
  private final ExecutionSignalHandler handler;

  public WorkflowAdapterBinder(NullAdapter nullAdapter) {
    this(nullAdapter, null);
  }

  /**
   * Story 4.4a constructor — wired with the production {@link ExecutionSignalHandler} (the {@code
   * ExecutionSignalRouter}). When provided, every {@link #register(CaseTypeRef, WorkflowAdapter)} call
   * ensures the adapter is subscribed to the router exactly once (single-subscriber invariant;
   * ArchUnit restricts {@link WorkflowAdapter#onExecutionSignal} callers to router + binder).
   */
  public WorkflowAdapterBinder(NullAdapter nullAdapter, ExecutionSignalHandler handler) {
    this.nullAdapter = Objects.requireNonNull(nullAdapter, "nullAdapter");
    this.handler = handler;
  }

  /**
   * Resolve the {@link WorkflowAdapter} for {@code caseType}. Returns the registered adapter or
   * {@link NullAdapter} if none has been attached.
   */
  public WorkflowAdapter resolve(CaseTypeRef caseType) {
    Objects.requireNonNull(caseType, "caseType");
    WorkflowAdapter resolved = registry.get(caseType);
    return resolved == null ? nullAdapter : resolved;
  }

  /**
   * Register {@code adapter} as the bound adapter for {@code caseType}. Idempotent on the same
   * {@code (caseType, adapter)} pair; replaces any prior registration with the most-recent adapter
   * (resolution rule, AC4).
   *
   * <p>Adapter-only contract — only an adapter's {@code attach(...)} call site should invoke this.
   * Surface is {@code public} (not package-private) so adapters living outside {@code
   * domain/service/} (notably the Story 4.4 BPMN adapter in {@code engine/}) can self-register; the
   * contract is enforced by convention + code review.
   */
  public void register(CaseTypeRef caseType, WorkflowAdapter adapter) {
    Objects.requireNonNull(caseType, "caseType");
    Objects.requireNonNull(adapter, "adapter");
    registry.put(caseType, adapter);
    if (handler != null) {
      // Subscribe each distinct adapter exactly once. computeIfAbsent serialises the
      // onExecutionSignal call so the single-subscriber invariant (Story 4.3 AC6) holds even when
      // multiple CaseTypes share an adapter instance.
      subscriptions.computeIfAbsent(adapter, a -> a.onExecutionSignal(handler));
    }
  }

  /**
   * Drop registration for {@code caseType}. Subsequent {@link #resolve(CaseTypeRef)} calls return
   * {@link NullAdapter}. Idempotent on unknown / already-removed entries.
   *
   * <p>Adapter-only contract — only an adapter's {@code detach(...)} call site should invoke this.
   * Surface is {@code public} (see {@link #register} Javadoc) for the same cross-package reason.
   */
  public void unregister(CaseTypeRef caseType) {
    Objects.requireNonNull(caseType, "caseType");
    registry.remove(caseType);
  }

  /**
   * Story 4.5 AC4 — detach the adapter registered for {@code caseType} and delegate to the
   * adapter's own {@link WorkflowAdapter#detach(CaseTypeRef)} so it can mark the scope as locally
   * detached (e.g. {@code BpmnWorkflowAdapter} adds the {@code caseTypeId} to its {@code
   * detachedCaseTypeIds} set so {@code emit()} drops signals for that scope).
   *
   * <p>{@link MappingRegistry} is NOT touched — in-flight cases must retain their frozen-version
   * mapping (AC4 invariant: "MappingRegistry MUST NOT remove old (caseTypeId, version) entries on
   * detach").
   *
   * <p>Idempotent — calling detach on an already-detached or unknown {@code caseType} is safe.
   */
  public void detach(CaseTypeRef caseType) {
    Objects.requireNonNull(caseType, "caseType");
    WorkflowAdapter adapter = registry.remove(caseType);
    if (adapter != null) {
      adapter.detach(caseType);
    }
  }
}
