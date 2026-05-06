package com.wkspower.platform.infrastructure.license;

import com.wkspower.platform.domain.service.LicenseService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Loads and verifies an Ed25519-signed license JWT from the file system.
 *
 * <p>Lifecycle:
 *
 * <ol>
 *   <li>On construction, immediately calls {@link #load()} to populate in-memory state.
 *   <li>A {@link Scheduled} method polls every {@code pollIntervalSeconds} seconds (default 30) and
 *       reloads when the file's {@code lastModified} timestamp changes.
 * </ol>
 *
 * <p>Fail-closed: any load failure (missing file, bad signature, malformed JWT) leaves the service
 * in OSS/degraded mode. Platform boot is never blocked.
 *
 * <p>Not annotated with {@code @Component} — Spring wiring is in {@code LicenseConfig}.
 */
public class LicenseServiceImpl implements LicenseService {

  private static final Logger LOG = LoggerFactory.getLogger(LicenseServiceImpl.class);

  private static final String OSS_TIER = "oss";

  /** Immutable snapshot of parsed license state. */
  private record LicenseState(
      boolean valid, String tier, String holder, Instant expiry, List<String> features) {

    static LicenseState oss() {
      return new LicenseState(false, OSS_TIER, null, null, Collections.emptyList());
    }

    static LicenseState degraded() {
      return new LicenseState(false, OSS_TIER, null, null, Collections.emptyList());
    }

    static LicenseState loaded(String tier, String holder, Instant expiry, List<String> features) {
      return new LicenseState(true, tier, holder, expiry, features);
    }
  }

  private final String licenseFilePath;
  private final PublicKey publicKey;

  /** Tracks the last-seen {@code lastModified} for hot-reload. */
  private volatile long lastModifiedSeen = -1L;

  private final AtomicReference<LicenseState> state = new AtomicReference<>(LicenseState.oss());

  /**
   * @param licenseFilePath path to the license JWT file (may be empty/null for OSS mode)
   * @param publicKey the Ed25519 public key used to verify signatures
   */
  public LicenseServiceImpl(String licenseFilePath, PublicKey publicKey) {
    this.licenseFilePath = licenseFilePath;
    this.publicKey = publicKey;
    load();
  }

  // -------------------------------------------------------------------------
  // LicenseService API
  // -------------------------------------------------------------------------

  @Override
  public boolean isFeatureEnabled(String featureKey) {
    LicenseState s = state.get();
    return s.valid() && s.features().contains(featureKey);
  }

  @Override
  public String getTier() {
    return state.get().tier();
  }

  @Override
  public Instant getExpiry() {
    return state.get().expiry();
  }

  @Override
  public String getLicenseHolder() {
    return state.get().holder();
  }

  // -------------------------------------------------------------------------
  // Hot-reload polling (AC5)
  // -------------------------------------------------------------------------

  /**
   * Polls every {@code wks.license.poll-interval-seconds} seconds (default 30). The fixed-delay
   * expression binds at context refresh time.
   */
  @Scheduled(fixedDelayString = "${wks.license.poll-interval-seconds:30}000")
  public void poll() {
    if (licenseFilePath == null || licenseFilePath.isBlank()) {
      return; // OSS mode — nothing to poll
    }
    Path path = Path.of(licenseFilePath);
    if (!Files.exists(path)) {
      if (lastModifiedSeen != 0L) {
        lastModifiedSeen = 0L;
        load();
      }
      return;
    }
    try {
      long current = Files.getLastModifiedTime(path).toMillis();
      if (current != lastModifiedSeen) {
        lastModifiedSeen = current;
        load();
        LOG.info("[LicenseService] License reloaded — tier={}", state.get().tier());
      }
    } catch (IOException e) {
      LOG.warn("[LicenseService] License poll failed reading lastModified: {}", e.getMessage());
    }
  }

  // -------------------------------------------------------------------------
  // Internal load
  // -------------------------------------------------------------------------

  /** Reads, verifies, and atomically updates in-memory state. Never throws. */
  public void load() {
    if (licenseFilePath == null || licenseFilePath.isBlank()) {
      state.set(LicenseState.oss());
      LOG.info("[LicenseService] No license file found — operating in OSS mode");
      return;
    }

    Path path = Path.of(licenseFilePath);
    if (!Files.exists(path)) {
      state.set(LicenseState.oss());
      LOG.info("[LicenseService] No license file found — operating in OSS mode");
      return;
    }

    String jwt;
    try {
      jwt = Files.readString(path, StandardCharsets.UTF_8).strip();
    } catch (IOException e) {
      state.set(LicenseState.degraded());
      LOG.warn(
          "[LicenseService] License load failed: {} — operating in degraded state", e.getMessage());
      return;
    }

    try {
      Claims claims =
          Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(jwt).getPayload();

      String tier = claims.get("tier", String.class);
      if (tier == null || tier.isBlank()) {
        tier = OSS_TIER;
      }
      String holder = claims.getSubject();
      Instant expiry = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;

      @SuppressWarnings("unchecked")
      List<String> features = claims.get("features", List.class);
      if (features == null) {
        features = Collections.emptyList();
      }

      LicenseState loaded = LicenseState.loaded(tier, holder, expiry, features);
      state.set(loaded);
      LOG.info(
          "[LicenseService] License loaded — tier={}, holder={}, expires={}",
          tier,
          holder,
          expiry != null ? expiry.toString() : "never");

    } catch (JwtException | IllegalArgumentException e) {
      state.set(LicenseState.degraded());
      LOG.warn(
          "[LicenseService] License load failed: {} — operating in degraded state", e.getMessage());
    }
  }

  // -------------------------------------------------------------------------
  // Static helpers (package-visible for tests)
  // -------------------------------------------------------------------------

  /**
   * Loads the bundled Ed25519 public key from {@code license-public.pem} on the classpath.
   *
   * @throws IllegalStateException if the key cannot be loaded
   */
  public static PublicKey loadBundledPublicKey() {
    try (InputStream in =
        LicenseServiceImpl.class.getClassLoader().getResourceAsStream("license-public.pem")) {
      if (in == null) {
        throw new IllegalStateException("Bundled license public key not found on classpath");
      }
      String pem = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      return parsePemPublicKey(pem);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read bundled license public key", e);
    }
  }

  /**
   * Parses a PEM-encoded Ed25519 public key (X.509 / SubjectPublicKeyInfo format).
   *
   * @param pem the PEM string (may include or omit headers)
   */
  public static PublicKey parsePemPublicKey(String pem) {
    String base64 =
        pem.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(base64);
    try {
      KeyFactory kf = KeyFactory.getInstance("Ed25519");
      return kf.generatePublic(new X509EncodedKeySpec(der));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Failed to parse Ed25519 public key", e);
    }
  }
}
