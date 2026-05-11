package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.response.CaseDto;
import com.wkspower.platform.api.mapper.CaseDtoMapper;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksValidationException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.FormDraftService;
import com.wkspower.platform.security.WksUserPrincipal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST surface for form submissions (Story 5.2 AC3). The only endpoint is:
 *
 * <pre>
 *   POST /api/cases/{caseId}/forms/{formId}/submit
 * </pre>
 *
 * <p>RBAC: {@code isAuthenticated()} — all authenticated users may attempt a form submit. The
 * domain service validates field constraints and emits WKS-FORM-002 on failure. Case-level
 * permission checks (whether the actor may edit this specific case type) are delegated to {@link
 * CaseService#submitForm} via the case-type RBAC enforced by the underlying {@code update} call.
 *
 * <p>No new {@code @ActiveProfiles("production")} ITs needed — standard Spring controller, no
 * production-profile specifics (Story 5.2 sprint lesson).
 */
@RestController
@RequestMapping("/api/cases")
public class FormController {

  private final CaseService caseService;
  private final StageRepository stageRepository;
  private final FormDraftService formDraftService;
  /**
   * Story 5.5 AC-4 — used to resolve the pinned CaseTypeConfig for the DTO response. The response
   * must embed the pinned CaseType (not the latest deployed) so the frontend renders the form the
   * case is bound to (Decision D20 frozen-on-version).
   */
  private final CaseTypeReader caseTypeReader;

  public FormController(
      CaseService caseService,
      StageRepository stageRepository,
      FormDraftService formDraftService,
      CaseTypeReader caseTypeReader) {
    this.caseService = caseService;
    this.stageRepository = stageRepository;
    this.formDraftService = formDraftService;
    this.caseTypeReader = caseTypeReader;
  }

  /**
   * Submit form data for an existing case. The request body is a flat {@code Map<String, Object>}
   * containing the submitted field values keyed by field id.
   *
   * <p>On success: the case data is updated, a {@link
   * com.wkspower.platform.domain.event.FormSubmitted} event is published, and the updated {@link
   * CaseDto} is returned (with embedded case-type view so the frontend can re-render without a
   * second round-trip).
   *
   * <p>On validation failure (WKS-FORM-002): HTTP 422 with field-level errors in the envelope.
   *
   * @param caseId the case to update
   * @param formId the form definition id declared on the case type
   * @param formData submitted field values (may be {@code null} — treated as empty)
   * @param actor the authenticated caller
   * @return the updated {@link CaseDto}
   */
  @PostMapping("/{caseId}/forms/{formId}/submit")
  @PreAuthorize("isAuthenticated()")
  @Transactional
  public ResponseEntity<ApiResponse<CaseDto>> submit(
      @PathVariable UUID caseId,
      @PathVariable String formId,
      @RequestBody(required = false) Map<String, Object> formData,
      @AuthenticationPrincipal WksUserPrincipal actor) {

    if (formData == null || formData.isEmpty()) {
      throw new WksValidationException(
          ErrorCode.WKS_FORM_003, "Form submission body is null or empty", null);
    }

    // Story 5.6 AC-0 — case-level access gate, single source of truth shared with
    // FormDraftController.
    // Throws WksNotFoundException (→ 404) if the case is missing — same wire shape as before.
    java.util.Set<String> actorRoles =
        actor.authenticated() == null ? java.util.Set.of() : actor.authenticated().roles();
    caseService.requireCaseAccess(caseId, actor.id(), actorRoles);

    // Story 5.6 AC2 — principal-aware overload threads the actor's role set through to the
    // per-field editableBy permission check.
    Case updated = caseService.submitForm(caseId, formId, formData, actor.id(), actorRoles);

    // Story 5.4 AC6 — delete-on-submit. Lives inside the controller's @Transactional so a
    // post-submit failure rolls back the deletion (preserving the draft) per AC6.
    formDraftService.deleteDraft(caseId, formId, actor.id());

    // Story 5.5 AC-4 — embed the PINNED CaseType in the response DTO (Decision D20). The
    // frontend FormPage reads caseDto.caseType.forms to render the form definition; it must see
    // the v-pinned form, not the latest deployed CaseType. Falls back to latest if pinned version
    // is not in registry (defensive — should not occur after resolveFormDefinitionForCase succeeded
    // above, but guards against race conditions on the read-only response path).
    CaseTypeConfig caseType =
        caseTypeReader
            .findVersion(updated.caseTypeId(), updated.caseTypeVersion())
            .orElseGet(() -> caseService.requireCaseType(updated.caseTypeId()));
    List<com.wkspower.platform.domain.model.Stage> history = stageRepository.loadHistory(caseId);

    CaseDto dto = CaseDtoMapper.toDto(updated, caseType, history);
    return ResponseEntity.ok(ApiResponse.success(dto));
  }
}
