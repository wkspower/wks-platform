package com.wkspower.platform.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import com.wkspower.platform.security.WksUserPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Slice test for {@link CaseController#transition}. Covers Story 2.4 AC1 contract. */
@WebMvcTest(CaseController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@ActiveProfiles("dev")
class CaseTransitionControllerTest {

  private static final UUID ACTOR_ID = UUID.randomUUID();
  private static final Instant NOW = Instant.parse("2026-04-26T10:00:00Z");

  @Autowired MockMvc mockMvc;

  @MockitoBean(name = "wksCaseService")
  CaseService caseService;

  @MockitoBean(name = "wksTaskService")
  com.wkspower.platform.domain.service.TaskService taskService;

  @MockitoBean(name = "caseTypePermissionEvaluator")
  CaseTypePermissionEvaluator evaluator;

  @MockitoBean JwtTokenProvider jwtTokenProvider;
  @MockitoBean UserRepository userRepository;

  @Test
  void transitionReturns200WithUpdatedCase() throws Exception {
    Case existing = sampleCase("open");
    Case advanced = withStatus(existing, "review");
    when(caseService.findById(existing.id())).thenReturn(existing);
    when(evaluator.hasVerb(any(), eq("loan-application"), eq("transition"))).thenReturn(true);
    when(caseService.transition(eq(existing.id()), eq("submit"), any(), eq(ACTOR_ID)))
        .thenReturn(advanced);
    when(caseService.requireCaseType(eq("loan-application"))).thenReturn(loanType());

    mockMvc
        .perform(
            post("/api/cases/" + existing.id() + "/transition")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"action\":\"submit\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("review"));
  }

  @Test
  void transitionReturns403WhenVerbDenied() throws Exception {
    Case existing = sampleCase("open");
    when(caseService.findById(existing.id())).thenReturn(existing);
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(false);

    mockMvc
        .perform(
            post("/api/cases/" + existing.id() + "/transition")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"action\":\"submit\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("WKS-API-403"));
  }

  @Test
  void transitionReturns404WhenCaseUnknown() throws Exception {
    UUID id = UUID.randomUUID();
    when(caseService.findById(id)).thenThrow(new WksNotFoundException("not found"));

    mockMvc
        .perform(
            post("/api/cases/" + id + "/transition")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"action\":\"submit\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void transitionReturns409WhenEngineRejects() throws Exception {
    Case existing = sampleCase("open");
    when(caseService.findById(existing.id())).thenReturn(existing);
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(caseService.transition(eq(existing.id()), anyString(), any(), any()))
        .thenThrow(new WksConflictException("no enabled receiver"));

    mockMvc
        .perform(
            post("/api/cases/" + existing.id() + "/transition")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"action\":\"unknown\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("WKS-RTM-409"));
  }

  @Test
  void transitionReturns400WhenActionMissing() throws Exception {
    Case existing = sampleCase("open");
    when(caseService.findById(existing.id())).thenReturn(existing);
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);

    mockMvc
        .perform(
            post("/api/cases/" + existing.id() + "/transition")
                .with(officerAuth())
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ---- helpers -----------------------------------------------------------

  private static Case sampleCase(String status) {
    return new Case(
        UUID.randomUUID(),
        "loan-application",
        1,
        status,
        null,
        Map.of("name", "Asha"),
        "pi-1",
        NOW,
        ACTOR_ID,
        NOW,
        0L);
  }

  private static Case withStatus(Case c, String status) {
    return new Case(
        c.id(),
        c.caseTypeId(),
        c.caseTypeVersion(),
        status,
        c.assignee(),
        c.data(),
        c.processInstanceId(),
        c.createdAt(),
        c.createdBy(),
        c.updatedAt(),
        c.version());
  }

  private static CaseTypeConfig loanType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(
            new StatusDefinition("open", "Open", StatusColor.ZINC),
            new StatusDefinition("review", "Review", StatusColor.AMBER)),
        List.of("name"),
        List.of(
            new RoleDefinition(
                "officer", List.of(Permission.VIEW, Permission.CREATE, Permission.TRANSITION))));
  }

  private static org.springframework.test.web.servlet.request.RequestPostProcessor officerAuth() {
    AuthenticatedUser user = new AuthenticatedUser(ACTOR_ID, "officer@x", Set.of("officer"));
    WksUserPrincipal principal = new WksUserPrincipal(user);
    Authentication auth =
        new UsernamePasswordAuthenticationToken(
            principal,
            principal.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_officer")));
    return authentication(auth);
  }
}
