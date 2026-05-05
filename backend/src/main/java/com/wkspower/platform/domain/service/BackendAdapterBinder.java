package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.port.BackendAdapter;
import com.wkspower.platform.domain.port.CaseTypeRef;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Resolves a {@link BackendAdapter} for a {@link CaseTypeRef} via DI. Architecture Decision 22 —
 * Mapping Layer is the only seam between WKS primitives and any backend.
 *
 * <p>Resolution rule (Story 4.1 AC4): if any registered adapter has called {@link
 * #register(CaseTypeRef, BackendAdapter)}, return the most-recently-registered adapter; else return
 * the singleton {@link NullAdapter}.
 *
 * <p>This binder is the ONLY way domain code obtains a {@link BackendAdapter} instance —
 * {@code @Autowired BackendAdapter} directly anywhere in {@code domain/} would defeat the rule.
 * Story 4.1 has zero call sites for this binder; Story 4.4 / 4.5 wire it up to call sites.
 *
 * <p>{@link #register(CaseTypeRef, BackendAdapter)} and {@link #unregister(CaseTypeRef)} are
 * package-private — only an adapter's own {@code attach(...)} / {@code detach(...)} implementation
 * (which lives in {@code domain/service} or in an engine package that depends on this one) reaches
 * into them. Domain services only call {@link #resolve(CaseTypeRef)}.
 *
 * <p>Pure-Java by design — no Spring annotations on the class itself. Wired into the Spring context
 * via {@code infrastructure.config.BackendAdapterConfig} to honour the standing rule that {@code
 * domain/} stays framework-free (NFR36).
 */
public class BackendAdapterBinder {

  private final ConcurrentMap<CaseTypeRef, BackendAdapter> registry = new ConcurrentHashMap<>();
  private final NullAdapter nullAdapter;

  public BackendAdapterBinder(NullAdapter nullAdapter) {
    this.nullAdapter = Objects.requireNonNull(nullAdapter, "nullAdapter");
  }

  /**
   * Resolve the {@link BackendAdapter} for {@code caseType}. Returns the registered adapter or
   * {@link NullAdapter} if none has been attached.
   */
  public BackendAdapter resolve(CaseTypeRef caseType) {
    Objects.requireNonNull(caseType, "caseType");
    BackendAdapter resolved = registry.get(caseType);
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
  public void register(CaseTypeRef caseType, BackendAdapter adapter) {
    Objects.requireNonNull(caseType, "caseType");
    Objects.requireNonNull(adapter, "adapter");
    registry.put(caseType, adapter);
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
}
