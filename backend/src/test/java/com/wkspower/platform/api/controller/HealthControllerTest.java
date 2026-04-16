package com.wkspower.platform.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

  @Autowired private MockMvc mockMvc;

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
