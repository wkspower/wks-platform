package com.wkspower.platform.domain.service;

/**
 * Represents the resolved state of the platform license.
 *
 * <p>State semantics:
 *
 * <ul>
 *   <li>{@link #VALID} — a valid, non-expired Ed25519-signed JWT was loaded.
 *   <li>{@link #OSS} — no license file is present (the default, expected state for OSS users).
 *   <li>{@link #EXPIRED} — a validly-signed JWT was found but its {@code exp} claim is in the past.
 *       The expiry timestamp is still readable for display. All EE features are disabled.
 *   <li>{@link #DEGRADED} — a license file was found but could not be parsed (bad signature,
 *       malformed JWT, or any other failure). All EE features are disabled.
 * </ul>
 *
 * <p>The platform NEVER refuses to boot due to license issues (AC4). This enum is always populated
 * with a valid value — it never throws.
 */
public enum LicenseState {
  /** Valid, non-expired license loaded. EE features enabled per JWT claims. */
  VALID,

  /** No license file configured or present. Platform operates in OSS mode — not an error. */
  OSS,

  /** License JWT has a valid signature but its {@code exp} claim is in the past. OSS fallback. */
  EXPIRED,

  /**
   * License file present but unverifiable (bad signature, malformed JWT, I/O error). OSS fallback.
   */
  DEGRADED
}
