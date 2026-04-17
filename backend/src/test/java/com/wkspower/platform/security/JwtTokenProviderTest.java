package com.wkspower.platform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class JwtTokenProviderTest {

  private static final String SECRET_BASE64 = base64Secret();

  private final MockEnvironment devEnv = new MockEnvironment().withProperty("ignore", "x");
  private final MockEnvironment productionEnv =
      (MockEnvironment) new MockEnvironment().withProperty("ignore", "x");

  {
    productionEnv.setActiveProfiles("production");
  }

  @Test
  void issuesAndParsesSameSubjectAndClaims() {
    JwtTokenProvider provider = new JwtTokenProvider(SECRET_BASE64, 1, devEnv);
    UUID id = UUID.randomUUID();
    User user = new User(id, "admin@wkspower.local", Set.of("admin"), true);

    String token = provider.issue(user);
    AuthenticatedUser parsed = provider.parse(token).orElseThrow();

    assertThat(parsed.id()).isEqualTo(id);
    assertThat(parsed.email()).isEqualTo("admin@wkspower.local");
    assertThat(parsed.roles()).containsExactly("admin");
  }

  @Test
  void rejectsExpiredToken() {
    JwtTokenProvider provider = new JwtTokenProvider(SECRET_BASE64, 1, devEnv);
    SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_BASE64));
    String expired =
        Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .claim("email", "u@x")
            .claim("roles", java.util.List.of("admin"))
            .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
            .expiration(Date.from(Instant.now().minusSeconds(3600)))
            .signWith(key, Jwts.SIG.HS256)
            .compact();

    assertThat(provider.parse(expired)).isEmpty();
  }

  @Test
  void rejectsTamperedSignature() {
    JwtTokenProvider provider = new JwtTokenProvider(SECRET_BASE64, 1, devEnv);
    User user = new User(UUID.randomUUID(), "u@x", Set.of("admin"), true);
    String token = provider.issue(user);
    String tampered = token.substring(0, token.length() - 2) + "aa";

    assertThat(provider.parse(tampered)).isEmpty();
  }

  @Test
  void rejectsTokenSignedWithWrongKey() {
    JwtTokenProvider provider = new JwtTokenProvider(SECRET_BASE64, 1, devEnv);
    byte[] otherKey = new byte[32];
    new SecureRandom().nextBytes(otherKey);
    String foreignToken =
        Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .claim("email", "u@x")
            .claim("roles", java.util.List.of("admin"))
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusSeconds(3600)))
            .signWith(Keys.hmacShaKeyFor(otherKey), Jwts.SIG.HS256)
            .compact();

    assertThat(provider.parse(foreignToken)).isEmpty();
  }

  @Test
  void rejectsUnsignedNoneAlgorithmToken() {
    JwtTokenProvider provider = new JwtTokenProvider(SECRET_BASE64, 1, devEnv);
    // Manual unsigned JWT with alg=none — JJWT must refuse to parse as signed claims.
    String header =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
    String payload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                ("{\"sub\":\"" + UUID.randomUUID() + "\",\"email\":\"u@x\",\"roles\":[]}")
                    .getBytes(StandardCharsets.UTF_8));
    String unsigned = header + "." + payload + ".";

    assertThat(provider.parse(unsigned)).isEmpty();
  }

  @Test
  void productionWithoutSecretFailsFast() {
    assertThatThrownBy(() -> new JwtTokenProvider("", 8, productionEnv))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("WKS-API-053");
  }

  @Test
  void devWithoutSecretUsesEphemeralKeyAndSelfRoundTrips() {
    JwtTokenProvider provider = new JwtTokenProvider("", 1, devEnv);
    String token = provider.issue(new User(UUID.randomUUID(), "u@x", Set.of("admin"), true));
    assertThat(provider.parse(token)).isPresent();
  }

  @Test
  void rejectsSecretShorterThan256Bits() {
    assertThatThrownBy(
            () ->
                new JwtTokenProvider(
                    Base64.getEncoder().encodeToString(new byte[16]), 8, productionEnv))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("WKS-API-053");
  }

  private static String base64Secret() {
    byte[] raw = new byte[32];
    new SecureRandom().nextBytes(raw);
    return Base64.getEncoder().encodeToString(raw);
  }
}
