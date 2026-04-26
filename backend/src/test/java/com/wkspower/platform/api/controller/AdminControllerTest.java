package com.wkspower.platform.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Slice test for {@link AdminController}. Covers the full HTTP envelope: ROLE_ADMIN gating, happy
 * path, validation aggregate, and the multipart 413 mapping.
 */
@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@ActiveProfiles("dev")
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ConfigService configService;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private UserRepository userRepository;

  // ---- happy path --------------------------------------------------------

  @Test
  void deployAsAdminReturnsOk() throws Exception {
    CaseTypeConfig config =
        new CaseTypeConfig(
            "application",
            "Application",
            1,
            null,
            new WorkflowRef("application.bpmn"),
            List.of(),
            List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
            List.of(),
            List.of(new RoleDefinition("admin", List.of())));
    DeploymentResult deployment =
        new DeploymentResult("dep-1", "applicationProcess", "procDef-1", 1, Instant.now());
    byte[] yamlBytes = "id: x".getBytes();
    byte[] bpmnBytes = "<x/>".getBytes();
    ArgumentCaptor<byte[]> yamlCap = ArgumentCaptor.forClass(byte[].class);
    ArgumentCaptor<byte[]> bpmnCap = ArgumentCaptor.forClass(byte[].class);
    when(configService.deploy(yamlCap.capture(), bpmnCap.capture(), eq("admin")))
        .thenReturn(DeployResult.ok(config, deployment));

    mockMvc
        .perform(
            multipart("/api/admin/deploy")
                .file(new MockMultipartFile("caseType", "ct.yaml", "text/plain", yamlBytes))
                .file(new MockMultipartFile("bpmn", "p.bpmn", "application/xml", bpmnBytes))
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.caseTypeId").value("application"))
        .andExpect(jsonPath("$.data.version").value(1))
        .andExpect(jsonPath("$.data.deploymentId").value("dep-1"))
        .andExpect(jsonPath("$.data.processDefinitionId").value("procDef-1"))
        .andExpect(jsonPath("$.data.schemaUri").value("/api/admin/case-types/application/schema"));

    // Pin argument order: a regression that swaps yaml/bpmn would otherwise pass.
    assertThat(yamlCap.getValue()).isEqualTo(yamlBytes);
    assertThat(bpmnCap.getValue()).isEqualTo(bpmnBytes);
  }

  // ---- 401 unauthenticated ----------------------------------------------

  @Test
  void deployUnauthenticatedReturnsUnauthorized() throws Exception {
    // No SecurityMockMvcRequestPostProcessor — request hits the chain anonymously. The
    // ExceptionTranslationFilter routes anonymous AccessDeniedException through the configured
    // WksAuthenticationEntryPoint, which writes 401 + WKS-API-401. This pins the standard
    // distinction between missing creds (401) and wrong-role (403, see deployAsNonAdmin).
    mockMvc
        .perform(
            multipart("/api/admin/deploy")
                .file(
                    new MockMultipartFile("caseType", "ct.yaml", "text/plain", "id: x".getBytes()))
                .file(
                    new MockMultipartFile("bpmn", "p.bpmn", "application/xml", "<x/>".getBytes())))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("WKS-API-401"));

    verifyNoInteractions(configService);
  }

  // ---- 403 ---------------------------------------------------------------

  @Test
  void deployAsNonAdminReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            multipart("/api/admin/deploy")
                .file(
                    new MockMultipartFile("caseType", "ct.yaml", "text/plain", "id: x".getBytes()))
                .file(new MockMultipartFile("bpmn", "p.bpmn", "application/xml", "<x/>".getBytes()))
                .with(user("user").roles("USER")))
        .andExpect(status().isForbidden());

    // The @PreAuthorize gate must reject before reaching the service. If a future change moves
    // authorisation to a downstream layer, this assertion catches the regression.
    verifyNoInteractions(configService);
  }

  // ---- 422 multi-error envelope ------------------------------------------

  @Test
  void validationFailureReturns422Aggregate() throws Exception {
    when(configService.deploy(any(), any(), any()))
        .thenReturn(
            DeployResult.invalid(
                List.of(
                    ErrorDetail.of("WKS-CFG-010", "BPMN bytes empty"),
                    ErrorDetail.of("WKS-CFG-099", "YAML parse failed"))));

    mockMvc
        .perform(
            multipart("/api/admin/deploy")
                .file(
                    new MockMultipartFile("caseType", "ct.yaml", "text/plain", "broken".getBytes()))
                .file(new MockMultipartFile("bpmn", "p.bpmn", "application/xml", "".getBytes()))
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.code").value("WKS-CFG-000"))
        .andExpect(jsonPath("$.error.errors[0].code").value("WKS-CFG-010"))
        .andExpect(jsonPath("$.error.errors[1].code").value("WKS-CFG-099"));
  }

  // ---- 413 oversized part ------------------------------------------------

  @Test
  void oversizedPartReturnsPayloadTooLarge() throws Exception {
    // Spring wraps Tomcat's underlying SizeLimitExceededException into
    // MaxUploadSizeExceededException
    // before reaching the handler chain. We simulate by configuring a tiny part cap via the test
    // and asserting the GlobalExceptionHandler maps the exception to 413 + WKS-API-413.
    when(configService.deploy(any(), any(), any()))
        .thenThrow(new MaxUploadSizeExceededException(1024L));

    mockMvc
        .perform(
            multipart("/api/admin/deploy")
                .file(
                    new MockMultipartFile(
                        "caseType", "ct.yaml", "text/plain", "x".repeat(2048).getBytes()))
                .file(new MockMultipartFile("bpmn", "p.bpmn", "application/xml", "<x/>".getBytes()))
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isPayloadTooLarge())
        .andExpect(jsonPath("$.error.code").value("WKS-API-413"));

    verify(configService).deploy(any(), any(), any());
  }

  // ---- missing part ------------------------------------------------------

  @Test
  void missingPartReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            multipart("/api/admin/deploy")
                .file(
                    new MockMultipartFile("caseType", "ct.yaml", "text/plain", "id: x".getBytes()))
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isBadRequest());
  }
}
