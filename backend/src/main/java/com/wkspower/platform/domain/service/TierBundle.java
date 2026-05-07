package com.wkspower.platform.domain.service;

import java.util.Arrays;
import java.util.Set;

/**
 * Tier bundle definitions. Each bundle lists the feature keys included by default for that tier.
 * These definitions are versioned with the build — license JWTs reference a tier name only.
 *
 * <p>Story 7.3: license JWT's "tier" claim resolves the bundle. JWT's "features[]" claim can add
 * features beyond the bundle (additive override). No removal override in Phase 0.
 *
 * <p>Note: this enum stores feature keys as {@link String} values (not {@link WksFeature} enum
 * references) to avoid Java static initialisation order issues.
 */
public enum TierBundle {
  OSS("oss", Set.of()),

  /** Phase 0: no Team-exclusive features yet; placeholder for a future story. */
  TEAM("team", Set.of("audit.checksums")),

  ENTERPRISE("enterprise", Set.of("auth.sso", "white-label", "audit.export", "audit.checksums")),

  DEMO("demo", Set.of("auth.sso", "white-label", "audit.export", "audit.checksums"));

  private final String tier;
  private final Set<String> includedFeatures;

  TierBundle(String tier, Set<String> includedFeatures) {
    this.tier = tier;
    this.includedFeatures = Set.copyOf(includedFeatures);
  }

  /** The tier name as referenced by the license JWT's {@code tier} claim. */
  public String tier() {
    return tier;
  }

  /** The set of feature keys included in this bundle by default. */
  public Set<String> includedFeatures() {
    return includedFeatures;
  }

  /**
   * Returns the {@link TierBundle} for the given tier name (case-insensitive). Unknown names fall
   * back to {@link #OSS} (fail-safe). Always returns a non-null bundle.
   *
   * @param tierName the tier name string (e.g. {@code "enterprise"}) — may be {@code null}
   */
  public static TierBundle forTier(String tierName) {
    if (tierName == null) return OSS;
    return Arrays.stream(values())
        .filter(b -> b.tier.equalsIgnoreCase(tierName))
        .findFirst()
        .orElse(OSS);
  }
}
