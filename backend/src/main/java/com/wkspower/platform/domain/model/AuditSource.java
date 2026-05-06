package com.wkspower.platform.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Typed source attribution for stage / status / task transitions (Story 4.3 AC5; Decision 1's three
 * canonical sources). The legacy bare-string vocabulary lived in {@link Stage#source()}, {@link
 * com.wkspower.platform.domain.event.StageEntered#source()}, {@link
 * com.wkspower.platform.domain.event.StageExited#source()} and friends — strings {@code
 * "wks-auto-rule"}, {@code "manual"}, {@code "backend-signal"}.
 *
 * <p>Story 4.3 introduces this {@code sealed interface} for the new {@code BackendSignalRouter}
 * code path only; the existing string slots stay unchanged. Migration of every call site to {@code
 * AuditSource} is folded into Story 4.4 (the next story to touch the audit surface for backend
 * signals) per {@code feedback_fold_debt_into_stories.md}.
 *
 * <p>{@link #toString()} is the bridge: it returns the bare wire string already used in the legacy
 * {@code source} columns so a router-emitted audit event carrying an {@code AuditSource} can be
 * compared to the existing {@code Stage.source} convention without drift. {@link AuditSourceTest}
 * pins the three return values exactly.
 *
 * <p>One difference from the legacy vocabulary: {@link Backend} renders as {@code
 * "backend(<adapterName>)"} (e.g. {@code "backend(bpmn)"}) — FR8 source attribution. The legacy
 * bare string was {@code "backend-signal"} with no adapter identity baked in; Story 4.3 reframes
 * this as the new contract per AC5. The legacy {@code "backend-signal"} string remains accepted by
 * {@link com.wkspower.platform.domain.service.WksStageAdvancer#advance} so existing call sites do
 * not break — that migration is part of 4.4.
 *
 * <p>The interface is {@code sealed} with three permitted records — adding a fourth source kind
 * requires editing this file (reviewer-visible surface change), matching the rigour of {@code
 * feedback_error_codes_are_wire_contract.md} for load-bearing wire shapes.
 */
public sealed interface AuditSource
    permits AuditSource.User, AuditSource.AutoRule, AuditSource.Backend {

  /** Manual user action — wire string {@code "manual"}. */
  record User(UUID actorId) implements AuditSource {
    public User {
      Objects.requireNonNull(actorId, "actorId");
    }

    @Override
    public String toString() {
      return "manual";
    }
  }

  /** WKS-internal auto-rule transition — wire string {@code "wks-auto-rule"}. */
  record AutoRule(String ruleId) implements AuditSource {
    public AutoRule {
      Objects.requireNonNull(ruleId, "ruleId");
    }

    @Override
    public String toString() {
      return "wks-auto-rule";
    }
  }

  /** Backend-driven transition — wire string {@code "backend(<adapterName>)"} (FR8). */
  record Backend(String adapterName) implements AuditSource {
    public Backend {
      Objects.requireNonNull(adapterName, "adapterName");
    }

    @Override
    public String toString() {
      return "backend(" + adapterName + ")";
    }
  }
}
