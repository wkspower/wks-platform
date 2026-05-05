package com.wkspower.platform.domain.port;

/**
 * Handler invoked by a {@link BackendAdapter} when a {@link BackendSignal} is emitted. Registered
 * via {@link BackendAdapter#onBackendSignal(BackendSignalHandler)}.
 *
 * <p>Per-instance ordering is preserved by the adapter (compliance test 5). Ordering across
 * instances is adapter-defined.
 */
@FunctionalInterface
public interface BackendSignalHandler {

  /**
   * Receive a backend signal. Implementations MUST not throw — Story 4.3's router is the only
   * legitimate handler and it has its own error-translation contract.
   */
  void onSignal(BackendSignal signal);
}
