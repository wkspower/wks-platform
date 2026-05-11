package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.response.CaseTypeSummaryDto;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto;
import com.wkspower.platform.api.mapper.CaseDtoMapper;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.exception.WksAuthenticationException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.WksUserPrincipal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public read surface for case types (Story 2.5 AC9). The list is filtered to entries the caller
 * has the {@code view} verb on — driving the frontend case-type filter dropdown without leaking
 * type metadata. The detail endpoint reuses the existing {@link CaseTypeViewDto} from Story 2.3 so
 * the case-list config-driven column generator and the case-detail Properties tab share one wire
 * shape.
 *
 * <p>Distinct from {@code /api/admin/case-types} (admin authoring path; not yet exposed) — this
 * surface is read-only and gated by the per-case-type {@code view} verb, not {@code ROLE_ADMIN}.
 *
 * <p>Anonymous callers are rejected by the JWT filter chain before reaching this controller.
 */
@RestController
@RequestMapping("/api/case-types")
public class CaseTypeController {

  private final CaseTypeReader reader;
  private final CaseTypePermissionEvaluator evaluator;

  /** Story 6.2 AC1 — for resolving outcomeMappings to surface on the case-type view endpoint. */
  private final MappingRegistry mappingRegistry;

  public CaseTypeController(
      CaseTypeReader reader,
      CaseTypePermissionEvaluator evaluator,
      MappingRegistry mappingRegistry) {
    this.reader = reader;
    this.evaluator = evaluator;
    this.mappingRegistry = mappingRegistry;
  }

  @GetMapping
  public ApiResponse<List<CaseTypeSummaryDto>> list(
      @AuthenticationPrincipal WksUserPrincipal actor) {
    requireAuthenticated(actor);
    List<CaseTypeSummaryDto> data =
        reader.all().stream()
            .filter(ct -> evaluator.hasVerb(actor.authenticated(), ct.id(), "view"))
            .sorted(
                Comparator.comparing(
                    CaseTypeConfig::displayName, Comparator.nullsLast(String::compareTo)))
            .map(
                ct ->
                    new CaseTypeSummaryDto(
                        ct.id(),
                        ct.displayName(),
                        ct.version(),
                        ct.statuses().size(),
                        ct.fields().size(),
                        List.copyOf(evaluator.verbsOf(actor.authenticated(), ct.id()))))
            .toList();
    return ApiResponse.success(data);
  }

  @GetMapping("/{id}")
  public ApiResponse<CaseTypeViewDto> get(
      @PathVariable("id") String id, @AuthenticationPrincipal WksUserPrincipal actor) {
    requireAuthenticated(actor);
    CaseTypeConfig caseType =
        reader
            .find(id)
            .orElseThrow(() -> new WksNotFoundException("Case type " + id + " not found"));
    if (!evaluator.hasVerb(actor.authenticated(), caseType.id(), "view")) {
      throw new AccessDeniedException(
          "Forbidden: missing verb 'view' on case type " + caseType.id());
    }
    // Story 6.2 AC1 — resolve outcomeMappings from the active mapping version, if registered.
    CaseTypeRef ref = new CaseTypeRef(caseType.id(), String.valueOf(caseType.version()));
    Optional<MappingDefinition> mapping =
        mappingRegistry.resolve(ref, String.valueOf(caseType.version()));
    return ApiResponse.success(CaseDtoMapper.toCaseTypeView(caseType, mapping));
  }

  private static void requireAuthenticated(WksUserPrincipal actor) {
    if (actor == null) {
      // JWT filter normally short-circuits before we get here; this is a defence-in-depth fallback
      // that maps to 401 (AuthN), not 403 (AuthZ), via GlobalExceptionHandler.
      throw new WksAuthenticationException("Authentication required");
    }
  }
}
