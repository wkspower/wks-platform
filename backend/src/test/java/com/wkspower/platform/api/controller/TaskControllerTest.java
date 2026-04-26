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
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.TaskService;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SecurityConfig;
import com.wkspower.platform.security.WksUserPrincipal;
import java.time.Instant;
import java.util.List;
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

/**
 * Slice test for {@link TaskController}. Covers complete + claim contract incl. all error codes.
 */
@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class})
@ActiveProfiles("dev")
class TaskControllerTest {

  private static final UUID ACTOR_ID = UUID.randomUUID();
  private static final UUID CASE_ID = UUID.randomUUID();
  private static final Instant NOW = Instant.parse("2026-04-26T10:00:00Z");

  @Autowired MockMvc mockMvc;

  @MockitoBean(name = "wksTaskService")
  TaskService taskService;

  @MockitoBean(name = "caseTypePermissionEvaluator")
  CaseTypePermissionEvaluator evaluator;

  @MockitoBean Clock clock;

  @MockitoBean JwtTokenProvider jwtTokenProvider;
  @MockitoBean UserRepository userRepository;

  // ---- POST /api/tasks/{id}/complete -------------------------------------

  @Test
  void completeReturns200AndCarriesArchetype() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("draft_section", null));
    when(evaluator.hasVerb(any(), eq("loan-application"), eq("transition"))).thenReturn(true);
    when(taskService.complete(eq("t1"), any(), eq(ACTOR_ID)))
        .thenReturn(sampleTask("draft_section", null));
    when(clock.now()).thenReturn(NOW);

    mockMvc
        .perform(
            post("/api/tasks/t1/complete")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"variables\":{\"approved\":true}}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.taskId").value("t1"))
        .andExpect(jsonPath("$.data.archetype").value("draft_section"))
        .andExpect(jsonPath("$.data.caseId").value(CASE_ID.toString()));
  }

  @Test
  void completeWithEmptyBodyWorks() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("submit_for_processing", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(taskService.complete(eq("t1"), any(), any()))
        .thenReturn(sampleTask("submit_for_processing", null));
    when(clock.now()).thenReturn(NOW);

    mockMvc
        .perform(post("/api/tasks/t1/complete").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.archetype").value("submit_for_processing"));
  }

  @Test
  void completeReturns403WhenVerbDenied() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("draft_section", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(false);

    mockMvc
        .perform(post("/api/tasks/t1/complete").with(officerAuth()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("WKS-API-403"));
  }

  @Test
  void completeReturns404WhenTaskUnknown() throws Exception {
    when(taskService.findById("nope")).thenThrow(new WksNotFoundException("task missing"));

    mockMvc
        .perform(post("/api/tasks/nope/complete").with(officerAuth()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("WKS-API-404"));
  }

  @Test
  void completeReturns409WhenEngineConflict() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("draft_section", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(taskService.complete(eq("t1"), any(), any()))
        .thenThrow(new WksConflictException("already completed"));

    mockMvc
        .perform(post("/api/tasks/t1/complete").with(officerAuth()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("WKS-RTM-409"));
  }

  @Test
  void completeReturnsArchetypeForBusinessFinal() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("business_final", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(taskService.complete(eq("t1"), any(), any()))
        .thenReturn(sampleTask("business_final", null));
    when(clock.now()).thenReturn(NOW);

    mockMvc
        .perform(post("/api/tasks/t1/complete").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.archetype").value("business_final"));
  }

  // ---- POST /api/tasks/{id}/claim ----------------------------------------

  @Test
  void claimReturns200WithAssignee() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("draft_section", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(taskService.claim(eq("t1"), eq(ACTOR_ID)))
        .thenReturn(sampleTask("draft_section", ACTOR_ID));
    when(clock.now()).thenReturn(NOW);

    mockMvc
        .perform(post("/api/tasks/t1/claim").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.assignee").value(ACTOR_ID.toString()));
  }

  @Test
  void claimSurfacesSubmitForProcessingArchetype() throws Exception {
    // Story 2.4 Task 6.3 — every archetype must propagate on the claim response.
    when(taskService.findById("t1")).thenReturn(sampleTask("submit_for_processing", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(taskService.claim(eq("t1"), eq(ACTOR_ID)))
        .thenReturn(sampleTask("submit_for_processing", ACTOR_ID));
    when(clock.now()).thenReturn(NOW);

    mockMvc
        .perform(post("/api/tasks/t1/claim").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.archetype").value("submit_for_processing"));
  }

  @Test
  void claimSurfacesBusinessFinalArchetype() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("business_final", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(taskService.claim(eq("t1"), eq(ACTOR_ID)))
        .thenReturn(sampleTask("business_final", ACTOR_ID));
    when(clock.now()).thenReturn(NOW);

    mockMvc
        .perform(post("/api/tasks/t1/claim").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.archetype").value("business_final"));
  }

  @Test
  void claimReturns409WhenAlreadyClaimed() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("draft_section", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(taskService.claim(eq("t1"), any())).thenThrow(new WksConflictException("already claimed"));

    mockMvc
        .perform(post("/api/tasks/t1/claim").with(officerAuth()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("WKS-RTM-409"));
  }

  @Test
  void claimReturns404WhenUnknownTask() throws Exception {
    when(taskService.findById("nope")).thenThrow(new WksNotFoundException("task missing"));

    mockMvc
        .perform(post("/api/tasks/nope/claim").with(officerAuth()))
        .andExpect(status().isNotFound());
  }

  // ---- helpers -----------------------------------------------------------

  private static Task sampleTask(String archetype, UUID assignee) {
    return new Task(
        "t1",
        "pi-1",
        CASE_ID,
        "loan-application",
        "draft",
        "Draft",
        assignee,
        archetype,
        NOW,
        null);
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
