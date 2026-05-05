package com.wkspower.platform.domain.port;

/**
 * Subscription handle returned by {@link BackendAdapter#onBackendSignal(BackendSignalHandler)}.
 * Closing the subscription removes the handler — subsequent signals MUST NOT be delivered to a
 * closed subscription's handler.
 *
 * <p>Style note: {@code close()} is declared without checked exception, mirroring the {@link
 * WorkflowEngine} port style.
 */
public interface BackendSignalSubscription extends AutoCloseable {

  @Override
  void close();
}
