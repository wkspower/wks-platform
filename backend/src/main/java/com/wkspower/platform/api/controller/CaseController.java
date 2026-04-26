package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.request.CreateCaseRequest;
import com.wkspower.platform.api.dto.request.UpdateCaseRequest;
import com.wkspower.platform.api.dto.response.CaseDto;
import com.wkspower.platform.api.dto.response.CaseSummaryDto;
import com.wkspower.platform.api.mapper.CaseDtoMapper;
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
  private final CaseTypePermissionEvaluator evaluator;

  public CaseController(CaseService caseService, CaseTypePermissionEvaluator evaluator) {
    this.caseService = caseService;
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

  @GetMapping
  public ApiResponse<List<CaseSummaryDto>> list(
      @RequestParam("caseType") String caseType,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size,
      @RequestParam(value = "sort", required = false) List<String> sort,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    if (caseType == null || caseType.isBlank()) {
      throw new WksValidationException(
          ErrorCode.WKS_API_001, "caseType query parameter is required", "caseType");
    }
    requireVerb(actor, caseType, "view");
    PageRequestParams params = PageRequestParams.of(page, size, sort);
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

  private void requireVerb(WksUserPrincipal actor, String caseTypeId, String verb) {
    if (actor == null || !evaluator.hasVerb(actor.authenticated(), caseTypeId, verb)) {
      throw new AccessDeniedException(
          "Forbidden: missing verb '" + verb + "' on case type " + caseTypeId);
    }
  }
}
