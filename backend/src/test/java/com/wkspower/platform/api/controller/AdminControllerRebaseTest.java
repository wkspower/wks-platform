package com.wkspower.platform.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.FieldAction;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.FieldMapping;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.StatusAction;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport.StatusMapping;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.CaseRebaseService;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Story 3.9 AC1/AC2/AC3/AC4 — slice tests for the rebase endpoints in {@link AdminController}.
 * Minimum 4 cases per Task 5.2.
 */
@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@ActiveProfiles("dev")
class AdminControllerRebaseTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ConfigService configService;
  @MockitoBean private CaseRebaseService caseRebaseService;
  @MockitoBean private CaseTypeVersionRegistry versionRegistry;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private LicenseService licenseService;

  private static final UUID CASE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final String CT_ID = "bfsi-kyc";

  private static CaseRebaseReport cleanDryRunReport(boolean applied) {
    return new CaseRebaseReport(
        CASE_ID,
        CT_ID,
        2,
        3,
        applied,
        List.of(new FieldMapping("name", FieldAction.KEEP, "TEXT", "TEXT", null)),
        List.of(new StatusMapping("open", StatusAction.KEEP, null)),
        List.of());
  }

  // ---- Case 1: GET dry-run 200 ----

  @Test
  void dryRun_asAdmin_returns200() throws Exception {
    when(caseRebaseService.dryRun(eq(CT_ID), eq(CASE_ID), eq(3)))
        .thenReturn(cleanDryRunReport(false));

    mockMvc
        .perform(
            get("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "3")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.caseId").value(CASE_ID.toString()))
        .andExpect(jsonPath("$.data.caseTypeId").value(CT_ID))
        .andExpect(jsonPath("$.data.fromVersion").value(2))
        .andExpect(jsonPath("$.data.toVersion").value(3))
        .andExpect(jsonPath("$.data.applied").value(false))
        .andExpect(jsonPath("$.data.irreconcilable").isArray())
        .andExpect(jsonPath("$.data.irreconcilable").isEmpty());
  }

  // ---- Case 2: POST apply 200 ----

  @Test
  void apply_asAdmin_returns200WithAppliedTrue() throws Exception {
    when(caseRebaseService.apply(
            eq(CT_ID), eq(CASE_ID), eq(3), anyString(), nullable(String.class), any()))
        .thenReturn(cleanDryRunReport(true));

    mockMvc
        .perform(
            post("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "3")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.applied").value(true))
        .andExpect(jsonPath("$.data.fromVersion").value(2))
        .andExpect(jsonPath("$.data.toVersion").value(3));
  }

  // ---- Case 3: POST apply 422 + WKS-CFG-034 ----

  @Test
  void apply_withIrreconcilable_returns422WithCfg034() throws Exception {
    when(caseRebaseService.apply(
            eq(CT_ID), eq(CASE_ID), eq(3), anyString(), nullable(String.class), any()))
        .thenThrow(
            new WksConfigException(
                List.of(
                    ErrorDetail.of(
                        ErrorCode.WKS_CFG_034.wire(),
                        "Rebase aborted — 1 irreconcilable item(s) require manual decision."
                            + " Inspect dry-run report and resolve before retrying."))));

    mockMvc
        .perform(
            post("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "3")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.errors[0].code").value("WKS-CFG-034"));
  }

  // ---- Case 4: GET reverse-version 422 + WKS-API-007 ----

  @Test
  void dryRun_reverseVersion_returns422WithApi007() throws Exception {
    when(caseRebaseService.dryRun(eq(CT_ID), eq(CASE_ID), eq(1)))
        .thenThrow(
            new WksConfigException(
                List.of(
                    ErrorDetail.of(
                        ErrorCode.WKS_API_007.wire(),
                        "toVersion must be strictly greater than current caseTypeVersion"
                            + " (current=2, requested=1)"))));

    mockMvc
        .perform(
            get("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "1")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.errors[0].code").value("WKS-API-007"))
        .andExpect(
            jsonPath("$.error.errors[0].message")
                .value(org.hamcrest.Matchers.containsString("strictly greater")));
  }

  // ---- Case 5: case not found → 404 ----

  @Test
  void dryRun_caseNotFound_returns404() throws Exception {
    when(caseRebaseService.dryRun(any(), any(), anyInt()))
        .thenThrow(new WksNotFoundException("Case not found"));

    mockMvc
        .perform(
            get("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "3")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isNotFound());
  }

  // ---- Case 6: unauthenticated → 401 ----

  @Test
  void dryRun_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "3"))
        .andExpect(status().isUnauthorized());
  }
}
