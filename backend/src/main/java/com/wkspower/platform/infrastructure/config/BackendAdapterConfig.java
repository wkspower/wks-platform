package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.service.BackendAdapterBinder;
import com.wkspower.platform.domain.service.NullAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Story 4.1 — wires the pure-Java {@link NullAdapter} and {@link BackendAdapterBinder} (both in
 * {@code domain/service}, framework-free per NFR36) as Spring singletons so future call sites
 * (Story 4.4 / 4.5) can inject {@code BackendAdapterBinder}.
 *
 * <p>This config has zero call sites in Story 4.1 — it exists so the beans are present on the
 * application context and so Story 4.4 can refactor call sites without re-wiring. The
 * infrastructure boundary is the correct home: {@code domain/} stays Spring-free.
 */
@Configuration
public class BackendAdapterConfig {

  @Bean
  public NullAdapter nullAdapter() {
    return new NullAdapter();
  }

  @Bean
  public BackendAdapterBinder backendAdapterBinder(NullAdapter nullAdapter) {
    return new BackendAdapterBinder(nullAdapter);
  }
}
