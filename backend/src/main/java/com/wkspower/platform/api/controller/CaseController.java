package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.request.CreateCaseRequest;
import com.wkspower.platform.api.dto.request.TransitionRequest;
import com.wkspower.platform.api.dto.request.UpdateCaseRequest;
import com.wkspower.platform.api.dto.response.CaseDto;
import com.wkspower.platform.api.dto.response.CaseSummaryDto;
import com.wkspower.platform.api.dto.response.TaskDto;
import com.wkspower.platform.api.mapper.CaseDtoMapper;
import com.wkspower.platform.api.mapper.TaskDtoMapper;
import com.wkspower.platform.api.pagination.PageRequestParams;
import com.wkspower.platform.api.pagination.SortSpec;
import com.wkspower.platform.api.pagination.SortWhitelist;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksValidationException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.page.SortOrder;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.TaskService;
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

  public CaseController(
      CaseService caseService, TaskService taskService, CaseTypePermissionEvaluator evaluator) {
    this.caseService = caseService;
    this.taskService = taskService;
    this.evaluator = evaluator;
  }

  @PostMapping
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

  @GetMapping("/{id}")
  public ApiResponse<CaseDto> get(
      @PathVariable("id") UUID id, @AuthenticationPrincipal WksUserPrincipal actor) {
    Case found = caseService.findById(id);
    requireVerb(actor, found.caseTypeId(), "view");
    CaseTypeConfig caseType = caseService.requireCaseType(found.caseTypeId());
    return ApiResponse.success(CaseDtoMapper.toDto(found, caseType));
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
   */
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

  private void requireVerb(WksUserPrincipal actor, String caseTypeId, String verb) {
    if (actor == null || !evaluator.hasVerb(actor.authenticated(), caseTypeId, verb)) {
      throw new AccessDeniedException(
          "Forbidden: missing verb '" + verb + "' on case type " + caseTypeId);
    }
  }
}
