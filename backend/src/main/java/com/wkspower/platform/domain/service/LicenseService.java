package com.wkspower.platform.domain.service;

import java.time.Instant;

/**
 * Domain port for license state queries.
 *
 * <p>All gating decisions flow through this interface. Callers never inspect the JWT or file system
 * directly — they ask the service. The service is always available: it returns safe defaults (OSS
 * mode, all features disabled) when no license is present or when verification fails.
 *
 * <p>Implementation lives in {@code infrastructure/license/LicenseServiceImpl.java}. Spring wiring
 * is in {@code infrastructure/config/LicenseConfig.java}.
 */
public interface LicenseService {

  /**
   * Returns {@code true} if the verified license JWT declares {@code featureKey} in its {@code
   * features} claim, or if the active tier bundle includes the key. Returns {@code false} when no
   * valid license is loaded (fail-closed). Logs WARN and returns {@code false} for keys not
   * registered in {@link WksFeature}.
   *
   * @param featureKey the feature identifier, e.g. {@code "auth.sso"}
   */
  boolean isFeatureEnabled(String featureKey);

  /**
   * Type-safe overload of {@link #isFeatureEnabled(String)}. Preferred for all in-codebase callers
   * — ensures gating decisions reference a registered feature key (compile-time safety via the
   * {@link WksFeature} enum).
   *
   * @param feature the registered feature to check
   */
  default boolean isFeatureEnabled(WksFeature feature) {
    return isFeatureEnabled(feature.key());
  }

  /**
   * Returns the tier declared in the JWT {@code tier} claim (e.g. {@code "enterprise"}, {@code
   * "team"}). Returns {@code "oss"} when no valid license is loaded.
   */
  String getTier();

  /**
   * Returns the expiry declared in the JWT {@code exp} claim. Returns {@code null} when no valid
   * license is loaded.
   */
  Instant getExpiry();

  /**
   * Returns the JWT {@code sub} claim (license holder / organisation name). Returns {@code null}
   * when no valid license is loaded.
   */
  String getLicenseHolder();

  /**
   * Returns the resolved {@link LicenseState} for the current license. Never throws.
   *
   * <ul>
   *   <li>{@link LicenseState#VALID} — non-expired, signature-verified JWT is loaded.
   *   <li>{@link LicenseState#OSS} — no license file configured or present.
   *   <li>{@link LicenseState#EXPIRED} — valid signature but {@code exp} is in the past.
   *   <li>{@link LicenseState#DEGRADED} — file present but unverifiable.
   * </ul>
   */
  LicenseState getLicenseState();

  /**
   * Returns a consistent point-in-time snapshot of the license state, tier, and expiry. Use this
   * instead of calling {@link #getLicenseState()}, {@link #getTier()}, and {@link #getExpiry()}
   * separately to avoid torn reads across concurrent state updates.
   */
  LicenseSnapshot getLicenseSnapshot();
}
