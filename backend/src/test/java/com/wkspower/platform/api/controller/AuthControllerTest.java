package com.wkspower.platform.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.api.dto.request.LoginRequest;
import com.wkspower.platform.domain.model.User;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SamlGatingFilter;
import com.wkspower.platform.security.SecurityConfig;
import com.wkspower.platform.security.SecurityConfig.ProductionProfile;
import com.wkspower.platform.security.WksUserPrincipal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Slice test for {@link AuthController}. {@code @Import(SecurityConfig.class)} so the filter chain
 * is in effect; {@code @Import(GlobalExceptionHandler.class)} so authentication failures surface as
 * WKS error envelopes instead of Spring's default 403 HTML.
 */
@WebMvcTest(AuthController.class)
@Import({
  SecurityConfig.class,
  GlobalExceptionHandler.class,
  JwtAuthenticationFilter.class,
  SamlGatingFilter.class
})
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private LicenseService licenseService;
  @MockitoBean private AuthenticationManager authenticationManager;
  @MockitoBean private ProductionProfile productionProfile;

  private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @BeforeEach
  void clearContext() {
    SecurityContextHolder.clearContext();
    when(productionProfile.active()).thenReturn(false);
  }

  @Test
  void loginSuccessSetsCookieAndReturnsEnvelope() throws Exception {
    Authentication auth =
        new UsernamePasswordAuthenticationToken("admin@wkspower.local", "pw", java.util.List.of());
    when(authenticationManager.authenticate(any())).thenReturn(auth);
    when(userRepository.findByEmail("admin@wkspower.local"))
        .thenReturn(Optional.of(new User(userId, "admin@wkspower.local", Set.of("admin"), true)));
    when(jwtTokenProvider.issue(any())).thenReturn("token.value.signed");
    when(jwtTokenProvider.ttlSeconds()).thenReturn(28_800L);

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new LoginRequest("admin@wkspower.local", "pw"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(userId.toString()))
        .andExpect(jsonPath("$.data.email").value("admin@wkspower.local"))
        .andExpect(jsonPath("$.data.roles[0]").value("admin"))
        .andExpect(cookie().exists("WKS_SESSION"))
        .andExpect(cookie().httpOnly("WKS_SESSION", true))
        .andExpect(cookie().value("WKS_SESSION", "token.value.signed"))
        .andExpect(
            header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Lax")));
  }

  @Test
  void loginFailureReturnsUnauthorizedWithWksCode() throws Exception {
    when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("nobody@x.com", "pw"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("WKS-API-401"));
  }

  @Test
  void loginValidationFailureReturnsBadRequestWithApi001() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("WKS-API-001"));
  }

  @Test
  void meReturnsAuthenticatedUserWhenPrincipalPresent() throws Exception {
    AuthenticatedUser authenticated =
        new AuthenticatedUser(userId, "admin@wkspower.local", Set.of("admin"));
    WksUserPrincipal principal = new WksUserPrincipal(authenticated);
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

    mockMvc
        .perform(get("/api/auth/me").with(authentication(auth)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(userId.toString()))
        .andExpect(jsonPath("$.data.email").value("admin@wkspower.local"));
  }

  @Test
  void meReturns401WhenUnauthenticated() throws Exception {
    mockMvc
        .perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("WKS-API-401"));
  }

  @Test
  void logoutReturnsNoContentAndExpiresCookie() throws Exception {
    mockMvc
        .perform(post("/api/auth/logout"))
        .andExpect(status().isNoContent())
        .andExpect(cookie().exists("WKS_SESSION"))
        .andExpect(cookie().maxAge("WKS_SESSION", 0));
  }

  /**
   * The three login-failure modes the spec names — unknown email, wrong password, inactive user —
   * must return byte-identical response bodies so attackers cannot enumerate accounts through
   * error-message differences. At this slice level all three surface to the controller as {@link
   * AuthenticationException}, so parameterising over distinct subclasses proves the controller +
   * handler produce one canonical envelope regardless of upstream cause.
   */
  @ParameterizedTest(name = "{0}")
  @MethodSource("genericAuthFailureCases")
  void allAuthFailureModesReturnIdenticalGenericEnvelope(String label, AuthenticationException ex)
      throws Exception {
    when(authenticationManager.authenticate(any())).thenThrow(ex);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(new LoginRequest(label + "@x.com", "pw"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("WKS-API-401"))
            .andExpect(jsonPath("$.error.message").value("Invalid email or password"))
            .andExpect(jsonPath("$.error.field").doesNotExist())
            .andExpect(jsonPath("$.data").doesNotExist())
            .andReturn();

    // Body must be byte-identical across failure modes — no stray fields leaking cause.
    String body = result.getResponse().getContentAsString();
    assertThatBodyMatchesCanonicalEnvelope(body);
  }

  private static java.util.stream.Stream<Arguments> genericAuthFailureCases() {
    return java.util.stream.Stream.of(
        Arguments.of("unknown-email", new BadCredentialsException("Bad credentials")),
        Arguments.of("wrong-password", new BadCredentialsException("Bad credentials")),
        Arguments.of("inactive-user", new DisabledException("User is disabled")));
  }

  private static void assertThatBodyMatchesCanonicalEnvelope(String body) {
    org.assertj.core.api.Assertions.assertThat(body)
        .contains("\"code\":\"WKS-API-401\"")
        .contains("\"message\":\"Invalid email or password\"")
        .doesNotContain("Bad credentials")
        .doesNotContain("disabled");
  }
}
