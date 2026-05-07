package com.wkspower.platform.domain.port;

/**
 * Outbound port for any execution backend that WKS may attach to a CaseType — BPMN today (Story 4.4
 * implements via embedded CIB seven), state-machine / Temporal / AI orchestrator tomorrow.
 *
 * <p>Architecture §828 (Decision 22 — BPMN Attachment &amp; Mapping Layer). The Mapping Layer is
 * the only seam between WKS primitives and any backend; adding a new backend is a new
 * implementation of this port plus mapping rules.
 *
 * <p>The five-method contract is locked. Story 4.9 (in-memory state-machine) will prove it; Story
 * 4.4 (BPMN refactor) will ship the first production implementation.
 *
 * <p>Implementations MUST translate adapter-side failures into {@link
 * com.wkspower.platform.domain.exception.WksWorkflowEngineException} per the existing engine port
 * contract.
 */
public interface WorkflowAdapter {

  /**
   * Register the adapter for a CaseType at the given scope (case or stage — see {@link
   * AttachmentScope}). Architecture §786 / Decision 22.
   *
   * <p>Idempotent on {@code (caseType, scope)} — repeated calls with the same args MUST NOT throw
   * and MUST NOT register twice (compliance test 7).
   */
  void attach(CaseTypeRef caseType, AttachmentScope scope);

  /**
   * Drop the adapter's registration for {@code caseType}. In-flight cases bound to the prior
   * mapping continue under their pinned CaseTypeVersion (Story 4.5 surface; this port only declares
   * the API).
   */
  void detach(CaseTypeRef caseType);

  /**
   * Register a handler that receives {@link ExecutionSignal}s emitted by the adapter. Returns an
   * {@link AutoCloseable} subscription handle — closing it MUST stop further deliveries to the
   * handler.
   *
   * <p>Per-instance ordering is preserved (compliance test 5). Cross-instance ordering is
   * adapter-defined.
   */
  ExecutionSignalSubscription onExecutionSignal(ExecutionSignalHandler handler);

  /**
   * Start backend execution for {@code caseInstance}. Returns an opaque adapter-specific instance
   * id (e.g. CIB process instance id) — callers MUST treat the value as identity only.
   *
   * <p>Idempotent on {@code caseInstance.id()} — repeated start of an already-running instance MUST
   * return the existing id and MUST NOT throw (compliance test 3).
   *
   * <p>Implementations MUST translate adapter-side failures into {@link
   * com.wkspower.platform.domain.exception.WksWorkflowEngineException}.
   */
  String start(CaseInstanceRef caseInstance);

  /**
   * Stop backend execution for {@code caseInstance}. Idempotent on a not-running, unknown, or
   * already-cancelled instance — MUST NOT throw (compliance test 4).
   */
  void cancel(CaseInstanceRef caseInstance);
}
