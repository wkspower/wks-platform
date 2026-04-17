package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.api.dto.response.AuthUserDto;
import com.wkspower.platform.domain.exception.WksAuthenticationException;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig.ProductionProfile;
import com.wkspower.platform.security.WksUserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints. Thin — auth goes through {@link AuthenticationManager}; JWT creation
 * goes through {@link JwtTokenProvider}. The controller never touches password hashes directly.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final String COOKIE_NAME = "WKS_SESSION";

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider tokenProvider;
  private final UserRepository userRepository;
  private final boolean productionProfileActive;

  public AuthController(
      AuthenticationManager authenticationManager,
      JwtTokenProvider tokenProvider,
      UserRepository userRepository,
      ProductionProfile productionProfile) {
    this.authenticationManager = authenticationManager;
    this.tokenProvider = tokenProvider;
    this.userRepository = userRepository;
    this.productionProfileActive = productionProfile.active();
  }

  @PostMapping("/login")
  // TODO: rate-limit (Phase 1) — tracked in Story 1.2 PR (link in story 1-2 review findings); JWT
  // in HttpOnly cookie + SameSite=Lax mitigates the cross-site abuse surface; brute-force
  // throttling and Argon2 CPU-DoS protection come with admin controls.
  public ResponseEntity<ApiResponse<AuthUserDto>> login(@Valid @RequestBody LoginRequest request) {
    String email = request.email() == null ? null : request.email().strip();
    Authentication authentication;
    try {
      authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(email, request.password()));
    } catch (AuthenticationException ex) {
      throw new WksAuthenticationException();
    }

    User user =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(WksAuthenticationException::new);

    String token = tokenProvider.issue(user);
    ResponseCookie cookie = sessionCookie(token, tokenProvider.ttlSeconds());
    AuthUserDto payload = new AuthUserDto(user.id().toString(), user.email(), user.roles());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success(payload));
  }

  @GetMapping("/me")
  public ApiResponse<AuthUserDto> currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof WksUserPrincipal principal)) {
      throw new WksAuthenticationException();
    }
    return ApiResponse.success(
        new AuthUserDto(
            principal.id().toString(),
            principal.authenticated().email(),
            principal.authenticated().roles()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    ResponseCookie expired = sessionCookie("", 0);
    response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
    return ResponseEntity.noContent().build();
  }

  private ResponseCookie sessionCookie(String value, long maxAgeSeconds) {
    return ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true)
        .secure(productionProfileActive)
        .sameSite("Lax")
        .path("/")
        .maxAge(maxAgeSeconds)
        .build();
  }
}
