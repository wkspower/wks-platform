package com.wkspower.platform.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkspower.platform.api.GlobalExceptionHandler;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksValidationAggregateException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.port.UserRepository;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.LicenseService;
import com.wkspower.platform.security.AuthenticatedUser;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.JwtAuthenticationFilter;
import com.wkspower.platform.security.JwtTokenProvider;
import com.wkspower.platform.security.SamlGatingFilter;
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

/** Slice test for {@link CaseController}. Covers the four endpoints + the SpEL permission gate. */
@WebMvcTest(CaseController.class)
@Import({
  SecurityConfig.class,
  GlobalExceptionHandler.class,
  JwtAuthenticationFilter.class,
  SamlGatingFilter.class
})
@ActiveProfiles("dev")
class CaseControllerTest {

  private static final UUID ACTOR_ID = UUID.randomUUID();
  private static final Instant NOW = Instant.parse("2026-04-26T10:00:00Z");

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockitoBean(name = "wksCaseService")
  CaseService caseService;

  @MockitoBean(name = "wksTaskService")
  com.wkspower.platform.domain.service.TaskService taskService;

  @MockitoBean(name = "wksStageAdvancer")
  com.wkspower.platform.domain.service.WksStageAdvancer stageAdvancer;

  @MockitoBean com.wkspower.platform.domain.port.StageRepository stageRepository;

  /** Story 5.5 AC-4 — CaseController now injects CaseTypeReader for pinned-version DTO build. */
  @MockitoBean com.wkspower.platform.domain.port.CaseTypeReader caseTypeReader;

  /** Story 2-6-1 — CaseController injects MappingRegistry to project TaskDto.formId. */
  @MockitoBean com.wkspower.platform.domain.service.MappingRegistry mappingRegistry;

  /** Story 9-2 — CaseController injects AuditEventWriter for GET /{id}/audit-events. */
  @MockitoBean com.wkspower.platform.audit.AuditEventWriter auditEventWriter;

  @MockitoBean(name = "caseTypePermissionEvaluator")
  CaseTypePermissionEvaluator caseTypePermissionEvaluator;

  @MockitoBean JwtTokenProvider jwtTokenProvider;
  @MockitoBean UserRepository userRepository;
  @MockitoBean LicenseService licenseService;

  // ---- POST /api/cases ---------------------------------------------------

  @Test
  void postReturns201OnHappyPath() throws Exception {
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("create")))
        .thenReturn(true);
    when(caseService.create(eq("loan-application"), any(), any(), eq(ACTOR_ID)))
        .thenReturn(sampleCase());
    when(caseService.requireCaseType(eq("loan-application"))).thenReturn(loanType());

    mockMvc
        .perform(
            post("/api/cases")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"caseTypeId\":\"loan-application\",\"data\":{\"name\":\"Asha\"}}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").exists())
        .andExpect(jsonPath("$.data.documentCount").value(0))
        .andExpect(jsonPath("$.data.caseType.displayName").value("Loan Application"));
  }

  @Test
  void postReturns403WhenCreateVerbDenied() throws Exception {
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("create"))).thenReturn(false);

    mockMvc
        .perform(
            post("/api/cases")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"caseTypeId\":\"loan-application\",\"data\":{}}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void postReturns422OnDataValidationFailure() throws Exception {
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("create"))).thenReturn(true);
    when(caseService.create(anyString(), any(), any(), any()))
        .thenThrow(
            new WksValidationAggregateException(
                "validation",
                List.of(ErrorDetail.ofField("WKS-API-001", "must not be blank", "name"))));

    mockMvc
        .perform(
            post("/api/cases")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"caseTypeId\":\"loan-application\",\"data\":{}}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.error.code").value("WKS-API-002"));
  }

  // ---- GET /api/cases/{id} -----------------------------------------------

  @Test
  void getReturns200WithEmbeddedCaseType() throws Exception {
    Case sample = sampleCase();
    when(caseService.findById(sample.id())).thenReturn(sample);
    when(caseService.requireCaseType("loan-application")).thenReturn(loanType());
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);

    mockMvc
        .perform(get("/api/cases/" + sample.id()).with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.caseType.id").value("loan-application"))
        .andExpect(jsonPath("$.data.caseType.statuses[0].id").value("open"));
  }

  // ---- Story 3.3 AC3 — stage timeline projection on GET /api/cases/{id} -------

  @Test
  void getReturnsStageTimelineForMultiStageCase() throws Exception {
    Case sample = sampleCase();
    when(caseService.findById(sample.id())).thenReturn(sample);
    when(caseService.requireCaseType("loan-application")).thenReturn(threeStageLoanType());
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    when(stageRepository.loadHistory(sample.id())).thenReturn(threeStageHistory(sample.id()));

    mockMvc
        .perform(get("/api/cases/" + sample.id()).with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.stages.length()").value(3))
        // Ordered by ordinal ASC: intake (COMPLETED) → underwriting (ACTIVE) → decision (PENDING).
        .andExpect(jsonPath("$.data.stages[0].stageId").value("intake"))
        .andExpect(jsonPath("$.data.stages[0].ordinal").value(0))
        .andExpect(jsonPath("$.data.stages[0].state").value("COMPLETED"))
        .andExpect(jsonPath("$.data.stages[0].displayName").value("Intake"))
        .andExpect(jsonPath("$.data.stages[1].stageId").value("underwriting"))
        .andExpect(jsonPath("$.data.stages[1].state").value("ACTIVE"))
        .andExpect(jsonPath("$.data.stages[2].stageId").value("decision"))
        .andExpect(jsonPath("$.data.stages[2].state").value("PENDING"))
        // CaseTypeViewDto also exposes the declared stages for the timeline schema.
        .andExpect(jsonPath("$.data.caseType.stages.length()").value(3))
        .andExpect(jsonPath("$.data.caseType.stages[0].id").value("intake"))
        .andExpect(jsonPath("$.data.caseType.stages[0].ordinal").value(0));
  }

  @Test
  void getReturnsEmptyStageTimelineForZeroStageCase() throws Exception {
    Case sample = sampleCase();
    when(caseService.findById(sample.id())).thenReturn(sample);
    when(caseService.requireCaseType("loan-application")).thenReturn(loanType());
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    when(stageRepository.loadHistory(sample.id())).thenReturn(List.of());

    mockMvc
        .perform(get("/api/cases/" + sample.id()).with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.stages.length()").value(0))
        .andExpect(jsonPath("$.data.caseType.stages.length()").value(0));
  }

  @Test
  void getReturnsSkippedStageInOrdinalPositionForBypassedCase() throws Exception {
    // AC3 — skipped stages stay in the list at their declared ordinal; the timeline never omits.
    Case sample = sampleCase();
    when(caseService.findById(sample.id())).thenReturn(sample);
    when(caseService.requireCaseType("loan-application")).thenReturn(threeStageLoanType());
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    when(stageRepository.loadHistory(sample.id()))
        .thenReturn(threeStageHistoryWithSkip(sample.id()));

    mockMvc
        .perform(get("/api/cases/" + sample.id()).with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.stages.length()").value(3))
        .andExpect(jsonPath("$.data.stages[0].state").value("COMPLETED"))
        .andExpect(jsonPath("$.data.stages[1].stageId").value("underwriting"))
        .andExpect(jsonPath("$.data.stages[1].state").value("SKIPPED"))
        .andExpect(jsonPath("$.data.stages[2].state").value("ACTIVE"));
  }

  @Test
  void getReturns404WhenServiceThrows() throws Exception {
    UUID id = UUID.randomUUID();
    Case sample = sampleCase(id);
    when(caseService.findById(id)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(true);
    when(caseService.requireCaseType(anyString())).thenThrow(new WksNotFoundException("not found"));

    mockMvc
        .perform(get("/api/cases/" + id).with(officerAuth()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("WKS-API-404"));
  }

  // ---- GET /api/cases (list) ---------------------------------------------

  @Test
  void listReturns200WithMeta() throws Exception {
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    Case s = sampleCase();
    when(caseService.list(any(CaseQuery.class), any(PageRequest.class)))
        .thenReturn(
            new Page<>(
                List.of(
                    new CaseSummary(
                        s.id(),
                        s.caseTypeId(),
                        s.status(),
                        null,
                        s.createdAt(),
                        s.updatedAt(),
                        Map.of())),
                1,
                0,
                20));

    mockMvc
        .perform(get("/api/cases?caseType=loan-application").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.meta.total").value(1))
        .andExpect(jsonPath("$.meta.size").value(20));
  }

  @Test
  void listWithoutCaseTypeParamReturns400() throws Exception {
    mockMvc.perform(get("/api/cases").with(officerAuth())).andExpect(status().isBadRequest());
  }

  // ---- GET /api/cases/{id}/tasks (Story 2.8 AC1) -------------------------

  @Test
  void listTasksReturns200WithPendingTasks() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    when(taskService.findByCase(caseId))
        .thenReturn(
            List.of(
                new com.wkspower.platform.domain.model.Task(
                    "t1",
                    "pi-1",
                    "pd-1",
                    caseId,
                    "loan-application",
                    "draft",
                    "Draft application",
                    null,
                    "draft_section",
                    NOW,
                    null)));
    when(taskService.readActionLabel("pd-1", "draft")).thenReturn("Draft application");

    mockMvc
        .perform(get("/api/cases/" + caseId + "/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].id").value("t1"))
        .andExpect(jsonPath("$.data[0].actionLabel").value("Draft application"))
        .andExpect(jsonPath("$.data[0].caseId").value(caseId.toString()))
        // Story 2-6-1 — no mapping registered → formId is null (affordance suppressed).
        .andExpect(jsonPath("$.data[0].formId").value(org.hamcrest.Matchers.nullValue()));
  }

  // ---- Story 2-6-1 — TaskDto.formId projection ---------------------------

  @Test
  void listTasksProjectsFormIdWhenMappingDeclaresForm() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    when(taskService.findByCase(caseId))
        .thenReturn(
            List.of(
                new com.wkspower.platform.domain.model.Task(
                    "t1",
                    "pi-1",
                    "pd-1",
                    caseId,
                    "loan-application",
                    "draft",
                    "Draft",
                    null,
                    "draft_section",
                    NOW,
                    null)));
    when(taskService.readActionLabel("pd-1", "draft")).thenReturn("Draft");
    when(mappingRegistry.resolve(any(), anyString()))
        .thenReturn(
            java.util.Optional.of(
                new com.wkspower.platform.domain.config.model.MappingDefinition(
                    List.of(
                        new com.wkspower.platform.domain.config.model.AttachmentDefinition(
                            "bpmn",
                            "loan.bpmn",
                            "case",
                            java.util.Optional.empty(),
                            Map.of(
                                "draft",
                                new com.wkspower.platform.domain.config.model.AttachmentDefinition
                                    .UserTaskMapping("wks-draft", "loan-form")),
                            java.util.Optional.empty(),
                            Map.of(),
                            List.of(),
                            Map.of())))));

    mockMvc
        .perform(get("/api/cases/" + caseId + "/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].formId").value("loan-form"));
  }

  @Test
  void listTasksReturnsNullFormIdWhenNoMappingMatchesTaskDefinitionKey() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);
    when(taskService.findByCase(caseId))
        .thenReturn(
            List.of(
                new com.wkspower.platform.domain.model.Task(
                    "t1",
                    "pi-1",
                    "pd-1",
                    caseId,
                    "loan-application",
                    "review",
                    "Review",
                    null,
                    "draft_section",
                    NOW,
                    null)));
    when(taskService.readActionLabel("pd-1", "review")).thenReturn("Review");
    // Mapping registered but no userTaskMapping for "review" → formId null.
    when(mappingRegistry.resolve(any(), anyString()))
        .thenReturn(
            java.util.Optional.of(
                new com.wkspower.platform.domain.config.model.MappingDefinition(
                    List.of(
                        new com.wkspower.platform.domain.config.model.AttachmentDefinition(
                            "bpmn",
                            "loan.bpmn",
                            "case",
                            java.util.Optional.empty(),
                            Map.of(
                                "draft",
                                new com.wkspower.platform.domain.config.model.AttachmentDefinition
                                    .UserTaskMapping("wks-draft", "loan-form")),
                            java.util.Optional.empty(),
                            Map.of(),
                            List.of(),
                            Map.of())))));

    mockMvc
        .perform(get("/api/cases/" + caseId + "/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].formId").value(org.hamcrest.Matchers.nullValue()));
  }

  @Test
  void listTasksReturns200EmptyForTerminalCase() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(true);
    when(taskService.findByCase(caseId)).thenReturn(List.of());

    mockMvc
        .perform(get("/api/cases/" + caseId + "/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void listTasksReturns403WithoutViewVerb() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(false);

    mockMvc
        .perform(get("/api/cases/" + caseId + "/tasks").with(officerAuth()))
        .andExpect(status().isForbidden());
  }

  @Test
  void listTasksReturns404WhenCaseUnknown() throws Exception {
    UUID caseId = UUID.randomUUID();
    when(caseService.findById(caseId)).thenThrow(new WksNotFoundException("missing"));

    mockMvc
        .perform(get("/api/cases/" + caseId + "/tasks").with(officerAuth()))
        .andExpect(status().isNotFound());
  }

  @Test
  void listTasksFallsBackToTaskNameWhenActionLabelMissing() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(true);
    when(taskService.findByCase(caseId))
        .thenReturn(
            List.of(
                new com.wkspower.platform.domain.model.Task(
                    "t1",
                    "pi-1",
                    "pd-1",
                    caseId,
                    "loan-application",
                    "draft",
                    "Review",
                    null,
                    "draft_section",
                    NOW,
                    null)));
    when(taskService.readActionLabel("pd-1", "draft")).thenReturn(null);

    mockMvc
        .perform(get("/api/cases/" + caseId + "/tasks").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].actionLabel").value("Review"));
  }

  // ---- GET /api/cases/{id}/audit-events (Story 9-2 AC1, AC2) -------------

  @Test
  void listAuditEventsReturns200WithAllFourSourceVariants() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);

    UUID actorUuid = UUID.fromString("00000000-0000-0000-0000-00000000beef");
    Instant t = NOW;
    when(auditEventWriter.findByCaseId(eq(caseId), eq(51)))
        .thenReturn(
            List.of(
                auditEvent(
                    caseId,
                    new com.wkspower.platform.domain.model.AuditSource.User(actorUuid),
                    "APPLIED",
                    "priority",
                    null,
                    null,
                    t),
                auditEvent(
                    caseId,
                    new com.wkspower.platform.domain.model.AuditSource.AutoRule("rule-7"),
                    "APPLIED",
                    "status",
                    null,
                    null,
                    t.minusSeconds(10)),
                auditEvent(
                    caseId,
                    new com.wkspower.platform.domain.model.AuditSource.Backend("bpmn"),
                    "BLOCKED",
                    "amount",
                    "task-1",
                    "loan-form",
                    t.minusSeconds(20)),
                auditEvent(
                    caseId,
                    new com.wkspower.platform.domain.model.AuditSource.ExecutionUnmapped("rest"),
                    "REJECTED",
                    null,
                    null,
                    null,
                    t.minusSeconds(30))));

    mockMvc
        .perform(get("/api/cases/" + caseId + "/audit-events").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(4))
        .andExpect(jsonPath("$.data.truncated").value(false))
        // USER variant — payload.actorId mirrors the persistence column.
        .andExpect(jsonPath("$.data.items[0].source.type").value("USER"))
        .andExpect(jsonPath("$.data.items[0].source.payload.actorId").value(actorUuid.toString()))
        .andExpect(jsonPath("$.data.items[0].result").value("APPLIED"))
        .andExpect(jsonPath("$.data.items[0].fieldId").value("priority"))
        .andExpect(jsonPath("$.data.items[0].eventType").value("case.data.edit"))
        // AUTO_RULE variant.
        .andExpect(jsonPath("$.data.items[1].source.type").value("AUTO_RULE"))
        .andExpect(jsonPath("$.data.items[1].source.payload.ruleId").value("rule-7"))
        // BACKEND variant — BLOCKED row carries openTaskId + formId.
        .andExpect(jsonPath("$.data.items[2].source.type").value("BACKEND"))
        .andExpect(jsonPath("$.data.items[2].source.payload.adapterName").value("bpmn"))
        .andExpect(jsonPath("$.data.items[2].result").value("BLOCKED"))
        .andExpect(jsonPath("$.data.items[2].openTaskId").value("task-1"))
        .andExpect(jsonPath("$.data.items[2].formId").value("loan-form"))
        // EXECUTION_UNMAPPED variant.
        .andExpect(jsonPath("$.data.items[3].source.type").value("EXECUTION_UNMAPPED"))
        .andExpect(jsonPath("$.data.items[3].source.payload.originAdapter").value("rest"))
        .andExpect(jsonPath("$.data.items[3].result").value("REJECTED"));
  }

  @Test
  void listAuditEventsSetsTruncatedTrueWhenRepoReturnsMoreThanLimit() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("view")))
        .thenReturn(true);

    UUID actorUuid = UUID.randomUUID();
    Instant t = NOW;
    // limit=2 → repo asked for 3; returning 3 rows → truncated=true, only 2 items in the wire.
    when(auditEventWriter.findByCaseId(eq(caseId), eq(3)))
        .thenReturn(
            List.of(
                auditEvent(
                    caseId,
                    new com.wkspower.platform.domain.model.AuditSource.User(actorUuid),
                    "APPLIED",
                    "f1",
                    null,
                    null,
                    t),
                auditEvent(
                    caseId,
                    new com.wkspower.platform.domain.model.AuditSource.User(actorUuid),
                    "APPLIED",
                    "f2",
                    null,
                    null,
                    t.minusSeconds(1)),
                auditEvent(
                    caseId,
                    new com.wkspower.platform.domain.model.AuditSource.User(actorUuid),
                    "APPLIED",
                    "f3",
                    null,
                    null,
                    t.minusSeconds(2))));

    mockMvc
        .perform(get("/api/cases/" + caseId + "/audit-events?limit=2").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.truncated").value(true));
  }

  @Test
  void listAuditEventsReturns200EmptyWhenNoRows() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(true);
    when(auditEventWriter.findByCaseId(eq(caseId), eq(51))).thenReturn(List.of());

    mockMvc
        .perform(get("/api/cases/" + caseId + "/audit-events").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(0))
        .andExpect(jsonPath("$.data.truncated").value(false));
  }

  @Test
  void listAuditEventsReturns403WithoutViewVerb() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(false);

    mockMvc
        .perform(get("/api/cases/" + caseId + "/audit-events").with(officerAuth()))
        .andExpect(status().isForbidden());
  }

  @Test
  void listAuditEventsClampsLimitAboveCapToServerMax() throws Exception {
    UUID caseId = UUID.randomUUID();
    Case sample = sampleCase(caseId);
    when(caseService.findById(caseId)).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("view"))).thenReturn(true);
    // limit=9999 → clamped to 200; repo asked for 201.
    when(auditEventWriter.findByCaseId(eq(caseId), eq(201))).thenReturn(List.of());

    mockMvc
        .perform(get("/api/cases/" + caseId + "/audit-events?limit=9999").with(officerAuth()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.items.length()").value(0));
  }

  @Test
  void listAuditEventsReturns404WhenCaseUnknown() throws Exception {
    UUID caseId = UUID.randomUUID();
    when(caseService.findById(caseId)).thenThrow(new WksNotFoundException("missing"));

    mockMvc
        .perform(get("/api/cases/" + caseId + "/audit-events").with(officerAuth()))
        .andExpect(status().isNotFound());
  }

  /** Story 9-2 — minimal {@code AuditEvent} factory used by the audit-events slice tests. */
  private static com.wkspower.platform.audit.AuditEvent auditEvent(
      UUID caseId,
      com.wkspower.platform.domain.model.AuditSource source,
      String result,
      String fieldId,
      String openTaskId,
      String formId,
      Instant occurredAt) {
    return new com.wkspower.platform.audit.AuditEvent(
        UUID.randomUUID(),
        caseId,
        com.wkspower.platform.audit.AuditEvent.EVENT_TYPE_CASE_DATA_EDIT,
        source,
        result,
        null,
        fieldId,
        openTaskId,
        formId,
        occurredAt,
        occurredAt);
  }

  // ---- PUT /api/cases/{id} -----------------------------------------------

  @Test
  void putReturns200OnHappyPath() throws Exception {
    Case sample = sampleCase();
    when(caseService.findById(sample.id())).thenReturn(sample);
    when(caseService.requireCaseType("loan-application")).thenReturn(loanType());
    when(caseTypePermissionEvaluator.hasVerb(any(), eq("loan-application"), eq("edit")))
        .thenReturn(true);
    when(caseService.update(eq(sample.id()), any(), eq(0L), eq(ACTOR_ID))).thenReturn(sample);

    mockMvc
        .perform(
            put("/api/cases/" + sample.id())
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"data\":{\"name\":\"Bob\"},\"version\":0}"))
        .andExpect(status().isOk());
  }

  @Test
  void putReturns409OnVersionMismatch() throws Exception {
    Case sample = sampleCase();
    when(caseService.findById(sample.id())).thenReturn(sample);
    when(caseTypePermissionEvaluator.hasVerb(any(), anyString(), eq("edit"))).thenReturn(true);
    when(caseService.update(any(), any(), any(Long.class), any()))
        .thenThrow(new WksConflictException("modified"));

    mockMvc
        .perform(
            put("/api/cases/" + sample.id())
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"data\":{\"name\":\"Bob\"},\"version\":99}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("WKS-RTM-409"));
  }

  // ---- POST /api/cases/{id}/advance-stage & /skip-stage (Story 3.1 AC10) -

  @Test
  void advanceStageReturns404WithStg004WhenCaseUnknown() throws Exception {
    // Story 3.1 code review S2 (2026-05-05): unknown caseId on stage endpoints must surface
    // WKS-STG-004 (the wire-contract code for "advance/skip references unknown caseId"), not the
    // generic WKS-API-404 from the loaded-first findById precheck.
    UUID id = UUID.randomUUID();
    when(caseService.findById(id)).thenThrow(new WksNotFoundException("missing"));

    mockMvc
        .perform(
            post("/api/cases/" + id + "/advance-stage")
                .with(officerAuth())
                .contentType("application/json")
                .content("{}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("WKS-STG-004"));
  }

  @Test
  void skipStageReturns404WithStg004WhenCaseUnknown() throws Exception {
    UUID id = UUID.randomUUID();
    when(caseService.findById(id)).thenThrow(new WksNotFoundException("missing"));

    mockMvc
        .perform(
            post("/api/cases/" + id + "/skip-stage")
                .with(officerAuth())
                .contentType("application/json")
                .content("{\"targetStageId\":\"decision\"}"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("WKS-STG-004"));
  }

  // ---- helpers -----------------------------------------------------------

  private static Case sampleCase() {
    return sampleCase(UUID.randomUUID());
  }

  private static Case sampleCase(UUID id) {
    return new Case(
        id,
        "loan-application",
        1,
        "open",
        null,
        Map.of("name", "Asha"),
        "pi-1",
        NOW,
        ACTOR_ID,
        NOW,
        0L);
  }

  private static CaseTypeConfig loanType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("officer", List.of(Permission.VIEW, Permission.CREATE))),
        List.of(),
        List.of());
  }

  /** Story 3.3 — three-stage CaseType fixture (intake → underwriting → decision). */
  private static CaseTypeConfig threeStageLoanType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("officer", List.of(Permission.VIEW, Permission.CREATE))),
        List.of(
            new com.wkspower.platform.domain.config.model.StageDefinition("intake", "Intake", 0),
            new com.wkspower.platform.domain.config.model.StageDefinition(
                "underwriting", "Underwriting", 1),
            new com.wkspower.platform.domain.config.model.StageDefinition(
                "decision", "Decision", 2)),
        List.of());
  }

  /** Story 3.3 — happy path: stage 0 COMPLETED, stage 1 ACTIVE, stage 2 PENDING. */
  private static List<com.wkspower.platform.domain.model.Stage> threeStageHistory(UUID caseId) {
    Instant t0 = NOW;
    Instant t1 = NOW.plusSeconds(60);
    return List.of(
        new com.wkspower.platform.domain.model.Stage(
            UUID.randomUUID(),
            caseId,
            "intake",
            0,
            com.wkspower.platform.domain.model.StageState.COMPLETED,
            t0,
            t1,
            "manual",
            null),
        new com.wkspower.platform.domain.model.Stage(
            UUID.randomUUID(),
            caseId,
            "underwriting",
            1,
            com.wkspower.platform.domain.model.StageState.ACTIVE,
            t1,
            null,
            "manual",
            null),
        new com.wkspower.platform.domain.model.Stage(
            UUID.randomUUID(),
            caseId,
            "decision",
            2,
            com.wkspower.platform.domain.model.StageState.PENDING,
            null,
            null,
            null,
            null));
  }

  /** Story 3.3 — bypass case: stage 0 COMPLETED, stage 1 SKIPPED, stage 2 ACTIVE. */
  private static List<com.wkspower.platform.domain.model.Stage> threeStageHistoryWithSkip(
      UUID caseId) {
    Instant t0 = NOW;
    Instant t1 = NOW.plusSeconds(60);
    return List.of(
        new com.wkspower.platform.domain.model.Stage(
            UUID.randomUUID(),
            caseId,
            "intake",
            0,
            com.wkspower.platform.domain.model.StageState.COMPLETED,
            t0,
            t1,
            "manual",
            null),
        new com.wkspower.platform.domain.model.Stage(
            UUID.randomUUID(),
            caseId,
            "underwriting",
            1,
            com.wkspower.platform.domain.model.StageState.SKIPPED,
            null,
            t1,
            "manual",
            null),
        new com.wkspower.platform.domain.model.Stage(
            UUID.randomUUID(),
            caseId,
            "decision",
            2,
            com.wkspower.platform.domain.model.StageState.ACTIVE,
            t1,
            null,
            "manual",
            null));
  }

  /** Authenticated post-processor with a valid {@link AuthenticatedUser} principal. */
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
