package com.wkspower.platform.domain.config.model;

/**
 * One custom status declared by a case type. {@code color} is optional.
 *
 * <p>Story 3.6 AC1 — adds the {@code terminal} flag (deferred from Story 3.2; the slot did not
 * exist when the canonical {@code [open, closed]} default was introduced). Semantics: {@code
 * terminal=true} declares the status as a stage-scoped dead-end — same-stage transitions are
 * blocked once a case enters this status. The flag is purely declarative here; consumer enforcement
 * lives in {@code CaseService.transition} (Story 3.6 Task 5). Stage advance always bypasses the
 * terminal guard — advancing replaces the status anyway.
 *
 * <p>Defaults: omitted in YAML ⇒ {@code false}. The Story 3.2 canonical default flips {@code
 * closed.terminal = true}; {@code open.terminal = false} (see {@code
 * ConfigValidator.DEFAULT_STATUSES}).
 */
public record StatusDefinition(String id, String displayName, StatusColor color, boolean terminal) {

  /**
   * Backward-compat constructor for callers (and tests) authored before Story 3.6 introduced the
   * {@code terminal} slot. Defaults to {@code false} — matches the YAML-omitted shape.
   */
  public StatusDefinition(String id, String displayName, StatusColor color) {
    this(id, displayName, color, false);
  }
}
