package com.wkspower.platform.domain.port;

/**
 * Outbound port for emitting domain events. Implementations bridge to a transport (Spring's {@code
 * ApplicationEventPublisher} in 2.2; SSE / audit listeners follow in Story 4.x).
 *
 * <p>Event types are domain records — no marker interface, no inheritance. Spring's publisher
 * accepts {@link Object} since 4.2, so the port follows suit.
 */
public interface EventPublisher {

  /** Publish a domain event synchronously. */
  void publish(Object event);

  /**
   * Publish a domain event after the current transaction commits (Story 3.1 AC4–AC7 discipline).
   * Mirrors Story 2.4's {@code CaseStatusListener} idiom — subscribers must not observe state that
   * the underlying transaction subsequently rolls back.
   *
   * <p>Outside an active transaction (test harnesses, background work) the default implementation
   * delegates to {@link #publish} so unit tests using a simple recording stub keep observing events
   * synchronously without overriding this method.
   */
  default void publishAfterCommit(Object event) {
    publish(event);
  }
}
