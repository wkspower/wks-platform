package com.wkspower.platform.domain.port;

/**
 * Handler invoked by a {@link WorkflowAdapter} when a {@link ExecutionSignal} is emitted.
 * Registered via {@link WorkflowAdapter#onExecutionSignal(ExecutionSignalHandler)}.
 *
 * <p>Per-instance ordering is preserved by the adapter (compliance test 5). Ordering across
 * instances is adapter-defined.
 */
@FunctionalInterface
public interface ExecutionSignalHandler {

  /**
   * Receive a backend signal. Implementations MUST not throw — Story 4.3's router is the only
   * legitimate handler and it has its own error-translation contract.
   */
  void onSignal(ExecutionSignal signal);
}
