package com.wkspower.platform.infrastructure.time;

import com.wkspower.platform.domain.port.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** Default {@link Clock} bound to {@link Instant#now()}. */
@Component
public class SystemClock implements Clock {

  @Override
  public Instant now() {
    return Instant.now();
  }
}
