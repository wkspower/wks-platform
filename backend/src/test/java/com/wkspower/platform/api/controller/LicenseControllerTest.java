package com.wkspower.platform.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.LicenseState;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
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
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
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
    when(licenseService.getLicenseState()).thenReturn(LicenseState.VALID);
    when(licenseService.getTier()).thenReturn("enterprise");
    when(licenseService.getExpiry()).thenReturn(Instant.parse("2027-01-01T00:00:00Z"));

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
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
    when(licenseService.getLicenseState()).thenReturn(LicenseState.OSS);
    when(licenseService.getTier()).thenReturn("oss");
    when(licenseService.getExpiry()).thenReturn(null);

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
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
    when(licenseService.getLicenseState()).thenReturn(LicenseState.EXPIRED);
    when(licenseService.getTier()).thenReturn("oss");
    when(licenseService.getExpiry()).thenReturn(expiredAt);

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
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
    when(licenseService.getLicenseState()).thenReturn(LicenseState.DEGRADED);
    when(licenseService.getTier()).thenReturn("oss");
    when(licenseService.getExpiry()).thenReturn(null);

    mockMvc
        .perform(get("/api/license/status").with(authentication(auth())))
        .andExpect(status().isOk())
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
}
