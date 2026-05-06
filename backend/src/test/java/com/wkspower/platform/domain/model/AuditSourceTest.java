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
    // Story 4.3.1 AC6 — miss-sentinel uses the BackendUnmapped sub-record. Real adapters with
    // adapterName="unmapped" render as backend(unmapped); the miss sentinel renders distinguishably
    // as backend(unmapped:<originAdapter>) so the audit string is never collidable.
    AuditSource collidingRealAdapter = new AuditSource.Backend("unmapped");
    AuditSource missSentinel = new AuditSource.BackendUnmapped("unmapped");

    assertThat(collidingRealAdapter.toString()).isEqualTo("backend(unmapped)");
    assertThat(missSentinel.toString()).isEqualTo("backend(unmapped:unmapped)");
    assertThat(missSentinel.toString()).isNotEqualTo(collidingRealAdapter.toString());
  }

  @Test
  void backendUnmappedCarriesOriginAdapter() {
    AuditSource missSentinel = new AuditSource.BackendUnmapped("bpmn");

    assertThat(missSentinel.toString()).isEqualTo("backend(unmapped:bpmn)");
  }

  @Test
  void nullArgumentsAreRejected() {
    assertThatThrownBy(() -> new AuditSource.User(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new AuditSource.AutoRule(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new AuditSource.Backend(null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new AuditSource.BackendUnmapped(null))
        .isInstanceOf(NullPointerException.class);
  }
}
