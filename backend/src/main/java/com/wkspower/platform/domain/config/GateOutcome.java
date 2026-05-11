package com.wkspower.platform.domain.config;

/**
 * Story 3.9 CF#1 — describes what the blast-radius gate actually did on an accepted force-override
 * deploy. Threaded through {@link DeployResult} and {@link ValidationResult} so {@link
 * com.wkspower.platform.api.controller.AdminController} can emit a forensically-accurate audit log
 * entry on the {@code admin.deploy.force_override} event.
 *
 * <p>The three values cover all paths:
 *
 * <ul>
 *   <li>{@link #LENIENT_SUCCESS} — {@code force=true} was supplied, but the lenient parser handled
 *       the prior YAML cleanly; no service-side bypass engaged. The gate passed via normal
 *       classification; {@code force} was structurally accepted but had no effect beyond allowing
 *       the deploy to proceed. The audit log reason is {@code lenient-success}.
 *   <li>{@link #UNPARSEABLE_BYPASS} — the prior YAML could not be re-parsed even leniently; the
 *       gate was bypassed entirely under {@code force=true}. A service-side WARN was fired. The
 *       audit log reason is {@code WKS-CFG-030-unparseable} (preserves 3-11 wire string).
 *   <li>{@link #NO_OVERRIDE_USED} — {@code force=true} was supplied but neither path engaged (e.g.
 *       first deploy for this CaseType — no prior version to compare). The audit log is NOT emitted
 *       on this branch (preserves 3-11 silent-ignore semantics for the no-prior case).
 * </ul>
 */
public enum GateOutcome {
  /**
   * {@code force=true} was supplied and the prior YAML was re-parsed leniently (or strictly); the
   * blast-radius classifier ran normally. No WKS-CFG-030 bypass engaged.
   */
  LENIENT_SUCCESS,

  /**
   * {@code force=true} was supplied and the prior YAML could not be re-parsed (strict + lenient
   * both failed). The gate was bypassed. Service-level WARN emitted.
   */
  UNPARSEABLE_BYPASS,

  /**
   * {@code force=true} was supplied but no override was necessary (e.g. first deploy — no prior
   * version). Audit log is NOT emitted on this branch.
   */
  NO_OVERRIDE_USED
}
