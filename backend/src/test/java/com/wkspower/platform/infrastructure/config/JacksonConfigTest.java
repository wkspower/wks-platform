package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Round-trip assertions guarding AC6 (ISO 8601 timestamps, canonical UUIDs) + strict inbound
 * parsing via {@code FAIL_ON_UNKNOWN_PROPERTIES}. Builds the real platform {@link ObjectMapper} by
 * feeding {@link JacksonConfig}'s customizer into a {@link Jackson2ObjectMapperBuilder} exactly as
 * Spring Boot does — no Spring context required.
 */
class JacksonConfigTest {

  private ObjectMapper mapper;

  @BeforeEach
  void setUp() {
    Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
    new JacksonConfig().wksObjectMapperCustomizer().customize(builder);
    mapper = builder.build();
  }

  @Test
  void instantSerialisesAsIsoZ() throws Exception {
    Instant t = Instant.parse("2026-01-02T03:04:05Z");
    String json = mapper.writeValueAsString(new Holder(t));
    assertThat(json).contains("\"value\":\"2026-01-02T03:04:05Z\"");
    assertThat(mapper.readValue(json, Holder.class).value()).isEqualTo(t);
  }

  @Test
  void offsetDateTimeRoundTripsAsIso() throws Exception {
    OffsetDateTime t = OffsetDateTime.parse("2026-04-24T14:22:18Z");
    String json = mapper.writeValueAsString(new OdtHolder(t));
    assertThat(json).contains("\"value\":\"2026-04-24T14:22:18Z\"");
  }

  @Test
  void localDateSerialisesAsIsoDate() throws Exception {
    LocalDate d = LocalDate.of(2026, 4, 24);
    String json = mapper.writeValueAsString(new LdHolder(d));
    assertThat(json).contains("\"value\":\"2026-04-24\"");
  }

  @Test
  void uuidSerialisesAsCanonicalHyphenatedString() throws Exception {
    UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
    String json = mapper.writeValueAsString(new UuidHolder(id));
    assertThat(json).contains("\"id\":\"11111111-2222-3333-4444-555555555555\"");
  }

  @Test
  void writeDatesAsTimestampsIsDisabled() {
    assertThat(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
  }

  @Test
  void unknownPropertyDeserialisationFails() {
    assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isTrue();
    assertThatThrownBy(
            () ->
                mapper.readValue(
                    "{\"id\":\"11111111-2222-3333-4444-555555555555\",\"bogus\":1}",
                    UuidHolder.class))
        .isInstanceOf(UnrecognizedPropertyException.class);
  }

  record Holder(Instant value) {}

  record OdtHolder(OffsetDateTime value) {}

  record LdHolder(LocalDate value) {}

  record UuidHolder(UUID id) {}
}
