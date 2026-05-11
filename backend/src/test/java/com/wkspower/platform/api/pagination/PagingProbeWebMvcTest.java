package com.wkspower.platform.api.pagination;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.SamlGatingFilter;
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
 * End-to-end slice test for {@link PageRequestParams}: validates envelope shape for success ({@code
 * meta.total|page|size}) and error-code strings ({@code WKS-API-003}, {@code WKS-API-004}, {@code
 * WKS-API-005}) through a real Spring MVC stack.
 *
 * <p>{@code SecurityAutoConfiguration} is excluded — this test targets the MVC + envelope path, not
 * auth. {@link GlobalExceptionHandler} is imported explicitly so the error handler chain is
 * exercised end-to-end.
 */
@WebMvcTest(
    controllers = PagingProbeController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters =
        @Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {JwtAuthenticationFilter.class, SamlGatingFilter.class}))
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PagingProbeWebMvcTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void defaultsProducePopulatedMeta() throws Exception {
    mockMvc
        .perform(get("/_test/paging-probe"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(3)))
        .andExpect(jsonPath("$.meta.total").value(42))
        .andExpect(jsonPath("$.meta.page").value(0))
        .andExpect(jsonPath("$.meta.size").value(20))
        .andExpect(jsonPath("$.error").doesNotExist());
  }

  @Test
  void explicitPageAndSizeAreReflected() throws Exception {
    mockMvc
        .perform(get("/_test/paging-probe").param("page", "3").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.page").value(3))
        .andExpect(jsonPath("$.meta.size").value(5));
  }

  @Test
  void sizeAbove100IsClamped() throws Exception {
    mockMvc
        .perform(get("/_test/paging-probe").param("size", "500"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.size").value(100));
  }

  @Test
  void sizeZeroReturns400WithWksApi003() throws Exception {
    mockMvc
        .perform(get("/_test/paging-probe").param("size", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("WKS-API-003"))
        .andExpect(jsonPath("$.error.field").value("size"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void negativePageReturns400WithWksApi003() throws Exception {
    mockMvc
        .perform(get("/_test/paging-probe").param("page", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("WKS-API-003"))
        .andExpect(jsonPath("$.error.field").value("page"));
  }

  @Test
  void unknownSortPropertyReturns400WithWksApi004() throws Exception {
    mockMvc
        .perform(get("/_test/paging-probe").param("sort", "mysteryField,desc"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("WKS-API-004"))
        .andExpect(jsonPath("$.error.field").value("sort"));
  }

  @Test
  void unknownSortDirectionReturns400WithWksApi005() throws Exception {
    mockMvc
        .perform(get("/_test/paging-probe").param("sort", "updatedAt,sideways"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("WKS-API-005"));
  }
}
