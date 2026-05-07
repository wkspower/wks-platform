package com.wkspower.platform.security;

import com.wkspower.platform.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Issues and parses HS256 JWTs backed by an HMAC-SHA key. The key source is {@code wks.jwt.secret}
 * (a Base64-encoded HMAC key ≥32 bytes / 256 bits).
 *
 * <ul>
 *   <li>{@code dev} profile with missing secret → ephemeral key generated per JVM (WKS-API-052)
 *   <li>{@code production} with missing secret → application fails to start (WKS-API-053)
 * </ul>
 *
 * <p>{@code JwtTokenProvider} and the {@code infrastructure.license} package are the only classes
 * allowed to import {@code io.jsonwebtoken.*} — enforced by ArchUnit (Story 7.1 relaxed the rule to
 * also cover {@code ..license..}).
 */
@Component
public class JwtTokenProvider {

  static final String PROFILE_PRODUCTION = "production";
  static final String PROFILE_DEV = "dev";
  static final int MIN_SECRET_BYTES = 32;

  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

  private final SecretKey signingKey;
  private final Duration tokenTtl;

  public JwtTokenProvider(
      @Value("${wks.jwt.secret:}") String secretBase64,
      @Value("${wks.jwt.ttl-hours:8}") int ttlHours,
      Environment environment) {
    if (ttlHours <= 0) {
      throw new IllegalStateException(
          "WKS-API-053 wks.jwt.ttl-hours must be a positive integer (got " + ttlHours + ").");
    }
    this.signingKey = resolveKey(secretBase64, environment);
    this.tokenTtl = Duration.ofHours(ttlHours);
  }

  /** Returns the TTL in seconds — used by controllers to align cookie {@code Max-Age}. */
  public long ttlSeconds() {
    return tokenTtl.toSeconds();
  }

  /** Issues a compact JWT carrying the user id, email, and roles. */
  public String issue(User user) {
    Instant now = Instant.now();
    Instant expiry = now.plus(tokenTtl);
    return Jwts.builder()
        .subject(user.id().toString())
        .claim("email", user.email())
        .claim("roles", List.copyOf(user.roles()))
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(signingKey, Jwts.SIG.HS256)
        .compact();
  }

  /**
   * Parses and validates a token. Returns empty on any failure (expiry, bad signature, wrong
   * algorithm, malformed payload). Never throws; callers let the filter chain produce 401.
   */
  public Optional<AuthenticatedUser> parse(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    try {
      Jws<Claims> jws = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
      Claims claims = jws.getPayload();
      UUID id = UUID.fromString(claims.getSubject());
      String email = claims.get("email", String.class);
      Set<String> roles = extractRoles(claims);
      if (email == null) {
        return Optional.empty();
      }
      return Optional.of(new AuthenticatedUser(id, email, roles));
    } catch (JwtException | IllegalArgumentException ex) {
      log.debug("JWT rejected: {}", ex.getClass().getSimpleName());
      return Optional.empty();
    }
  }

  private static Set<String> extractRoles(Claims claims) {
    Object raw = claims.get("roles");
    if (raw instanceof List<?> list) {
      Set<String> roles = new HashSet<>();
      for (Object item : list) {
        if (item instanceof String s) {
          roles.add(s);
        }
      }
      return Set.copyOf(roles);
    }
    return Set.of();
  }

  private static SecretKey resolveKey(String secretBase64, Environment environment) {
    boolean production =
        Arrays.asList(environment.getActiveProfiles()).contains(PROFILE_PRODUCTION);
    String trimmed = secretBase64 == null ? "" : secretBase64.strip();
    if (trimmed.isEmpty()) {
      if (production) {
        throw new IllegalStateException(
            "WKS-API-053 JWT signing secret is required in production. Set WKS_JWT_SECRET to a "
                + "Base64-encoded random value of at least 32 bytes.");
      }
      byte[] random = new byte[MIN_SECRET_BYTES];
      new SecureRandom().nextBytes(random);
      log.info("WKS-API-052 Ephemeral dev JWT secret generated; tokens invalidate on restart.");
      return Keys.hmacShaKeyFor(random);
    }
    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(trimmed);
    } catch (IllegalArgumentException ex) {
      throw new IllegalStateException(
          "WKS-API-053 WKS_JWT_SECRET is not valid Base64. Provide a Base64-encoded random value "
              + "of at least "
              + MIN_SECRET_BYTES
              + " bytes.");
    }
    if (decoded.length < MIN_SECRET_BYTES) {
      throw new IllegalStateException(
          "WKS-API-053 WKS_JWT_SECRET must decode to at least "
              + MIN_SECRET_BYTES
              + " bytes (256 bits). Provide Base64-encoded randomness.");
    }
    return Keys.hmacShaKeyFor(decoded);
  }
}
