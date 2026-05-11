package com.wkspower.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.WksFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-request gate for {@code /api/auth/saml/**} paths.
 *
 * <p>When {@link LicenseService#isFeatureEnabled(WksFeature) isFeatureEnabled(AUTH_SSO)} is {@code
 * false} (OSS, Team, Expired, or Degraded state), the filter short-circuits the request with HTTP
 * 404 and error-code {@code WKS-LIC-003} — the endpoint simply does not exist for the current
 * license. When the feature is enabled, the filter is a no-op and the request proceeds down the
 * Spring Security chain (currently returning 401 until Story 10.4 wires the SAML provider).
 *
 * <p>The response envelope matches {@link WksAuthenticationEntryPoint} ({@code code} / {@code
 * message} / {@code field}) so SI clients can deserialise both error paths with one DTO. (Story 7-5
 * AC1 literal text said {@code errorCode}, but Task 1 binds the implementation to the existing
 * envelope shape — kept consistent with the rest of the WKS error surface.)
 *
 * <p>Registered before {@link JwtAuthenticationFilter} in {@link SecurityConfig}. The per-request
 * {@link LicenseService} call is constant-time (the service memoises the license-verification
 * result internally — Story 7.1/7.2 design); no bean-construction-time snapshot is taken,
 * satisfying AC3 (hot-reload without restart).
 *
 * <p>Story 7.5 AC1 / AC2 / AC3.
 */
@Component
public class SamlGatingFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(SamlGatingFilter.class);
  private static final String SAML_PATH_PREFIX = "/api/auth/saml/";
  private static final String SAML_PATH_EXACT = "/api/auth/saml";

  private final LicenseService licenseService;
  private final ObjectMapper objectMapper;

  public SamlGatingFilter(LicenseService licenseService, ObjectMapper objectMapper) {
    this.licenseService = licenseService;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (!isSamlPath(path)) {
      chain.doFilter(request, response);
      return;
    }
    if (licenseService.isFeatureEnabled(WksFeature.AUTH_SSO)) {
      // Feature is licensed — pass through; Story 10.4 will add the actual SAML provider.
      chain.doFilter(request, response);
      return;
    }
    // Feature is off — 404 with WKS-LIC-003.
    LOG.debug(
        "event=saml.gate.blocked path={} licenseTier={} errorCode={}",
        path,
        licenseService.getTier(),
        ErrorCode.WKS_LIC_003.wire());
    rejectWithNotFound(response, path);
  }

  private static boolean isSamlPath(String path) {
    return path != null && (path.equals(SAML_PATH_EXACT) || path.startsWith(SAML_PATH_PREFIX));
  }

  private void rejectWithNotFound(HttpServletResponse response, String path) throws IOException {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("code", ErrorCode.WKS_LIC_003.wire());
    body.put("message", "SSO/SAML is not available on the current license.");
    body.put("field", null);
    objectMapper.writeValue(response.getWriter(), body);
  }
}
