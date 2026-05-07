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
   * features} claim. Returns {@code false} when no valid license is loaded (fail-closed).
   *
   * @param featureKey the feature identifier, e.g. {@code "advanced-reporting"}
   */
  boolean isFeatureEnabled(String featureKey);

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
}
