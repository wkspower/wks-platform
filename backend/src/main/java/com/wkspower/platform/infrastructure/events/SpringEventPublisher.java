package com.wkspower.platform.infrastructure.events;

import com.wkspower.platform.domain.port.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Spring-backed adapter for {@link EventPublisher}. {@link #publish} delegates to the framework's
 * {@code ApplicationEventPublisher}; in-process {@code @EventListener} beans receive events
 * synchronously by default — the SSE bridge (Story 4.3) attaches such a listener.
 *
 * <p>{@link #publishAfterCommit} (Story 3.1) registers a {@link TransactionSynchronization} so the
 * event lands only after the current transaction commits. Outside an active transaction (test
 * harnesses, background work) it falls back to synchronous {@link #publish}.
 */
@Component
public class SpringEventPublisher implements EventPublisher {

  private final ApplicationEventPublisher delegate;

  public SpringEventPublisher(ApplicationEventPublisher delegate) {
    this.delegate = delegate;
  }

  @Override
  public void publish(Object event) {
    delegate.publishEvent(event);
  }

  @Override
  public void publishAfterCommit(Object event) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              delegate.publishEvent(event);
            }
          });
    } else {
      delegate.publishEvent(event);
    }
  }
}
