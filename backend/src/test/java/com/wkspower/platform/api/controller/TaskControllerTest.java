package com.wkspower.platform.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.CrossCaseTaskListResult;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.domain.service.TaskService;
import java.util.Collection;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SamlGatingFilter;
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
@Import({
  SecurityConfig.class,
  GlobalExceptionHandler.class,
  JwtAuthenticationFilter.class,
  SamlGatingFilter.class
})
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

  @MockitoBean CaseTypeReader caseTypeReader;

  @MockitoBean Clock clock;

  @MockitoBean JwtTokenProvider jwtTokenProvider;
  @MockitoBean UserRepository userRepository;
  @MockitoBean LicenseService licenseService;

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
  void completeReturns409WhenTaskAlreadyCompleted() throws Exception {
    // Story 2.8 AC5 — a task that disappeared between client load and the complete click
    // (typically: another user / another tab completed it concurrently) MUST surface as 409 so
    // the frontend conflict path renders the [Refresh case] recovery action. Returning 404 here
    // breaks AC5 because `classifyError` would treat it as a generic non-conflict failure.
    when(taskService.findById("nope")).thenThrow(new WksNotFoundException("task missing"));

    mockMvc
        .perform(post("/api/tasks/nope/complete").with(officerAuth()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("WKS-RTM-409"))
        .andExpect(
            jsonPath("$.error.message")
                .value(org.hamcrest.Matchers.containsString("already completed")));
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

  // ---- AC13-named coverage (Story 2.8) -----------------------------------
  // The three tests below mirror the named cases listed in Story 2.8 AC13. They duplicate the
  // scenarios already covered above with the spec-aligned identifiers so the test surface matches
  // the AC text verbatim.

  @Test
  void complete_409_unknown_task_treated_as_already_completed() throws Exception {
    // AC5 contract: any "task is gone" condition surfaces as 409 (conflict), not 404. The Phase
    // 0 backend cannot distinguish "never existed" from "already completed" at the engine layer
    // — both paths surface as the same conflict copy because that's the realistic root cause
    // from a UI driven by server-supplied task ids.
    when(taskService.findById("nope")).thenThrow(new WksNotFoundException("task missing"));
    mockMvc
        .perform(post("/api/tasks/nope/complete").with(officerAuth()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("WKS-RTM-409"));
  }

  @Test
  void complete_403_missing_verb() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("draft_section", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(false);
    mockMvc
        .perform(post("/api/tasks/t1/complete").with(officerAuth()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("WKS-API-403"));
  }

  @Test
  void complete_409_already_completed_by_other() throws Exception {
    when(taskService.findById("t1")).thenReturn(sampleTask("draft_section", null));
    when(evaluator.hasVerb(any(), anyString(), eq("transition"))).thenReturn(true);
    when(taskService.complete(eq("t1"), any(), any()))
        .thenThrow(new WksConflictException("Task t1 already completed"));
    mockMvc
        .perform(post("/api/tasks/t1/complete").with(officerAuth()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("WKS-RTM-409"));
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

  // ---- GET /api/tasks (Story 13-1) ---------------------------------------

  @Test
  void listAcrossCasesReturnsEnvelopeWithItemsAndTruncated() throws Exception {
    // AC1 — endpoint returns ApiResponse<CrossCaseTaskListDto> with items+truncated wrapper and
    // reuses TaskDtoMapper.toDtos exactly (verified via the wire-shape assertions below).
    when(caseTypeReader.all()).thenReturn(typesAB());
    when(evaluator.hasVerb(any(), eq("A"), eq("view"))).thenReturn(true);
    when(evaluator.hasVerb(any(), eq("B"), eq("view"))).thenReturn(true);
    when(taskService.listAcrossCases(eq(Set.of("A", "B")), eq(500)))
        .thenReturn(new CrossCaseTaskListResult(List.of(taskOnCaseType("t1", "A")), false));
    when(taskService.readActionLabel(anyString(), anyString())).thenReturn("Action");

    mockMvc
        .perform(get("/api/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(1))
        .andExpect(jsonPath("$.data.items[0].id").value("t1"))
        .andExpect(jsonPath("$.data.items[0].caseTypeId").value("A"))
        .andExpect(jsonPath("$.data.items[0].actionLabel").value("Action"))
        .andExpect(jsonPath("$.data.truncated").value(false));
  }

  @Test
  void listAcrossCasesSetsTruncatedTrueWhenEngineCaps() throws Exception {
    // AC5 — when the engine result is at-or-over the cap, truncated=true flows through.
    when(caseTypeReader.all()).thenReturn(typesAB());
    when(evaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(true);
    when(taskService.listAcrossCases(any(), eq(500)))
        .thenReturn(new CrossCaseTaskListResult(List.of(taskOnCaseType("t1", "A")), true));
    when(taskService.readActionLabel(anyString(), anyString())).thenReturn("Action");

    mockMvc
        .perform(get("/api/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.truncated").value(true));
  }

  @Test
  void listAcrossCasesFiltersOutCaseTypesWithoutViewVerb() throws Exception {
    // AC4 — case-type C has no view verb; the permitted set passed to the service excludes it
    // (verified by the eq(Set.of("A", "B")) match). Tasks on C never enter the result regardless
    // of engine state.
    when(caseTypeReader.all()).thenReturn(typesABC());
    when(evaluator.hasVerb(any(), eq("A"), eq("view"))).thenReturn(true);
    when(evaluator.hasVerb(any(), eq("B"), eq("view"))).thenReturn(true);
    when(evaluator.hasVerb(any(), eq("C"), eq("view"))).thenReturn(false);
    when(taskService.listAcrossCases(eq(Set.of("A", "B")), eq(500)))
        .thenReturn(new CrossCaseTaskListResult(List.of(), false));

    mockMvc
        .perform(get("/api/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(0))
        .andExpect(jsonPath("$.data.truncated").value(false));
  }

  @Test
  void listAcrossCasesEmptyWhenUserHasNoPermittedCaseTypes() throws Exception {
    // AC5 empty state — no view verb on any case-type returns items=[] without invoking the
    // engine (verified by passing an empty Set to the service stub).
    when(caseTypeReader.all()).thenReturn(typesAB());
    when(evaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(false);
    when(taskService.listAcrossCases(eq(Set.of()), eq(500)))
        .thenReturn(CrossCaseTaskListResult.empty());

    mockMvc
        .perform(get("/api/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(0))
        .andExpect(jsonPath("$.data.truncated").value(false));
  }

  private static Collection<CaseTypeConfig> typesAB() {
    return List.of(stubType("A"), stubType("B"));
  }

  private static Collection<CaseTypeConfig> typesABC() {
    return List.of(stubType("A"), stubType("B"), stubType("C"));
  }

  private static CaseTypeConfig stubType(String id) {
    return CaseTypeConfig.builder().id(id).displayName(id).version(1).build();
  }

  // ---- helpers -----------------------------------------------------------

  private static Task sampleTask(String archetype, UUID assignee) {
    return new Task(
        "t1",
        "pi-1",
        "pd-1",
        CASE_ID,
        "loan-application",
        "draft",
        "Draft",
        assignee,
        archetype,
        NOW,
        null);
  }

  /** Story 13-1 — builder used by listAcrossCases tests to vary id + caseTypeId. */
  private static Task taskOnCaseType(String id, String caseTypeId) {
    return new Task(
        id,
        "pi-" + id,
        "pd-1",
        CASE_ID,
        caseTypeId,
        "draft",
        "Draft",
        null,
        "draft_section",
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
