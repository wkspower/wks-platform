package com.wkspower.platform.domain.port;

import java.time.Instant;

/**
 * Tiny outbound port for the current time. Lets domain services swap a fixed {@link Instant} in
 * tests without dragging {@link java.time.Clock} (whose name collides) or Spring's clock
 * abstraction into the domain.
 */
public interface Clock {

  /** Current instant. Default infrastructure adapter delegates to {@code Instant.now()}. */
  Instant now();
}
