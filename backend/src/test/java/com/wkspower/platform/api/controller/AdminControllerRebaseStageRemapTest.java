package com.wkspower.platform.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.CaseRebaseService;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Story 3.9.1 AC-1 — controller slice tests for the stageRemap body extension on the rebase apply
 * endpoint. Tests the AC-1 requirement that omitting stageRemap preserves Story 3.9 behavior
 * (dangling stages still irreconcilable → WKS-CFG-034), and that supplying stageRemap passes it
 * through to the service.
 *
 * <p>LicenseService mock pattern: N=5 occurrence per Story 3.9.1 Dev Notes. The verbatim
 * {@code @MockitoBean LicenseService} pattern from PR #422/#431/#432 is applied here since Story
 * 7-6 has not yet landed {@code @SecurityConfigTestSupport} (merge order: 3-9-1 precedes 7-6).
 */
@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@ActiveProfiles("dev")
class AdminControllerRebaseStageRemapTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ConfigService configService;
  @MockitoBean private CaseRebaseService caseRebaseService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private LicenseService licenseService;

  private static final UUID CASE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final String CT_ID = "bfsi-kyc";

  private static CaseRebaseReport appliedReport() {
    return new CaseRebaseReport(
        CASE_ID,
        CT_ID,
        1,
        2,
        true,
        List.of(new FieldMapping("name", FieldAction.KEEP, "TEXT", "TEXT", null)),
        List.of(new StatusMapping("open", StatusAction.KEEP, null)),
        List.of());
  }

  // ---- AC-1: omitted stageRemap preserves Story 3.9 behavior (WKS-CFG-034 on irreconcilable) ----

  @Test
  void omittedStageRemap_preservesStory3_9Behavior() throws Exception {
    when(caseRebaseService.apply(
            eq(CT_ID), eq(CASE_ID), eq(2), anyString(), nullable(String.class), eq(Map.of())))
        .thenThrow(
            new WksConfigException(
                List.of(
                    ErrorDetail.of(
                        ErrorCode.WKS_CFG_034.wire(),
                        "Rebase aborted — 1 irreconcilable item(s) require manual decision."))));

    mockMvc
        .perform(
            post("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "2")
                // no body → stageRemap defaults to empty → Story 3.9 irreconcilable path
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.errors[0].code").value("WKS-CFG-034"));
  }

  // ---- AC-1: stageRemap supplied → passes to service → apply success ----

  @Test
  void suppliedStageRemap_passedToService_returnsApplied() throws Exception {
    when(caseRebaseService.apply(
            eq(CT_ID),
            eq(CASE_ID),
            eq(2),
            anyString(),
            nullable(String.class),
            eq(Map.of("underwriting", "review"))))
        .thenReturn(appliedReport());

    mockMvc
        .perform(
            post("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stageRemap\":{\"underwriting\":\"review\"}}")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.applied").value(true));
  }

  // ---- AC-2: WKS-CFG-036 from invalid stageRemap from-key ----

  @Test
  void invalidFromKey_returns422_WKS_CFG_036() throws Exception {
    when(caseRebaseService.apply(
            eq(CT_ID), eq(CASE_ID), eq(2), anyString(), nullable(String.class), any()))
        .thenThrow(
            new WksConfigException(
                List.of(
                    ErrorDetail.of(
                        ErrorCode.WKS_CFG_036.wire(),
                        "stageRemap key 'bad-stage' is not present in fromVersion.stages")),
                Map.of("key", "bad-stage", "reason", "not present in fromVersion.stages")));

    mockMvc
        .perform(
            post("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stageRemap\":{\"bad-stage\":\"review\"}}")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.errors[0].code").value("WKS-CFG-036"));
  }

  // ---- AC-3: WKS-CFG-037 from invalid stageRemap to-value ----

  @Test
  void invalidToValue_returns422_WKS_CFG_037() throws Exception {
    when(caseRebaseService.apply(
            eq(CT_ID), eq(CASE_ID), eq(2), anyString(), nullable(String.class), any()))
        .thenThrow(
            new WksConfigException(
                List.of(
                    ErrorDetail.of(
                        ErrorCode.WKS_CFG_037.wire(),
                        "stageRemap value 'bad-target' for key 'underwriting' is not present in"
                            + " toVersion.stages")),
                Map.of(
                    "from",
                    "underwriting",
                    "to",
                    "bad-target",
                    "reason",
                    "not present in toVersion.stages")));

    mockMvc
        .perform(
            post("/api/admin/case-types/{caseTypeId}/cases/{caseId}/rebase", CT_ID, CASE_ID)
                .param("to", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stageRemap\":{\"underwriting\":\"bad-target\"}}")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.errors[0].code").value("WKS-CFG-037"));
  }
}
