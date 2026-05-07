package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.infrastructure.license.LicenseServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link LicenseServiceImpl} covering all four AC6 state transitions.
 *
 * <p>Pure Java — no Spring context, no {@code @SpringBootTest}. A test Ed25519 key pair is
 * generated once per test class; each test writes (or omits) a JWT file in a temp directory.
 */
class LicenseServiceTest {

  @TempDir Path tempDir;

  private static KeyPair testKeyPair;

  @BeforeAll
  static void generateKeyPair() throws NoSuchAlgorithmException {
    testKeyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private Path writeLicenseFile(String jwt) throws IOException {
    Path file = tempDir.resolve("test-license.jwt");
    Files.writeString(file, jwt, StandardCharsets.UTF_8);
    return file;
  }

  private String buildValidJwt(String tier, String holder, List<String> features, Instant expiry) {
    return Jwts.builder()
        .subject(holder)
        .expiration(expiry != null ? Date.from(expiry) : null)
        .claim("tier", tier)
        .claim("features", features)
        .signWith(testKeyPair.getPrivate())
        .compact();
  }

  private LicenseServiceImpl serviceFor(String filePath) {
    return new LicenseServiceImpl(filePath, testKeyPair.getPublic());
  }

  // -------------------------------------------------------------------------
  // AC6 test 1 — valid JWT: correct claims exposed
  // -------------------------------------------------------------------------

  @Test
  void validJwt_correctClaimsExposed() throws IOException {
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    // Use registered feature keys — enterprise tier bundle auto-grants all four Phase-0 features
    String jwt =
        buildValidJwt("enterprise", "Acme Corp", List.of("auth.sso", "audit.export"), expiry);
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.VALID);
    assertThat(svc.getTier()).isEqualTo("enterprise");
    assertThat(svc.getLicenseHolder()).isEqualTo("Acme Corp");
    assertThat(svc.getExpiry()).isEqualTo(expiry);
    // enterprise tier bundle grants auth.sso and audit.export
    assertThat(svc.isFeatureEnabled("auth.sso")).isTrue();
    assertThat(svc.isFeatureEnabled("audit.export")).isTrue();
    // unregistered keys are fail-closed
    assertThat(svc.isFeatureEnabled("non-existent-feature")).isFalse();
  }

  // -------------------------------------------------------------------------
  // AC6 test 2 — bad-signature JWT: degraded state, no exception thrown
  // -------------------------------------------------------------------------

  @Test
  void badSignatureJwt_degradedState_noException() throws IOException, NoSuchAlgorithmException {
    // Sign with a DIFFERENT private key so the bundled public key rejects it.
    KeyPair otherKeyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    String jwt =
        Jwts.builder()
            .subject("Evil Corp")
            .expiration(Date.from(Instant.now().plus(365, ChronoUnit.DAYS)))
            .claim("tier", "enterprise")
            .claim("features", List.of("everything"))
            .signWith(otherKeyPair.getPrivate())
            .compact();
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.DEGRADED);
    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse(); // use registered key
    assertThat(svc.getLicenseHolder()).isNull();
    assertThat(svc.getExpiry()).isNull();
  }

  // -------------------------------------------------------------------------
  // AC6 test 2b — malformed JWT: degraded state, no exception thrown
  // -------------------------------------------------------------------------

  @Test
  void malformedJwt_degradedState_noException() throws IOException {
    Path licenseFile = writeLicenseFile("this-is-not-a-jwt");

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.DEGRADED);
    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse(); // registered key, but degraded state
  }

  // -------------------------------------------------------------------------
  // AC6 test 3 — missing file: OSS mode, isFeatureEnabled returns false
  // -------------------------------------------------------------------------

  @Test
  void missingFile_ossMode() {
    // Path that does not exist.
    LicenseServiceImpl svc = serviceFor(tempDir.resolve("nonexistent.jwt").toString());

    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.OSS);
    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse(); // registered key, but OSS state
    assertThat(svc.getLicenseHolder()).isNull();
    assertThat(svc.getExpiry()).isNull();
  }

  // -------------------------------------------------------------------------
  // AC6 test 3b — blank/null file path: OSS mode
  // -------------------------------------------------------------------------

  @Test
  void blankFilePath_ossMode() {
    LicenseServiceImpl svc = serviceFor("");

    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.OSS);
    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse(); // registered key, but OSS state
  }

  // -------------------------------------------------------------------------
  // AC6 test 4 — hot-reload cycle: valid → missing → valid
  // -------------------------------------------------------------------------

  @Test
  void hotReloadCycle_validToMissingToValid() throws IOException, InterruptedException {
    Path licenseFile = tempDir.resolve("hot-reload.jwt");

    // Step 1 — write a valid JWT for team tier (team bundle includes audit.checksums)
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    String jwt = buildValidJwt("team", "HotReload Inc", List.of("audit.checksums"), expiry);
    Files.writeString(licenseFile, jwt, StandardCharsets.UTF_8);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // Initially valid — team bundle includes audit.checksums
    assertThat(svc.getTier()).isEqualTo("team");
    assertThat(svc.isFeatureEnabled("audit.checksums")).isTrue();

    // Step 2 — delete the file (simulates license removal).
    // Use poll() so the lastModified-comparison gate is exercised (AC5). poll() detects
    // the missing file because lastModifiedSeen differs from its initial value (0 sentinel).
    Files.delete(licenseFile);
    svc.poll();

    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("audit.checksums")).isFalse();

    // Step 3 — write a new (different tier) valid JWT.
    // Sleep 10 ms to ensure the OS-level lastModified timestamp advances so poll()'s
    // lastModified comparison detects the change.
    Thread.sleep(10);
    String jwt2 =
        buildValidJwt("enterprise", "HotReload Inc", List.of("audit.export", "auth.sso"), expiry);
    Files.writeString(licenseFile, jwt2, StandardCharsets.UTF_8);
    svc.poll();

    assertThat(svc.getTier()).isEqualTo("enterprise");
    assertThat(svc.isFeatureEnabled("audit.export")).isTrue(); // via tier bundle
    assertThat(svc.isFeatureEnabled("auth.sso")).isTrue(); // via tier bundle
  }

  // -------------------------------------------------------------------------
  // AC2 / Story 7-2 — Expired JWT: EXPIRED state with expiry populated
  // -------------------------------------------------------------------------

  @Test
  void expiredJwt_expiredState_expiryPopulated_noException() throws IOException {
    // Build a JWT whose expiry is 1 day in the past (signed with the test key — valid signature).
    Instant pastExpiry =
        Instant.now()
            .minus(1, java.time.temporal.ChronoUnit.DAYS)
            .truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    String jwt =
        Jwts.builder()
            .subject("Expired Corp")
            .expiration(Date.from(pastExpiry))
            .claim("tier", "enterprise")
            .claim("features", List.of("advanced-reporting"))
            .signWith(testKeyPair.getPrivate())
            .compact();
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // ExpiredJwtException is caught separately — state is EXPIRED, expiry is readable
    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.EXPIRED);
    assertThat(svc.getTier()).isEqualTo("oss"); // EE features disabled on expiry
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse(); // registered key, but expired state
    assertThat(svc.getExpiry()).isNotNull();
    assertThat(svc.getExpiry()).isEqualTo(pastExpiry);
    // CF1: holder IS preserved on expiry (extracted from ExpiredJwtException.getClaims())
    assertThat(svc.getLicenseHolder()).isEqualTo("Expired Corp");
  }

  // -------------------------------------------------------------------------
  // Fix 5 — Algorithm-pinning: HS256 token rejected, no exception thrown
  // -------------------------------------------------------------------------

  @Test
  void hs256SignedJwt_rejected_degradedState_noException() throws IOException {
    // Build a JWT signed with HMAC-SHA256 (wrong algorithm — service expects Ed25519).
    // JJWT 0.12.x's verifyWith(Ed25519PublicKey) rejects non-EdDSA tokens because the
    // public key type mismatches the HMAC algorithm, so no explicit algorithm pin is needed.
    SecretKey hmacKey = Keys.hmacShaKeyFor(new byte[32]);
    String jwt =
        Jwts.builder()
            .subject("Attacker Corp")
            .expiration(Date.from(Instant.now().plus(365, ChronoUnit.DAYS)))
            .claim("tier", "enterprise")
            .claim("features", List.of("everything"))
            .signWith(hmacKey, Jwts.SIG.HS256)
            .compact();
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // Algorithm mismatch → JwtException → degraded/oss state; no exception escapes
    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.DEGRADED);
    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse(); // registered key, but degraded state
    assertThat(svc.getLicenseHolder()).isNull();
  }

  // -------------------------------------------------------------------------
  // Story 7-3: AC3/AC4 — isFeatureEnabled with unknown key logs WARN, returns false
  // -------------------------------------------------------------------------

  @Test
  void isFeatureEnabled_withUnknownKey_returnsFalse() throws IOException {
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    String jwt = buildValidJwt("enterprise", "Acme Corp", List.of(), expiry);
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // Unregistered key — registry guard returns false (fail-closed)
    assertThat(svc.isFeatureEnabled("any-unknown-key")).isFalse();
    assertThat(svc.isFeatureEnabled("advanced-reporting")).isFalse();
    assertThat(svc.isFeatureEnabled("")).isFalse();
  }

  // -------------------------------------------------------------------------
  // Story 7-3: AC2 — enterprise tier auto-grants features via bundle (no JWT features[] needed)
  // -------------------------------------------------------------------------

  @Test
  void isFeatureEnabled_enterpriseTierBundle_grantsAllFourFeaturesWithoutExplicitClaims()
      throws IOException {
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    // Empty features[] — features should come from tier bundle alone
    String jwt = buildValidJwt("enterprise", "Acme Corp", List.of(), expiry);
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // Enterprise bundle grants all four Phase-0 features even without explicit features[]
    assertThat(svc.isFeatureEnabled("auth.sso")).isTrue();
    assertThat(svc.isFeatureEnabled("white-label")).isTrue();
    assertThat(svc.isFeatureEnabled("audit.export")).isTrue();
    assertThat(svc.isFeatureEnabled("audit.checksums")).isTrue();
  }

  // -------------------------------------------------------------------------
  // Story 7-3: AC2 — OSS tier grants no EE features
  // -------------------------------------------------------------------------

  @Test
  void isFeatureEnabled_ossTier_returnsFalseForEEFeature() throws IOException {
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    String jwt = buildValidJwt("oss", "OSS User", List.of(), expiry);
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.VALID);
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse();
    assertThat(svc.isFeatureEnabled("white-label")).isFalse();
    assertThat(svc.isFeatureEnabled("audit.export")).isFalse();
    assertThat(svc.isFeatureEnabled("audit.checksums")).isFalse();
  }

  // -------------------------------------------------------------------------
  // Story 7-3: AC2 — TEAM tier grants audit.checksums only
  // -------------------------------------------------------------------------

  @Test
  void isFeatureEnabled_teamTier_grantsOnlyAuditChecksums() throws IOException {
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    String jwt = buildValidJwt("team", "Team User", List.of(), expiry);
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.VALID);
    assertThat(svc.isFeatureEnabled("audit.checksums")).isTrue();
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse();
    assertThat(svc.isFeatureEnabled("white-label")).isFalse();
    assertThat(svc.isFeatureEnabled("audit.export")).isFalse();
  }

  // -------------------------------------------------------------------------
  // Story 7-3: AC2 — JWT features[] additive override beyond tier bundle
  // -------------------------------------------------------------------------

  @Test
  void isFeatureEnabled_jwtFeaturesAdditive_grantsFeaturesBeyondTierBundle() throws IOException {
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    // OSS tier but JWT explicitly grants auth.sso as additive override
    String jwt = buildValidJwt("oss", "Custom User", List.of("auth.sso"), expiry);
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // auth.sso granted via explicit JWT features[] even though OSS bundle has none
    assertThat(svc.isFeatureEnabled("auth.sso")).isTrue();
    // others still not granted
    assertThat(svc.isFeatureEnabled("white-label")).isFalse();
  }

  // -------------------------------------------------------------------------
  // Story 7-3: CF1 — EXPIRED license preserves holder from JWT sub claim
  // -------------------------------------------------------------------------

  @Test
  void getLicenseHolder_onExpiredLicense_returnsHolderFromJwt() throws IOException {
    Instant pastExpiry = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    String jwt =
        Jwts.builder()
            .subject("Expired Holder Corp")
            .expiration(Date.from(pastExpiry))
            .claim("tier", "enterprise")
            .claim("features", List.of("auth.sso"))
            .signWith(testKeyPair.getPrivate())
            .compact();
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    assertThat(svc.getLicenseState()).isEqualTo(LicenseState.EXPIRED);
    // CF1: holder preserved from expired JWT
    assertThat(svc.getLicenseHolder()).isEqualTo("Expired Holder Corp");
    // features are still disabled on expiry
    assertThat(svc.isFeatureEnabled("auth.sso")).isFalse();
  }

  // -------------------------------------------------------------------------
  // Story 7-3: typed overload WksFeature compile-time safety
  // -------------------------------------------------------------------------

  @Test
  void isFeatureEnabled_typedOverload_enterpriseTierGrantsFeature() throws IOException {
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    String jwt = buildValidJwt("enterprise", "Acme Corp", List.of(), expiry);
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // Typed overload delegates to String overload via default method
    assertThat(svc.isFeatureEnabled(WksFeature.AUTH_SSO)).isTrue();
    assertThat(svc.isFeatureEnabled(WksFeature.WHITE_LABEL)).isTrue();
    assertThat(svc.isFeatureEnabled(WksFeature.AUDIT_EXPORT)).isTrue();
    assertThat(svc.isFeatureEnabled(WksFeature.AUDIT_CHECKSUMS)).isTrue();
  }
}
