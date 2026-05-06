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
    String jwt =
        buildValidJwt("enterprise", "Acme Corp", List.of("advanced-reporting", "sso"), expiry);
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    assertThat(svc.getTier()).isEqualTo("enterprise");
    assertThat(svc.getLicenseHolder()).isEqualTo("Acme Corp");
    assertThat(svc.getExpiry()).isEqualTo(expiry);
    assertThat(svc.isFeatureEnabled("advanced-reporting")).isTrue();
    assertThat(svc.isFeatureEnabled("sso")).isTrue();
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

    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("everything")).isFalse();
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

    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("any-feature")).isFalse();
  }

  // -------------------------------------------------------------------------
  // AC6 test 3 — missing file: OSS mode, isFeatureEnabled returns false
  // -------------------------------------------------------------------------

  @Test
  void missingFile_ossMode() {
    // Path that does not exist.
    LicenseServiceImpl svc = serviceFor(tempDir.resolve("nonexistent.jwt").toString());

    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("advanced-reporting")).isFalse();
    assertThat(svc.getLicenseHolder()).isNull();
    assertThat(svc.getExpiry()).isNull();
  }

  // -------------------------------------------------------------------------
  // AC6 test 3b — blank/null file path: OSS mode
  // -------------------------------------------------------------------------

  @Test
  void blankFilePath_ossMode() {
    LicenseServiceImpl svc = serviceFor("");

    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("any")).isFalse();
  }

  // -------------------------------------------------------------------------
  // AC6 test 4 — hot-reload cycle: valid → missing → valid
  // -------------------------------------------------------------------------

  @Test
  void hotReloadCycle_validToMissingToValid() throws IOException, InterruptedException {
    Path licenseFile = tempDir.resolve("hot-reload.jwt");

    // Step 1 — write a valid JWT
    Instant expiry = Instant.now().plus(365, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    String jwt = buildValidJwt("team", "HotReload Inc", List.of("reports"), expiry);
    Files.writeString(licenseFile, jwt, StandardCharsets.UTF_8);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // Initially valid
    assertThat(svc.getTier()).isEqualTo("team");
    assertThat(svc.isFeatureEnabled("reports")).isTrue();

    // Step 2 — delete the file (simulates license removal).
    // Use poll() so the lastModified-comparison gate is exercised (AC5). poll() detects
    // the missing file because lastModifiedSeen differs from its initial value (0 sentinel).
    Files.delete(licenseFile);
    svc.poll();

    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("reports")).isFalse();

    // Step 3 — write a new (different tier) valid JWT.
    // Sleep 10 ms to ensure the OS-level lastModified timestamp advances so poll()'s
    // lastModified comparison detects the change.
    Thread.sleep(10);
    String jwt2 = buildValidJwt("enterprise", "HotReload Inc", List.of("reports", "sso"), expiry);
    Files.writeString(licenseFile, jwt2, StandardCharsets.UTF_8);
    svc.poll();

    assertThat(svc.getTier()).isEqualTo("enterprise");
    assertThat(svc.isFeatureEnabled("reports")).isTrue();
    assertThat(svc.isFeatureEnabled("sso")).isTrue();
  }

  // -------------------------------------------------------------------------
  // Fix 3 — Expired JWT: falls back to degraded state, no exception thrown
  // -------------------------------------------------------------------------

  @Test
  void expiredJwt_degradedState_noException() throws IOException {
    // Build a JWT whose expiry is 60 seconds in the past.
    String jwt =
        Jwts.builder()
            .subject("Expired Corp")
            .expiration(Date.from(Instant.now().minusSeconds(60)))
            .claim("tier", "enterprise")
            .claim("features", List.of("advanced-reporting"))
            .signWith(testKeyPair.getPrivate())
            .compact();
    Path licenseFile = writeLicenseFile(jwt);

    LicenseServiceImpl svc = serviceFor(licenseFile.toString());

    // JJWT rejects expired tokens with JwtException → service falls to degraded/oss state
    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("advanced-reporting")).isFalse();
    assertThat(svc.getLicenseHolder()).isNull();
    assertThat(svc.getExpiry()).isNull();
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
    assertThat(svc.getTier()).isEqualTo("oss");
    assertThat(svc.isFeatureEnabled("everything")).isFalse();
    assertThat(svc.getLicenseHolder()).isNull();
  }
}
