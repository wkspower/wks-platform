package com.wkspower.platform.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 4.3 AC5 — pin {@link AuditSource}'s {@link Object#toString()} renderings exactly to the
 * bare strings already used in {@link Stage#source()} and the {@code StageEntered}/{@code
 * StageExited} domain events ({@code "wks-auto-rule"}, {@code "manual"}, {@code "backend-signal"} /
 * FR8 {@code "backend(<adapter>)"}). Drift here would silently desync the new router code path from
 * the legacy string slots that Story 4.4 must keep aligning until the migration completes.
 */
class AuditSourceTest {

  @Test
  void userTypeRendersAsManual() {
    AuditSource source = new AuditSource.User(UUID.randomUUID());

    assertThat(source.toString()).isEqualTo("manual");
  }

  @Test
  void autoRuleTypeRendersAsWksAutoRule() {
    AuditSource source = new AuditSource.AutoRule("auto-1");

    assertThat(source.toString()).isEqualTo("wks-auto-rule");
  }

  @Test
  void backendTypeRendersAsBackendOfAdapterName() {
    AuditSource source = new AuditSource.Backend("bpmn");

    assertThat(source.toString()).isEqualTo("backend(bpmn)");
  }

  @Test
  void backendUnmappedSentinelRendersUnspoofably() {
    // Story 4.3.1 AC6 / Story 4-8 — miss-sentinel uses the ExecutionUnmapped sub-record. Real
    // adapters with adapterName="unmapped" render as backend(unmapped); the miss sentinel renders
    // distinguishably as execution(unmapped:<originAdapter>) so the audit string is never
    // collidable. Story 4-8 re-anchored the sentinel prefix from "backend(" to "execution(" to
    // align with the ExecutionSignal vocabulary; the V202605070006 Flyway migration rewrites any
    // pre-existing backend(unmapped:*) wire strings in case_audit.
    AuditSource collidingRealAdapter = new AuditSource.Backend("unmapped");
    AuditSource missSentinel = new AuditSource.ExecutionUnmapped("unmapped");

    assertThat(collidingRealAdapter.toString()).isEqualTo("backend(unmapped)");
    assertThat(missSentinel.toString()).isEqualTo("execution(unmapped:unmapped)");
    assertThat(missSentinel.toString()).isNotEqualTo(collidingRealAdapter.toString());
  }

  @Test
  void backendUnmappedCarriesOriginAdapter() {
    AuditSource missSentinel = new AuditSource.ExecutionUnmapped("bpmn");

    assertThat(missSentinel.toString()).isEqualTo("execution(unmapped:bpmn)");
  }

  @Test
  void nullArgumentsAreRejected() {
    assertThatThrownBy(() -> new AuditSource.User(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new AuditSource.AutoRule(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new AuditSource.Backend(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new AuditSource.ExecutionUnmapped(null))
        .isInstanceOf(NullPointerException.class);
  }
}
