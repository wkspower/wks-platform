package com.wkspower.platform.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SamlGatingFilter;
import com.wkspower.platform.security.SecurityConfig;
import com.wkspower.platform.security.WksUserPrincipal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Story 7-6 AC-2 — Audit-export endpoint gating IT (WebMvcTest slice; H2 not needed — no DB path).
 *
 * <p>Verifies that {@code POST /api/admin/audit/export}:
 *
 * <ul>
 *   <li>Returns 403 + {@code WKS-LIC-005} when {@code audit.export} feature is disabled.
 *   <li>Returns 501 + {@code WKS-API-501} when {@code audit.export} feature is enabled
 *       (honest-deferral stub).
 * </ul>
 *
 * <p>Named {@code *IT} per the story's {@code it:} block convention. Uses {@code @WebMvcTest} for
 * fast startup — the audit-export controller has no DB code path.
 *
 * <p>AC-4 compliance: imports {@code SecurityConfig}; {@code @MockitoBean LicenseService} on class.
 */
@WebMvcTest(AuditExportController.class)
@Import({
  SecurityConfig.class,
  GlobalExceptionHandler.class,
  JwtAuthenticationFilter.class,
  SamlGatingFilter.class
})
class AuditExportGatingIT {

  @Autowired private MockMvc mockMvc;

  /** AC-4: @MockitoBean LicenseService on every SecurityConfig-importing slice. */
  @MockitoBean private LicenseService licenseService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private UserRepository userRepository;

  private static final UUID ACTOR_ID = UUID.randomUUID();

  private Authentication auth() {
    AuthenticatedUser user = new AuthenticatedUser(ACTOR_ID, "admin@wks.local", Set.of("admin"));
    WksUserPrincipal principal = new WksUserPrincipal(user);
    return new UsernamePasswordAuthenticationToken(
        principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  // ---------------------------------------------------------------------------
  // AC-2: returns403_whenFeatureDisabled_501_whenEnabled
  // ---------------------------------------------------------------------------

  @Test
  void returns403_whenFeatureDisabled_501_whenEnabled() throws Exception {
    // --- Positive path: feature disabled → 403 + WKS-LIC-005 ---
    when(licenseService.isFeatureEnabled(WksFeature.AUDIT_EXPORT)).thenReturn(false);
    when(licenseService.getTier()).thenReturn("oss");

    mockMvc
        .perform(
            post("/api/admin/audit/export")
                .with(authentication(auth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value(ErrorCode.WKS_LIC_005.wire()))
        .andExpect(jsonPath("$.error.field").value("audit.export"));

    // --- Negative path: feature enabled → 501 + WKS-API-501 (honest-deferral stub) ---
    when(licenseService.isFeatureEnabled(WksFeature.AUDIT_EXPORT)).thenReturn(true);

    mockMvc
        .perform(
            post("/api/admin/audit/export")
                .with(authentication(auth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isNotImplemented())
        .andExpect(jsonPath("$.error.code").value(ErrorCode.WKS_API_501.wire()))
        .andExpect(
            jsonPath("$.error.message")
                .value(org.hamcrest.Matchers.containsString(AuditExportController.TRACKING_STORY)));
  }
}
