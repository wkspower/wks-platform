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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
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
 * <p><b>Fail-closed policy.</b> If {@code LicenseService.isFeatureEnabled} throws (e.g. the
 * service is mid-reload, dependency-injected stub mis-wired, or any unexpected runtime fault),
 * the filter treats SSO as <em>unavailable</em> and emits 404 + {@code WKS-LIC-004}. This is
 * deliberately the same HTTP shape as the "feature off" response so a degraded license layer
 * cannot accidentally open the SAML surface to an unauthenticated caller. The underlying
 * exception is logged at WARN for operators.
 *
 * <p>Story 7.5 AC1 / AC2 / AC3.
 */
@Component
public class SamlGatingFilter extends OncePerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(SamlGatingFilter.class);
  // Ant matcher (case-insensitive, path-normalised) — covers /api/auth/saml and
  // /api/auth/saml/** in one rule, defeats trivial casing/encoding bypasses
  // (e.g. /API/AUTH/SAML/metadata, //api/auth/saml///init).
  private static final RequestMatcher SAML_MATCHER =
      new AntPathRequestMatcher("/api/auth/saml/**", null, /* caseSensitive */ false);

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
    // CORS preflights MUST reach SecurityConfig's `OPTIONS /** permitAll` rule untouched —
    // a 404+JSON here would corrupt the preflight and break browser-side SAML init from the SPA.
    if ("OPTIONS".equals(request.getMethod())) {
      chain.doFilter(request, response);
      return;
    }
    String path = request.getRequestURI();
    if (!SAML_MATCHER.matches(request)) {
      chain.doFilter(request, response);
      return;
    }
    boolean ssoEnabled;
    try {
      ssoEnabled = licenseService.isFeatureEnabled(WksFeature.AUTH_SSO);
    } catch (Exception e) {
      // Fail CLOSED — see class Javadoc. Treat unknown-availability as unavailable so a
      // degraded license layer cannot open the SAML surface.
      LOG.warn(
          "event=saml.gate.licenseService.error path={} errorCode={} cause={}",
          path,
          ErrorCode.WKS_LIC_004.wire(),
          e.toString(),
          e);
      rejectWithNotFound(
          response, path, ErrorCode.WKS_LIC_004.wire(), "SSO availability check unavailable");
      return;
    }
    if (ssoEnabled) {
      // Feature is licensed — pass through; Story 10.4 will add the actual SAML provider.
      chain.doFilter(request, response);
      return;
    }
    // Feature is off — 404 with WKS-LIC-003.
    LOG.debug(
        "event=saml.gate.blocked path={} licenseTier={} errorCode={}",
        path,
        safeTier(),
        ErrorCode.WKS_LIC_003.wire());
    rejectWithNotFound(
        response,
        path,
        ErrorCode.WKS_LIC_003.wire(),
        "SSO/SAML is not available on the current license.");
  }

  /** Best-effort tier lookup for the debug log line — never throws. */
  private String safeTier() {
    try {
      return licenseService.getTier();
    } catch (Exception e) {
      return "unknown";
    }
  }

  private void rejectWithNotFound(
      HttpServletResponse response, String path, String code, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("code", code);
    body.put("message", message);
    body.put("field", null);
    objectMapper.writeValue(response.getWriter(), body);
  }
}
