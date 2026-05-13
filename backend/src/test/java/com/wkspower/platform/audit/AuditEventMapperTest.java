package com.wkspower.platform.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.model.AuditSource;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 9-3 AC4 unit guard — every {@link AuditSource} sealed variant round-trips through {@link
 * AuditEventMapper#sourceType} + {@link AuditEventMapper#sourcePayload} + {@link
 * AuditEventMapper#fromColumns} identically. Pure unit (no Spring context, no DB) — the Postgres-IT
 * counterpart proves the same invariant survives a JDBC write/read cycle.
 */
class AuditEventMapperTest {

  @Test
  void userVariantRoundTrips() {
    UUID actorId = UUID.fromString("a1b2c3d4-1111-2222-3333-444455556666");
    AuditSource original = new AuditSource.User(actorId);

    String type = AuditEventMapper.sourceType(original);
    Map<String, Object> payload = AuditEventMapper.sourcePayload(original);

    assertThat(type).isEqualTo("USER");
    assertThat(payload).containsEntry("actorId", actorId.toString());

    AuditSource roundtripped = AuditEventMapper.fromColumns(type, payload);
    assertThat(roundtripped).isEqualTo(original);
    assertThat(roundtripped).isInstanceOf(AuditSource.User.class);
    assertThat(((AuditSource.User) roundtripped).actorId()).isEqualTo(actorId);
  }

  @Test
  void autoRuleVariantRoundTrips() {
    AuditSource original = new AuditSource.AutoRule("rule-onboard-fast-path");

    String type = AuditEventMapper.sourceType(original);
    Map<String, Object> payload = AuditEventMapper.sourcePayload(original);

    assertThat(type).isEqualTo("AUTO_RULE");
    assertThat(payload).containsEntry("ruleId", "rule-onboard-fast-path");

    AuditSource roundtripped = AuditEventMapper.fromColumns(type, payload);
    assertThat(roundtripped).isEqualTo(original);
    assertThat(((AuditSource.AutoRule) roundtripped).ruleId()).isEqualTo("rule-onboard-fast-path");
  }

  @Test
  void backendVariantRoundTrips() {
    AuditSource original = new AuditSource.Backend("bpmn");

    String type = AuditEventMapper.sourceType(original);
    Map<String, Object> payload = AuditEventMapper.sourcePayload(original);

    assertThat(type).isEqualTo("BACKEND");
    assertThat(payload).containsEntry("adapterName", "bpmn");

    AuditSource roundtripped = AuditEventMapper.fromColumns(type, payload);
    assertThat(roundtripped).isEqualTo(original);
    assertThat(((AuditSource.Backend) roundtripped).adapterName()).isEqualTo("bpmn");
  }

  @Test
  void executionUnmappedVariantRoundTrips() {
    AuditSource original = new AuditSource.ExecutionUnmapped("camunda");

    String type = AuditEventMapper.sourceType(original);
    Map<String, Object> payload = AuditEventMapper.sourcePayload(original);

    assertThat(type).isEqualTo("EXECUTION_UNMAPPED");
    assertThat(payload).containsEntry("originAdapter", "camunda");

    AuditSource roundtripped = AuditEventMapper.fromColumns(type, payload);
    assertThat(roundtripped).isEqualTo(original);
    assertThat(((AuditSource.ExecutionUnmapped) roundtripped).originAdapter()).isEqualTo("camunda");
  }

  @Test
  void unknownSourceTypeIsLoudFailure() {
    // Per feedback_silent_fallback_defeats_pin: never default-on-unknown.
    assertThatThrownBy(() -> AuditEventMapper.fromColumns("MYSTERY", Map.of("foo", "bar")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("MYSTERY")
        .hasMessageContaining("USER")
        .hasMessageContaining("AUTO_RULE")
        .hasMessageContaining("BACKEND")
        .hasMessageContaining("EXECUTION_UNMAPPED");
  }

  @Test
  void nullSourceTypeIsLoudFailure() {
    assertThatThrownBy(() -> AuditEventMapper.fromColumns(null, Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("source_type cannot be null");
  }

  @Test
  void nullPayloadIsLoudFailure() {
    assertThatThrownBy(() -> AuditEventMapper.fromColumns("USER", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("source_payload cannot be null");
  }

  @Test
  void missingPayloadKeyIsLoudFailure() {
    assertThatThrownBy(() -> AuditEventMapper.fromColumns("USER", Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("missing required key 'actorId'");
  }
}
