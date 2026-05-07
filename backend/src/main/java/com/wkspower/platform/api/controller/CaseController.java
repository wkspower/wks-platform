package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.request.CreateCaseRequest;
import com.wkspower.platform.api.dto.request.StageAdvanceRequest;
import com.wkspower.platform.api.dto.request.StageSkipRequest;
import com.wkspower.platform.api.dto.request.TransitionRequest;
import com.wkspower.platform.api.dto.request.UpdateCaseRequest;
import com.wkspower.platform.api.dto.response.CaseDto;
import com.wkspower.platform.api.dto.response.CaseSummaryDto;
import com.wkspower.platform.api.dto.response.StageActionResponse;
import com.wkspower.platform.api.dto.response.TaskDto;
import com.wkspower.platform.api.mapper.CaseDtoMapper;
import com.wkspower.platform.api.mapper.TaskDtoMapper;
import com.wkspower.platform.api.pagination.PageRequestParams;
import com.wkspower.platform.api.pagination.SortSpec;
import com.wkspower.platform.api.pagination.SortWhitelist;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksStageException;
import com.wkspower.platform.domain.exception.WksValidationException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.model.Stage;
import com.wkspower.platform.domain.model.StageState;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.page.SortOrder;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.TaskService;
import com.wkspower.platform.domain.service.WksStageAdvancer;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.WksUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST surface for case CRUD (Story 2.3 AC5–AC8). Permission gating happens in the handler bodies
 * (D2 from code review): for {@code GET /{id}} and {@code PUT /{id}} the case is loaded first (404
 * from {@link CaseService#findById} if missing), then the verb gate fires against the loaded
 * case-type id. {@code POST} and list-{@code GET} gate before any work because the case-type id
 * comes straight from the request.
 *
 * <p>{@code PUT} requires the {@code edit} verb (D1 from code review) — operators must grant {@code
 * edit} to roles that should retain mutation rights in their case-type YAML.
 */
@RestController
@RequestMapping("/api/cases")
public class CaseController {

  private static final SortWhitelist CASE_LIST_SORT =
      () -> Set.of("updatedAt", "createdAt", "status");

  private final CaseService caseService;
  private final TaskService taskService;
  private final CaseTypePermissionEvaluator evaluator;
  private final WksStageAdvancer stageAdvancer;
  private final StageRepository stageRepository;

  public CaseController(
      CaseService caseService,
      TaskService taskService,
      CaseTypePermissionEvaluator evaluator,
      WksStageAdvancer stageAdvancer,
      StageRepository stageRepository) {
    this.caseService = caseService;
    this.taskService = taskService;
    this.evaluator = evaluator;
    this.stageAdvancer = stageAdvancer;
    this.stageRepository = stageRepository;
  }

  /**
   * AC4 atomicity surface (Story 3.1 code review B1, 2026-05-05): {@code @Transactional} pins the
   * case insert + stage materialise + first-stage activate to a single Spring-managed transaction.
   * Any unchecked failure from {@link CaseService#create} or {@link
   * com.wkspower.platform.domain.service.WksStageAdvancer#bootstrap} rolls back the whole unit; the
   * after-commit {@code StageEntered} bootstrap event publishes only on a successful commit (see
   * {@link com.wkspower.platform.infrastructure.events.SpringEventPublisher#publishAfterCommit}).
   */
  @PostMapping
  @Transactional
  public ResponseEntity<ApiResponse<CaseDto>> create(
      @Valid @RequestBody CreateCaseRequest request,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    requireVerb(actor, request.caseTypeId(), "create");
    Case created =
        caseService.create(request.caseTypeId(), request.data(), request.assignee(), actor.id());
    CaseTypeConfig caseType = caseService.requireCaseType(created.caseTypeId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(CaseDtoMapper.toDto(created, caseType)));
  }

  /**
   * Story 3.3 AC3 — the read-model surface that drives the Stage Timeline UI. Loads the case-stage
   * history (one row per declared stage on the bound CaseType@version, including SKIPPED rows at
   * their declared ordinal) and passes it to the mapper so the wire {@link CaseDto#stages()} array
   * is populated in a single round-trip. Zero-stage CaseTypes return {@code stages: []}.
   */
  @GetMapping("/{id}")
  public ApiResponse<CaseDto> get(
      @PathVariable("id") UUID id, @AuthenticationPrincipal WksUserPrincipal actor) {
    Case found = caseService.findById(id);
    requireVerb(actor, found.caseTypeId(), "view");
    CaseTypeConfig caseType = caseService.requireCaseType(found.caseTypeId());
    List<Stage> history = stageRepository.loadHistory(id);
    return ApiResponse.success(CaseDtoMapper.toDto(found, caseType, history));
  }

  /**
   * Story 2.8 AC1 — list pending (active, uncompleted) BPMN user tasks for a case. 404 when the
   * case does not exist; 403 when the caller lacks the {@code view} verb on its case-type. An empty
   * list (case at terminal end-event) returns {@code 200 data: []}.
   */
  @GetMapping("/{id}/tasks")
  public ApiResponse<List<TaskDto>> listTasks(
      @PathVariable("id") UUID id, @AuthenticationPrincipal WksUserPrincipal actor) {
    Case found = caseService.findById(id);
    requireVerb(actor, found.caseTypeId(), "view");
    List<TaskDto> dtos =
        TaskDtoMapper.toDtos(taskService.findByCase(id), taskService::readActionLabel);
    return ApiResponse.success(dtos);
  }

  @GetMapping
  public ApiResponse<List<CaseSummaryDto>> list(
      @RequestParam("caseType") String caseType,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size,
      // Bind as String[] so Spring's ConversionService does NOT split on commas — sort tokens are
      // `property,direction` (e.g. `updatedAt,desc`) and the comma is part of the wire shape.
      // Multi-sort uses repeated `?sort=` params, not CSV. List<String> binding here would split
      // `updatedAt,desc` into [`updatedAt`, `desc`] and reject `desc` as a bad property.
      @RequestParam(value = "sort", required = false) String[] sort,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    if (caseType == null || caseType.isBlank()) {
      throw new WksValidationException(
          ErrorCode.WKS_API_001, "caseType query parameter is required", "caseType");
    }
    requireVerb(actor, caseType, "view");
    PageRequestParams params =
        PageRequestParams.of(page, size, sort == null ? null : List.of(sort));
    // toPageable validates page/size bounds and the sort allow-list; we use the parsed sort
    // (not the Spring Pageable) to translate into our domain SortOrder list.
    params.toPageable(CASE_LIST_SORT);

    List<SortOrder> domainSort =
        params.parsedSort().stream()
            .map(s -> new SortOrder(s.property(), s.direction() == SortSpec.Direction.ASC))
            .toList();
    if (domainSort.isEmpty()) {
      domainSort = List.of(new SortOrder("updatedAt", false));
    }
    PageRequest pageRequest =
        PageRequest.of(
            params.page(), Math.min(params.size(), PageRequestParams.MAX_SIZE), domainSort);
    CaseQuery query = CaseQuery.of(caseType, status);

    Page<CaseSummary> results = caseService.list(query, pageRequest);
    List<CaseSummaryDto> dtos =
        results.content().stream().map(CaseDtoMapper::toSummaryDto).toList();
    Map<String, Object> meta =
        Map.of(
            "page", results.page(),
            "size", results.size(),
            "total", results.total());
    return ApiResponse.success(dtos, meta);
  }

  @PutMapping("/{id}")
  public ApiResponse<CaseDto> update(
      @PathVariable("id") UUID id,
      @Valid @RequestBody UpdateCaseRequest request,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    Case found = caseService.findById(id);
    requireVerb(actor, found.caseTypeId(), "edit");
    Case updated = caseService.update(id, request.data(), request.version(), actor.id());
    CaseTypeConfig caseType = caseService.requireCaseType(updated.caseTypeId());
    return ApiResponse.success(CaseDtoMapper.toDto(updated, caseType));
  }

  /**
   * Story 2.4 AC1 — advance the case through its BPMN process. Loads the case first so an unknown
   * id surfaces 404 before the verb check; the {@code transition} verb gates message dispatch.
   * Engine-side conflicts (no enabled receiver, optimistic lock) are translated to {@code
   * WKS-RTM-409} inside {@code CibSevenWorkflowEngine}; missing process instance surfaces {@code
   * WKS-RTM-500}.
   *
   * <p>Story 4.4b — {@code @Transactional} added to provide the transaction context that {@link
   * com.wkspower.platform.infrastructure.persistence.CaseStatusAdapter} requires ({@code
   * Propagation.MANDATORY}). On the BPMN path the signal router calls the status adapter inside the
   * engine transaction; on the manual zero-process and BPMN-manual paths the controller transaction
   * boundary is the outermost transactional context.
   */
  @Transactional
  @PostMapping("/{id}/transition")
  public ApiResponse<CaseDto> transition(
      @PathVariable("id") UUID id,
      @Valid @RequestBody TransitionRequest request,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    Case found = caseService.findById(id);
    requireVerb(actor, found.caseTypeId(), "transition");
    Case updated = caseService.transition(id, request.action(), request.variables(), actor.id());
    CaseTypeConfig caseType = caseService.requireCaseType(updated.caseTypeId());
    return ApiResponse.success(CaseDtoMapper.toDto(updated, caseType));
  }

  /**
   * Story 3.1 AC10 — manually advance the active stage. Server fills {@code source = "manual"};
   * {@code sourceRef} is optional. Permission gate: {@code transition} verb (no new permission
   * introduced). Error mapping: 404 on unknown caseId ({@code WKS-STG-004} / loaded-first 404), 409
   * on already-complete ({@code WKS-STG-001}) or concurrent ({@code WKS-STG-003}).
   */
  @PostMapping("/{id}/advance-stage")
  @Transactional
  public ResponseEntity<ApiResponse<StageActionResponse>> advanceStage(
      @PathVariable("id") UUID id,
      @Valid @RequestBody(required = false) StageAdvanceRequest request,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    requireTransitionVerbForStageAction(id, actor);
    String sourceRef = request == null ? null : request.sourceRef();
    stageAdvancer.advance(id, "manual", sourceRef);
    return ResponseEntity.ok(ApiResponse.success(loadStageHead(id)));
  }

  /**
   * Story 3.1 AC10 — skip ahead to a future-ordinal stage. Server fills {@code source = "manual"}.
   * Backward skip is rejected with {@code WKS-STG-002} (422); concurrent transitions surface as
   * {@code WKS-STG-003} (409).
   */
  @PostMapping("/{id}/skip-stage")
  @Transactional
  public ResponseEntity<ApiResponse<StageActionResponse>> skipStage(
      @PathVariable("id") UUID id,
      @Valid @RequestBody StageSkipRequest request,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    requireTransitionVerbForStageAction(id, actor);
    stageAdvancer.skipTo(id, request.targetStageId(), "manual", request.sourceRef());
    return ResponseEntity.ok(ApiResponse.success(loadStageHead(id)));
  }

  /**
   * Stage-action authz helper (Story 3.1 code review S2, 2026-05-05). Loads the case to gate the
   * {@code transition} verb — but translates a missing case to {@code WKS-STG-004} so the wire
   * contract from AC9 (unknown caseId → STG-004 → 404) holds, instead of leaking the generic
   * loaded-first {@code WKS-API-404}. Found cases route through the standard verb gate.
   */
  private void requireTransitionVerbForStageAction(UUID id, WksUserPrincipal actor) {
    Case found;
    try {
      found = caseService.findById(id);
    } catch (WksNotFoundException ex) {
      throw new WksStageException(
          ErrorCode.WKS_STG_004, "Case " + id + " not found — cannot advance / skip");
    }
    requireVerb(actor, found.caseTypeId(), "transition");
  }

  /**
   * Read the post-transition active stage from the history table. Returns {@code (id, null, null)}
   * when no stage is active (last-stage completion or zero-stage case).
   */
  private StageActionResponse loadStageHead(UUID caseId) {
    List<Stage> history = stageRepository.loadHistory(caseId);
    return history.stream()
        .filter(s -> s.state() == StageState.ACTIVE)
        .findFirst()
        .map(s -> new StageActionResponse(caseId, s.stageId(), s.ordinal()))
        .orElseGet(() -> new StageActionResponse(caseId, null, null));
  }

  private void requireVerb(WksUserPrincipal actor, String caseTypeId, String verb) {
    if (actor == null || !evaluator.hasVerb(actor.authenticated(), caseTypeId, verb)) {
      throw new AccessDeniedException(
          "Forbidden: missing verb '" + verb + "' on case type " + caseTypeId);
    }
  }
}
