package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WksFeature} and {@link TierBundle} — the feature-flag registry and tier
 * bundle definitions introduced in Story 7.3.
 *
 * <p>Pure Java — no Spring context.
 */
class FeatureFlagRegistryTest {

  // -------------------------------------------------------------------------
  // WksFeature registry completeness (AC1)
  // -------------------------------------------------------------------------

  @Test
  void allFourPhase0FeaturesRegistered() {
    Set<String> registeredKeys =
        Arrays.stream(WksFeature.values()).map(WksFeature::key).collect(Collectors.toSet());

    assertThat(registeredKeys)
        .containsExactlyInAnyOrder("auth.sso", "white-label", "audit.export", "audit.checksums");
  }

  @Test
  void fromKey_knownKey_returnsPresent() {
    assertThat(WksFeature.fromKey("auth.sso")).isPresent();
    assertThat(WksFeature.fromKey("white-label")).isPresent();
    assertThat(WksFeature.fromKey("audit.export")).isPresent();
    assertThat(WksFeature.fromKey("audit.checksums")).isPresent();
  }

  @Test
  void fromKey_unknownKey_returnsEmpty() {
    assertThat(WksFeature.fromKey("unknown-key")).isEmpty();
    assertThat(WksFeature.fromKey("")).isEmpty();
    assertThat(WksFeature.fromKey("AUDIT.CHECKSUMS")).isEmpty(); // case-sensitive
  }

  @Test
  void eachFeatureHasNonBlankDescription() {
    for (WksFeature f : WksFeature.values()) {
      assertThat(f.description()).as("description for %s", f.name()).isNotBlank();
    }
  }

  // -------------------------------------------------------------------------
  // TierBundle definitions (AC2)
  // -------------------------------------------------------------------------

  @Test
  void oss_hasNoIncludedFeatures() {
    assertThat(TierBundle.OSS.includedFeatures()).isEmpty();
  }

  @Test
  void team_includesAuditChecksums() {
    assertThat(TierBundle.TEAM.includedFeatures()).contains("audit.checksums");
  }

  @Test
  void enterprise_includesAllFourPhase0Features() {
    assertThat(TierBundle.ENTERPRISE.includedFeatures())
        .containsExactlyInAnyOrder("auth.sso", "white-label", "audit.export", "audit.checksums");
  }

  @Test
  void demo_includesAllFourPhase0Features() {
    assertThat(TierBundle.DEMO.includedFeatures())
        .containsExactlyInAnyOrder("auth.sso", "white-label", "audit.export", "audit.checksums");
  }

  // -------------------------------------------------------------------------
  // TierBundle.forTier() fallback (AC2)
  // -------------------------------------------------------------------------

  @Test
  void forTier_enterprise_returnsEnterprise() {
    assertThat(TierBundle.forTier("enterprise")).isEqualTo(TierBundle.ENTERPRISE);
  }

  @Test
  void forTier_caseInsensitive() {
    assertThat(TierBundle.forTier("ENTERPRISE")).isEqualTo(TierBundle.ENTERPRISE);
    assertThat(TierBundle.forTier("Enterprise")).isEqualTo(TierBundle.ENTERPRISE);
    assertThat(TierBundle.forTier("DEMO")).isEqualTo(TierBundle.DEMO);
  }

  @Test
  void forTier_unknownName_fallsBackToOss() {
    assertThat(TierBundle.forTier("unknown-tier")).isEqualTo(TierBundle.OSS);
    assertThat(TierBundle.forTier("")).isEqualTo(TierBundle.OSS);
  }

  @Test
  void forTier_null_fallsBackToOss() {
    assertThat(TierBundle.forTier(null)).isEqualTo(TierBundle.OSS);
  }

  // -------------------------------------------------------------------------
  // Fail-closed for unregistered key (AC3 / AC4) — registry guard
  // -------------------------------------------------------------------------

  @Test
  void fromKey_returnsEmpty_forUnregisteredKey() {
    // This is the registry guard — callers that use the String overload with an unregistered key
    // should get empty from fromKey(), which triggers the WARN+false path in LicenseServiceImpl.
    assertThat(WksFeature.fromKey("any-unknown-key")).isEmpty();
  }

  // -------------------------------------------------------------------------
  // WksFeature.defaultTiers() metadata (AC5 — endpoint shape)
  // -------------------------------------------------------------------------

  @Test
  void authSso_defaultTiers_containsEnterpriseAndDemo() {
    Set<String> tiers = WksFeature.AUTH_SSO.defaultTiers();
    assertThat(tiers).containsExactlyInAnyOrder("enterprise", "demo");
  }

  @Test
  void auditChecksums_defaultTiers_containsTeamEnterpriseAndDemo() {
    Set<String> tiers = WksFeature.AUDIT_CHECKSUMS.defaultTiers();
    assertThat(tiers).containsExactlyInAnyOrder("team", "enterprise", "demo");
  }

  // -------------------------------------------------------------------------
  // Cross-consistency: WksFeature.defaultTiers() must match TierBundle.includedFeatures()
  // -------------------------------------------------------------------------

  @Test
  void wksFeatureDefaultTiers_consistentWithTierBundleIncludedFeatures() {
    for (WksFeature feature : WksFeature.values()) {
      for (String tierName : feature.defaultTiers()) {
        TierBundle bundle = TierBundle.forTier(tierName);
        assertThat(bundle)
            .as("tier %s referenced by %s must resolve to a known TierBundle", tierName, feature)
            .isNotEqualTo(TierBundle.OSS);
        assertThat(bundle.includedFeatures())
            .as("TierBundle.%s must include feature %s", tierName, feature.key())
            .contains(feature.key());
      }
    }
  }
}
