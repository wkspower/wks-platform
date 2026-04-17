package com.wkspower.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads the {@code WKS_SESSION} cookie, validates the JWT via {@link JwtTokenProvider}, and
 * populates {@link SecurityContextHolder} on success. On any failure the chain continues with an
 * empty context, letting Spring Security's {@code authenticated()} rule produce a 401 — this filter
 * never throws.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String COOKIE_NAME = "WKS_SESSION";

  private final JwtTokenProvider tokenProvider;

  public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String token = extractToken(request);
    if (token != null) {
      tokenProvider
          .parse(token)
          .ifPresent(
              authenticated -> {
                WksUserPrincipal principal = new WksUserPrincipal(authenticated);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
              });
    }
    chain.doFilter(request, response);
  }

  private static String extractToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (COOKIE_NAME.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
