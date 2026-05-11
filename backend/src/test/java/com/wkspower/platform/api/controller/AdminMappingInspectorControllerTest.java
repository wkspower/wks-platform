package com.wkspower.platform.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.model.RecentSignalEntry;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.domain.service.SignalAuditRingBuffer;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Story 4.6 — slice test for {@link AdminMappingInspectorController}. Covers AC1, AC2, AC7 envelope
 * plus the {@code @PreAuthorize("hasRole('ADMIN')")} gate.
 */
@WebMvcTest(AdminMappingInspectorController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@ActiveProfiles("dev")
class AdminMappingInspectorControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private MappingRegistry mappingRegistry;
  @MockitoBean private CaseTypeVersionRegistry versionRegistry;
  @MockitoBean private SignalAuditRingBuffer ringBuffer;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private UserRepository userRepository;

  // -- AC1 happy path ------------------------------------------------------

  @Test
  void mappingInspectorReturnsDtoForKnownCaseType() throws Exception {
    AttachmentDefinition attachment =
        new AttachmentDefinition(
            "bpmn",
            "auto-loan.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.of(new EndEventMapping("draft -> underwriting")),
            Map.of(),
            List.of());
    MappingDefinition mapping = new MappingDefinition(List.of(attachment));
    when(versionRegistry.currentVersion("auto-loan")).thenReturn(Optional.of(3));
    when(mappingRegistry.resolve(any(CaseTypeRef.class), eq("3"))).thenReturn(Optional.of(mapping));

    mockMvc
        .perform(
            get("/api/admin/case-types/auto-loan/mapping-inspector")
                .with(user("admin@example.com").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.caseTypeId").value("auto-loan"))
        .andExpect(jsonPath("$.data.version").value("3"))
        .andExpect(jsonPath("$.data.emptyMapping").value(false))
        .andExpect(jsonPath("$.data.attachments[0].bpmnSource").value("auto-loan.bpmn"))
        .andExpect(jsonPath("$.data.attachments[0].elements[0].bpmnElement").value("endEvent"))
        .andExpect(jsonPath("$.data.attachments[0].elements[0].wksEffect").value("stageTransition"))
        .andExpect(
            jsonPath("$.data.attachments[0].elements[0].target").value("draft -> underwriting"));
  }

  // -- AC1 unknown caseType -> 404 ----------------------------------------

  @Test
  void mappingInspectorUnknownCaseTypeReturns404() throws Exception {
    when(versionRegistry.currentVersion("missing")).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/api/admin/case-types/missing/mapping-inspector")
                .with(user("admin@example.com").roles("ADMIN")))
        .andExpect(status().isNotFound());
  }

  // -- AC1 zero-attachment -------------------------------------------------

  @Test
  void mappingInspectorZeroAttachmentReturnsEmptyTrue() throws Exception {
    when(versionRegistry.currentVersion("simple")).thenReturn(Optional.of(1));
    when(mappingRegistry.resolve(any(CaseTypeRef.class), eq("1"))).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/api/admin/case-types/simple/mapping-inspector")
                .with(user("admin@example.com").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.emptyMapping").value(true))
        .andExpect(jsonPath("$.data.attachments").isArray())
        .andExpect(jsonPath("$.data.attachments.length()").value(0));
  }

  // -- AC2 recent-signals --------------------------------------------------

  @Test
  void recentSignalsReturnsRingBufferSnapshot() throws Exception {
    RecentSignalEntry entry =
        new RecentSignalEntry(
            Instant.parse("2026-05-09T14:23:01.234Z"),
            ExecutionSignalKind.STAGE_TRANSITION,
            "endEvent_credit_approved",
            "matched-rule",
            null,
            "stageTransition",
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            null);
    RecentSignalEntry miss =
        new RecentSignalEntry(
            Instant.parse("2026-05-09T14:22:55.012Z"),
            ExecutionSignalKind.TASK_COMPLETED,
            "userTask_review",
            "unmapped",
            null,
            null,
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            "WKS-MAP-404");
    when(ringBuffer.recent("auto-loan")).thenReturn(List.of(entry, miss));

    mockMvc
        .perform(
            get("/api/admin/case-types/auto-loan/recent-signals")
                .with(user("admin@example.com").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.caseTypeId").value("auto-loan"))
        .andExpect(jsonPath("$.data.signals.length()").value(2))
        .andExpect(jsonPath("$.data.signals[0].kind").value("STAGE_TRANSITION"))
        .andExpect(jsonPath("$.data.signals[0].decision").value("matched-rule"))
        .andExpect(jsonPath("$.data.signals[0].errorCode").doesNotExist())
        .andExpect(jsonPath("$.data.signals[1].decision").value("unmapped"))
        .andExpect(jsonPath("$.data.signals[1].errorCode").value("WKS-MAP-404"));
  }

  // -- 403 non-admin -------------------------------------------------------

  @Test
  void nonAdminReturnsForbidden() throws Exception {
    mockMvc
        .perform(
            get("/api/admin/case-types/auto-loan/mapping-inspector")
                .with(user("user@example.com").roles("USER")))
        .andExpect(status().isForbidden());

    verifyNoInteractions(versionRegistry, mappingRegistry, ringBuffer);
  }
}
