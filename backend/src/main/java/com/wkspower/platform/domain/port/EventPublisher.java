package com.wkspower.platform.domain.port;

/**
 * Outbound port for emitting domain events. Implementations bridge to a transport (Spring's {@code
 * ApplicationEventPublisher} in 2.2; SSE / audit listeners follow in Story 4.x).
 *
 * <p>Event types are domain records — no marker interface, no inheritance. Spring's publisher
 * accepts {@link Object} since 4.2, so the port follows suit.
 */
public interface EventPublisher {

  /** Publish a domain event. */
  void publish(Object event);
}
