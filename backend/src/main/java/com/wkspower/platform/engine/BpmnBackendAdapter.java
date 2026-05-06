package com.wkspower.platform.engine;

import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.BackendAdapter;
import com.wkspower.platform.domain.port.BackendSignal;
import com.wkspower.platform.domain.port.BackendSignalHandler;
import com.wkspower.platform.domain.port.BackendSignalSubscription;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseTypeRef;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Story 4.4a — BPMN execution backend wrapped as a {@link BackendAdapter}. Architecture Decision 22
 * / §786: the existing CIB seven engine ({@link
 * com.wkspower.platform.engine.CibSevenWorkflowEngine}) becomes the first production adapter; the
 * Mapping Layer is the single seam between WKS primitives and any execution backend.
 *
 * <p>This adapter wraps {@link WorkflowEngine#startProcessInstance(String, Map)} for {@link #start}
 * and routes BPMN execution-listener events through {@link
 * BackendSignalHandler#onSignal(BackendSignal)} via the package-visible {@link
 * #emit(BackendSignal)} method invoked by {@link
 * com.wkspower.platform.engine.listeners.CaseStatusListener}.
 *
 * <p><b>AC1 — direct-mutation removed:</b> the listener no longer writes case state directly. It
 * builds a {@link BackendSignal} and calls {@link #emit(BackendSignal)}; the registered handler (in
 * production: Story 4.3's {@code BackendSignalRouter}) consults the mapping layer and applies the
 * configured stage / status transition. Manual transitions through {@code CaseService.transition}
 * continue to call the legacy direct path until Story 4.4b reroutes them (documented in PR body as
 * the post-4.4a dual-state).
 */
@Component
public class BpmnBackendAdapter implements BackendAdapter {

  /** Adapter name reported on every signal — FR8 source attribution. */
  public static final String ADAPTER_NAME = "bpmn";

  private static final Logger log = LoggerFactory.getLogger(BpmnBackendAdapter.class);

  private final AtomicReference<BackendSignalHandler> handlerRef = new AtomicReference<>();

  public BpmnBackendAdapter() {
    // Phase-0 — the adapter does not own the WorkflowEngine reference. CaseService.create still
    // calls WorkflowEngine.startProcessInstance directly (4.4b reroutes through start(...)). Not
    // injecting WorkflowEngine here breaks an otherwise-circular Spring bean graph:
    //   cibSevenWorkflowEngine -> processEngineConfigurationImpl -> caseStatusEnginePlugin
    //     -> caseStatusListener -> bpmnBackendAdapter -> cibSevenWorkflowEngine.
  }

  @Override
  public void attach(CaseTypeRef caseType, AttachmentScope scope) {
    Objects.requireNonNull(caseType, "caseType");
    Objects.requireNonNull(scope, "scope");
    // Idempotent on (caseType, scope). The actual BPMN deployment happens through
    // ConfigService.deploy → WorkflowEngine.deploy; attach() here is a registration ping —
    // BackendAdapterBinder.register is the single source of truth for routing resolution.
    log.debug("BpmnBackendAdapter attached for caseType={} scope={}", caseType, scope);
  }

  @Override
  public void detach(CaseTypeRef caseType) {
    Objects.requireNonNull(caseType, "caseType");
    log.debug("BpmnBackendAdapter detached for caseType={}", caseType);
  }

  @Override
  public BackendSignalSubscription onBackendSignal(BackendSignalHandler handler) {
    Objects.requireNonNull(handler, "handler");
    // Single-subscriber invariant — Story 4.3 AC1 / AC6 enforced by ArchUnit
    // (BackendAdapterPortIsolationTest). Idempotent for the SAME handler instance so that the
    // eager boot-time registration (BpmnBackendAdapterRegistrar ApplicationReadyEvent) and the
    // BackendAdapterBinder.computeIfAbsent path can both succeed without racing.
    handlerRef.compareAndSet(null, handler);
    if (handlerRef.get() != handler) {
      throw new IllegalStateException(
          "BpmnBackendAdapter already has a different registered handler — single-subscriber"
              + " invariant (Story 4.3 AC6)");
    }
    return () -> handlerRef.compareAndSet(handler, null);
  }

  @Override
  public String start(CaseInstanceRef caseInstance) {
    Objects.requireNonNull(caseInstance, "caseInstance");
    // Phase-0 — BackendAdapter.start is exposed but CaseService.create still drives engine start
    // directly via WorkflowEngine. Story 4.4b reroutes CaseService.create through this method.
    // Until then, return the synthetic id so audit / diagnostics distinguish a routed start.
    return "bpmn:pending:" + caseInstance.id();
  }

  @Override
  public void cancel(CaseInstanceRef caseInstance) {
    Objects.requireNonNull(caseInstance, "caseInstance");
    log.debug("BpmnBackendAdapter cancel (no-op pre-4.4b) for case={}", caseInstance.id());
  }

  /**
   * Engine-callback dispatch surface. Invoked by {@link
   * com.wkspower.platform.engine.listeners.CaseStatusListener} after extracting the {@link
   * BackendSignal} from a BPMN execution event. The signal is forwarded to the registered handler
   * ({@code BackendSignalRouter} in production wiring); when no handler is registered yet the call
   * is silently dropped at warn-log level — should not happen post-boot.
   */
  public void emit(BackendSignal signal) {
    Objects.requireNonNull(signal, "signal");
    BackendSignalHandler handler = handlerRef.get();
    if (handler == null) {
      log.warn(
          "BpmnBackendAdapter.emit invoked before handler registration — signal dropped: kind={}"
              + " caseId={} source={}",
          signal.kind(),
          signal.caseInstance().id(),
          signal.source());
      return;
    }
    handler.onSignal(signal);
  }
}
