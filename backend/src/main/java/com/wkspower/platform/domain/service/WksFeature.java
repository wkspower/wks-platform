package com.wkspower.platform.domain.service;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of all license-gated features. Every feature that can be controlled by a license must
 * have an entry here — this is the single source of truth for gating decisions.
 *
 * <p>Story 7.3: {@code featureKey} is the stable wire string referenced by license JWTs and
 * callers. Renaming an entry here MUST be paired with a migration strategy (old keys still
 * recognized). {@code defaultTiers} lists the tier names whose bundles include this feature.
 *
 * <p>Note: {@code defaultTiers} stores tier name strings (not {@link TierBundle} enum references)
 * to avoid Java static initialisation order issues.
 */
public enum WksFeature {
  AUTH_SSO("auth.sso", "SSO/SAML authentication (FR28)", Set.of("enterprise", "demo")),

  WHITE_LABEL(
      "white-label", "White-labeling / custom branding (FR34)", Set.of("enterprise", "demo")),

  AUDIT_EXPORT("audit.export", "Audit log export (FR43)", Set.of("enterprise", "demo")),

  AUDIT_CHECKSUMS(
      "audit.checksums",
      "Tamper-evident audit checksums (FR46)",
      Set.of("team", "enterprise", "demo"));

  private final String key;
  private final String description;
  private final Set<String> defaultTiers;

  WksFeature(String key, String description, Set<String> defaultTiers) {
    this.key = key;
    this.description = description;
    this.defaultTiers = Set.copyOf(defaultTiers);
  }

  /** Returns the stable wire string for this feature (referenced in JWTs and API responses). */
  public String key() {
    return key;
  }

  /** Returns a human-readable description of this feature. */
  public String description() {
    return description;
  }

  /**
   * Returns the set of tier names whose bundles include this feature by default. The values match
   * {@link TierBundle#tier()} for each relevant bundle.
   */
  public Set<String> defaultTiers() {
    return defaultTiers;
  }

  /**
   * Looks up a {@link WksFeature} by its wire key. Returns empty if the key is not registered.
   *
   * @param key the feature key string (e.g. {@code "auth.sso"})
   */
  public static Optional<WksFeature> fromKey(String key) {
    return Arrays.stream(values()).filter(f -> f.key.equals(key)).findFirst();
  }
}
