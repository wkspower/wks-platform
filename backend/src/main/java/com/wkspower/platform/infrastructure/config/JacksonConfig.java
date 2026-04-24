package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Locks down Jackson behaviour for the platform ObjectMapper:
 *
 * <ul>
 *   <li>{@code WRITE_DATES_AS_TIMESTAMPS=false} — every {@code Instant}/{@code OffsetDateTime}/
 *       {@code LocalDate} serialises as ISO 8601 text (AC6). Spring Boot's default is already
 *       {@code false}, but we override explicitly because historical regressions came from {@code
 *       spring.jackson.serialization.write-dates-as-timestamps=true} slipping into yaml "for
 *       consistency with millis" and silently breaking the API contract.
 *   <li>{@code JavaTimeModule} is auto-registered by Spring Boot via {@code
 *       jackson-datatype-jsr310} on the classpath; we rely on that and assert the behaviour in
 *       tests.
 *   <li>{@code FAIL_ON_UNKNOWN_PROPERTIES=true} — strict inbound parsing. Unknown fields fail the
 *       request fast rather than silently discarding payload keys the client intended us to read.
 * </ul>
 *
 * <p>Using {@link Jackson2ObjectMapperBuilderCustomizer} (not a replacement {@code ObjectMapper}
 * bean) so Spring Boot's module auto-discovery and Jackson auto-configuration stay in control — we
 * only adjust the features that matter.
 */
@Configuration
public class JacksonConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer wksObjectMapperCustomizer() {
    return builder ->
        builder
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }
}
