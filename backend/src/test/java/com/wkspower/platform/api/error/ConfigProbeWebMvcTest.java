package com.wkspower.platform.api.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Slice test covering the multi-error envelope path — asserts HTTP 422, umbrella code {@code
 * WKS-CFG-000}, and that every inner error surfaces in {@code error.errors[]} (never
 * fail-on-first).
 */
@WebMvcTest(
    controllers = ConfigProbeController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters =
        @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ConfigProbeWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void multiErrorAggregateReturns422WithFullEnvelope() throws Exception {
    mockMvc
        .perform(get("/_test/config-probe"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.code").value("WKS-CFG-000"))
        .andExpect(jsonPath("$.error.message").value("Configuration invalid"))
        .andExpect(jsonPath("$.error.field").doesNotExist())
        .andExpect(jsonPath("$.error.errors", org.hamcrest.Matchers.hasSize(3)))
        .andExpect(jsonPath("$.error.errors[0].code").value("WKS-CFG-101"))
        .andExpect(jsonPath("$.error.errors[0].field").value("name"))
        .andExpect(jsonPath("$.error.errors[0].line").value(3))
        .andExpect(jsonPath("$.error.errors[1].code").value("WKS-CFG-102"))
        .andExpect(jsonPath("$.error.errors[1].line").value(17))
        .andExpect(jsonPath("$.error.errors[2].code").value("WKS-CFG-103"))
        .andExpect(jsonPath("$.error.errors[2].field").value("roles[0]"))
        .andExpect(jsonPath("$.error.errors[2].line").doesNotExist())
        .andExpect(jsonPath("$.data").doesNotExist());
  }
}
