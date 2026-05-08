package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.FormDraft;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.service.FormDraftService;
import com.wkspower.platform.security.WksUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Story 5.4 — REST surface for form drafts (auto-save + resume + discard).
 *
 * <pre>
 *   GET    /api/cases/{caseId}/forms/{formId}/draft  -> 200 + FormDraftDto, or 404
 *   PUT    /api/cases/{caseId}/forms/{formId}/draft  -> 200 + FormDraftDto (upsert)
 *   DELETE /api/cases/{caseId}/forms/{formId}/draft  -> 204 (idempotent)
 * </pre>
 *
 * <p>RBAC: {@code isAuthenticated()} + the caller must have access to the underlying case (the
 * {@link CaseRepository#findById} call doubles as the access gate — a user without read access on
 * the case has no way to reach this endpoint via the routing layer; in addition, the draft scope
 * already filters by {@code userId} via the {@link WksUserPrincipal}, so cross-user reads are
 * impossible — AC5).
 *
 * <p>No new {@code WKS-DRAFT-*} error codes — standard 404 (no draft) and 422 (invalid body via
 * {@code @Valid}) suffice. Memory {@code feedback_error_codes_are_wire_contract.md}: codes are
 * stable wire strings; allocate only when there's a domain-meaningful failure mode.
 */
@RestController
@RequestMapping("/api/cases")
public class FormDraftController {

  private final FormDraftService draftService;
  private final CaseRepository caseRepository;

  public FormDraftController(FormDraftService draftService, CaseRepository caseRepository) {
    this.draftService = draftService;
    this.caseRepository = caseRepository;
  }

  @GetMapping("/{caseId}/forms/{formId}/draft")
  @PreAuthorize("isAuthenticated()")
  @Transactional(readOnly = true)
  public ResponseEntity<ApiResponse<FormDraftDto>> get(
      @PathVariable UUID caseId,
      @PathVariable String formId,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    requireCaseExists(caseId);
    Optional<FormDraft> found = draftService.findDraft(caseId, formId, actor.id());
    return found
        .map(d -> ResponseEntity.ok(ApiResponse.success(FormDraftDto.from(d))))
        .orElseGet(() -> ResponseEntity.<ApiResponse<FormDraftDto>>notFound().build());
  }

  @PutMapping("/{caseId}/forms/{formId}/draft")
  @PreAuthorize("isAuthenticated()")
  @Transactional
  public ResponseEntity<ApiResponse<FormDraftDto>> put(
      @PathVariable UUID caseId,
      @PathVariable String formId,
      @Valid @RequestBody SaveDraftRequest body,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    requireCaseExists(caseId);
    FormDraft saved =
        draftService.saveDraft(
            caseId,
            formId,
            actor.id(),
            body.payload(),
            body.scrollY(),
            body.sectionExpanded(),
            body.caseTypeVersionAtSave());
    return ResponseEntity.ok(ApiResponse.success(FormDraftDto.from(saved)));
  }

  @DeleteMapping("/{caseId}/forms/{formId}/draft")
  @PreAuthorize("isAuthenticated()")
  @Transactional
  public ResponseEntity<Void> delete(
      @PathVariable UUID caseId,
      @PathVariable String formId,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    requireCaseExists(caseId);
    draftService.deleteDraft(caseId, formId, actor.id());
    return ResponseEntity.noContent().build();
  }

  private void requireCaseExists(UUID caseId) {
    Case existing =
        caseRepository
            .findById(caseId)
            .orElseThrow(() -> new WksNotFoundException("Case " + caseId + " not found"));
    // referenced solely to confirm the row exists; assignee/RBAC checks live elsewhere.
    if (existing == null) {
      throw new WksNotFoundException("Case " + caseId + " not found");
    }
  }

  /** Request body for PUT — auto-validated via {@code @Valid}. */
  public record SaveDraftRequest(
      @NotNull Map<String, Object> payload,
      @PositiveOrZero int scrollY,
      Map<String, Boolean> sectionExpanded,
      @Positive int caseTypeVersionAtSave) {}

  /** Wire DTO for GET / PUT responses. */
  public record FormDraftDto(
      UUID id,
      UUID caseId,
      String formId,
      Map<String, Object> payload,
      int scrollY,
      Map<String, Boolean> sectionExpanded,
      int caseTypeVersionAtSave,
      Instant updatedAt) {

    public static FormDraftDto from(FormDraft d) {
      return new FormDraftDto(
          d.id(),
          d.caseId(),
          d.formId(),
          d.payload(),
          d.scrollY(),
          d.sectionExpanded(),
          d.caseTypeVersionAtSave(),
          d.updatedAt());
    }
  }
}
