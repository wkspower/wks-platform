package com.wkspower.platform.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.LicenseSnapshot;
import com.wkspower.platform.domain.service.LicenseState;
import com.wkspower.platform.domain.service.WksFeature;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SamlGatingFilter;
import com.wkspower.platform.security.SecurityConfig;
import com.wkspower.platform.security.WksUserPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test for {@link LicenseController}.
 *
 * <p>Verifies that {@code GET /api/license/status} returns a correct {@code LicenseStatusDto} for
 * each {@link LicenseState} variant and that unauthenticated requests are rejected with 401.
 */
@WebMvcTest(LicenseController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SamlGatingFilter.class})
class LicenseControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean LicenseService licenseService;
  @MockitoBean JwtTokenProvider jwtTokenProvider;
  @MockitoBean UserRepository userRepository;

  private static final UUID ACTOR_ID = UUID.randomUUID();

  private Authentication auth() {
    AuthenticatedUser user = new AuthenticatedUser(ACTOR_ID, "user@wkspower.local", Set.of("user"));
    WksUserPrincipal principal = new WksUserPrincipal(user);
    return new UsernamePasswordAuthenticationToken(
        principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

  // -------------------------------------------------------------------------
  // VALID state
  // -------------------------------------------------------------------------

  @Test
  void validState_returnsValidDtoWithTierAndNoExpiredAt() throws Exception {
    when(licenseService.getLicenseSnapshot())
        .thenReturn(
            new LicenseSnapshot(
                LicenseState.VALID, "enterprise", Instant.parse("2027-01-01T00:00:00Z")));
    when(licenseService.getLicenseHolder()).thenReturn("Acme Corp");
    when(licenseService.getPublicKeyFingerprint()).thenReturn("a".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.data.state").value("valid"))
        .andExpect(jsonPath("$.data.tier").value("enterprise"))
        .andExpect(jsonPath("$.data.expiredAt").doesNotExist())
        .andExpect(jsonPath("$.error").doesNotExist());
  }

  // -------------------------------------------------------------------------
  // OSS state
  // -------------------------------------------------------------------------

  @Test
  void ossState_returnsOssTierAndNoExpiredAt() throws Exception {
    when(licenseService.getLicenseSnapshot())
        .thenReturn(new LicenseSnapshot(LicenseState.OSS, "oss", null));
    when(licenseService.getLicenseHolder()).thenReturn(null);
    when(licenseService.getPublicKeyFingerprint()).thenReturn("b".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.data.state").value("oss"))
        .andExpect(jsonPath("$.data.tier").value("oss"))
        .andExpect(jsonPath("$.data.expiredAt").doesNotExist())
        .andExpect(jsonPath("$.error").doesNotExist());
  }

  // -------------------------------------------------------------------------
  // EXPIRED state — expiry timestamp must be included in response
  // -------------------------------------------------------------------------

  @Test
  void expiredState_returnsExpiredTierOssAndIsoExpiredAt() throws Exception {
    Instant expiredAt = Instant.parse("2025-12-31T23:59:59Z");
    when(licenseService.getLicenseSnapshot())
        .thenReturn(new LicenseSnapshot(LicenseState.EXPIRED, "oss", expiredAt));
    when(licenseService.getLicenseHolder()).thenReturn(null);
    when(licenseService.getPublicKeyFingerprint()).thenReturn("c".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.data.state").value("expired"))
        .andExpect(jsonPath("$.data.tier").value("oss"))
        .andExpect(jsonPath("$.data.expiredAt").value("2025-12-31T23:59:59Z"))
        .andExpect(jsonPath("$.error").doesNotExist());
  }

  // -------------------------------------------------------------------------
  // DEGRADED state
  // -------------------------------------------------------------------------

  @Test
  void degradedState_returnsOssTierAndNoExpiredAt() throws Exception {
    when(licenseService.getLicenseSnapshot())
        .thenReturn(new LicenseSnapshot(LicenseState.DEGRADED, "oss", null));
    when(licenseService.getLicenseHolder()).thenReturn(null);
    when(licenseService.getPublicKeyFingerprint()).thenReturn("d".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.data.state").value("degraded"))
        .andExpect(jsonPath("$.data.tier").value("oss"))
        .andExpect(jsonPath("$.data.expiredAt").doesNotExist())
        .andExpect(jsonPath("$.error").doesNotExist());
  }

  // -------------------------------------------------------------------------
  // Unauthenticated request must be rejected
  // -------------------------------------------------------------------------

  @Test
  void unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/license/status")).andExpect(status().isUnauthorized());
  }

  // -------------------------------------------------------------------------
  // CF2 — expiredAt must be ISO_INSTANT formatted
  // -------------------------------------------------------------------------

  @Test
  void statusEndpoint_expiredAt_usesIsoInstantFormat() throws Exception {
    Instant expiredAt = Instant.parse("2025-12-31T23:59:59Z");
    when(licenseService.getLicenseSnapshot())
        .thenReturn(new LicenseSnapshot(LicenseState.EXPIRED, "oss", expiredAt));
    when(licenseService.getLicenseHolder()).thenReturn(null);
    when(licenseService.getPublicKeyFingerprint()).thenReturn("e".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.expiredAt").value("2025-12-31T23:59:59Z"));
  }

  // -------------------------------------------------------------------------
  // Story 7-4 — new DTO fields: licenseHolder, expiresAt, publicKeyFingerprint
  // -------------------------------------------------------------------------

  @Test
  void statusEndpoint_includesLicenseHolder_whenValid() throws Exception {
    when(licenseService.getLicenseSnapshot())
        .thenReturn(
            new LicenseSnapshot(
                LicenseState.VALID, "enterprise", Instant.parse("2027-06-01T00:00:00Z")));
    when(licenseService.getLicenseHolder()).thenReturn("Acme Corp");
    when(licenseService.getPublicKeyFingerprint()).thenReturn("f".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.licenseHolder").value("Acme Corp"));
  }

  @Test
  void statusEndpoint_licenseHolder_nullForOss() throws Exception {
    when(licenseService.getLicenseSnapshot())
        .thenReturn(new LicenseSnapshot(LicenseState.OSS, "oss", null));
    when(licenseService.getLicenseHolder()).thenReturn(null);
    when(licenseService.getPublicKeyFingerprint()).thenReturn("0".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.licenseHolder").doesNotExist());
  }

  @Test
  void statusEndpoint_expiresAt_populatedForValid() throws Exception {
    Instant expiry = Instant.parse("2027-03-15T12:00:00Z");
    when(licenseService.getLicenseSnapshot())
        .thenReturn(new LicenseSnapshot(LicenseState.VALID, "team", expiry));
    when(licenseService.getLicenseHolder()).thenReturn("Beta Corp");
    when(licenseService.getPublicKeyFingerprint()).thenReturn("1".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.expiresAt").value("2027-03-15T12:00:00Z"));
  }

  @Test
  void statusEndpoint_expiresAt_nullForOss() throws Exception {
    when(licenseService.getLicenseSnapshot())
        .thenReturn(new LicenseSnapshot(LicenseState.OSS, "oss", null));
    when(licenseService.getLicenseHolder()).thenReturn(null);
    when(licenseService.getPublicKeyFingerprint()).thenReturn("2".repeat(64));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.expiresAt").doesNotExist());
  }

  @Test
  void statusEndpoint_publicKeyFingerprintAlwaysPresent() throws Exception {
    when(licenseService.getLicenseSnapshot())
        .thenReturn(new LicenseSnapshot(LicenseState.OSS, "oss", null));
    when(licenseService.getLicenseHolder()).thenReturn(null);
    // Simulate a realistic 64-char lowercase hex fingerprint
    when(licenseService.getPublicKeyFingerprint())
        .thenReturn("a3b4c5d6e7f801234567890abcdef0123456789abcdef0123456789abcdef01");

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.data.publicKeyFingerprint")
                .value("a3b4c5d6e7f801234567890abcdef0123456789abcdef0123456789abcdef01"));
  }

  // -------------------------------------------------------------------------
  // Story 7-3: GET /api/license/features — all registered features returned
  // -------------------------------------------------------------------------

  @Test
  void featuresEndpoint_returnsAllRegisteredFeatures() throws Exception {
    when(licenseService.getTier()).thenReturn("enterprise");
    // Stub both overloads: the controller calls isFeatureEnabled(WksFeature) which is a default
    // interface method. Mockito intercepts default methods directly on the mock (it does NOT
    // delegate to the real implementation), so we must stub the WksFeature overload explicitly.
    when(licenseService.isFeatureEnabled(any(WksFeature.class))).thenReturn(true);
    when(licenseService.isFeatureEnabled(any(String.class))).thenReturn(true);

    mockMvc
        .perform(get("/api/license/features").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.tier").value("enterprise"))
        .andExpect(jsonPath("$.data.features", hasSize(WksFeature.values().length)))
        .andExpect(jsonPath("$.data.features[0].key").isNotEmpty())
        .andExpect(jsonPath("$.data.features[0].description").isNotEmpty())
        .andExpect(jsonPath("$.data.features[0].bundleTiers").isArray())
        .andExpect(jsonPath("$.data.features[0].enabled").value(true));
  }

  @Test
  void featuresEndpoint_setsNoCacheHeader() throws Exception {
    when(licenseService.getTier()).thenReturn("oss");
    when(licenseService.isFeatureEnabled(any(String.class))).thenReturn(false);

    mockMvc
        .perform(get("/api/license/features").with(authentication(auth())))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"));
  }

  @Test
  void featuresEndpoint_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/license/features")).andExpect(status().isUnauthorized());
  }
}
