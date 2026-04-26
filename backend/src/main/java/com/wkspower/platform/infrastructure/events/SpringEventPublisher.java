package com.wkspower.platform.infrastructure.events;

import com.wkspower.platform.domain.port.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-backed adapter for {@link EventPublisher}. Delegates to the framework's {@code
 * ApplicationEventPublisher}; in-process {@code @EventListener} beans receive events synchronously
 * by default — the SSE bridge (Story 4.3) attaches such a listener.
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
}
