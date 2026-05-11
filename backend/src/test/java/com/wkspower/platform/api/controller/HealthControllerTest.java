package com.wkspower.platform.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SamlGatingFilter;
import com.wkspower.platform.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test for the health endpoint. With {@code spring-boot-starter-security} on the classpath
 * (Story 1.2), {@code @WebMvcTest} requires {@code @Import(SecurityConfig.class)} so the filter
 * chain is applied — otherwise every endpoint would resolve as {@code permitAll} and the test would
 * not prove what it claims.
 */
@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SamlGatingFilter.class})
class HealthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private LicenseService licenseService;

  @Test
  void healthReturnsEnvelopeWithVersionAndUptime() throws Exception {
    mockMvc
        .perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.version").isString())
        .andExpect(jsonPath("$.data.version").isNotEmpty())
        .andExpect(jsonPath("$.data.uptime").isString())
        .andExpect(jsonPath("$.data.uptime").isNotEmpty())
        .andExpect(jsonPath("$.error").doesNotExist())
        .andExpect(jsonPath("$.meta").exists());
  }
}
