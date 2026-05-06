package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.request.AppendStatusRequest;
import com.wkspower.platform.api.dto.request.RenameStatusRequest;
import com.wkspower.platform.api.dto.response.StageStatusSetDto;
import com.wkspower.platform.api.dto.response.StatusView;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksStageException;
import com.wkspower.platform.domain.exception.WksValidationException;
import com.wkspower.platform.domain.port.StatusOptionsStore;
import com.wkspower.platform.domain.service.StatusOptionsAdminService;
import com.wkspower.platform.domain.service.StatusOptionsAdminService.LookupKind;
import com.wkspower.platform.domain.service.StatusOptionsAdminService.ResolvedStatusSet;
import com.wkspower.platform.domain.service.StatusOptionsAdminService.StatusAdminLookupFailure;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Story 3.7 — admin endpoints for stage-scoped status set CRUD. Append-class per Decision 21:
 * append + rename succeed without bumping the CaseType version; mutate-class operations (DELETE,
 * PATCH terminal) are rejected with {@code 405 / WKS-STG-009} pending Story 3.8's version-bump
 * envelope.
 *
 * <p>Live-propagation strategy: option (a) — every read re-resolves against {@code status_options}.
 * No cache. See {@link com.wkspower.platform.domain.service.StatusOptionsResolver}.
 *
 * <p>Sits under {@code /api/admin/case-types/{caseTypeId}/stages/{stageId}/statuses}; gated by
 * {@code ROLE_ADMIN} (matches existing {@link AdminController#deploy} surface — there is no
 * per-case-type verb on the admin path).
 */
@RestController
@RequestMapping("/api/admin/case-types/{caseTypeId}/stages/{stageId}/statuses")
public class CaseTypeStatusAdminController {

  private final StatusOptionsAdminService service;

  public CaseTypeStatusAdminController(StatusOptionsAdminService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<StageStatusSetDto> get(
      @PathVariable("caseTypeId") String caseTypeId, @PathVariable("stageId") String stageId) {
    ResolvedStatusSet resolved = mapLookupErrors(() -> service.resolve(caseTypeId, stageId));
    return ApiResponse.success(toDto(resolved));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<StatusView>> append(
      @PathVariable("caseTypeId") String caseTypeId,
      @PathVariable("stageId") String stageId,
      @Valid @RequestBody AppendStatusRequest body) {
    requireBody(body);
    requireField(body.id(), "id");
    requireField(body.displayName(), "displayName");
    requireField(body.color(), "color");
    String color = validateColor(body.color());
    boolean terminal = body.terminal() != null && body.terminal();
    StatusDefinition appended;
    try {
      appended =
          mapLookupErrors(
              () ->
                  service.append(
                      caseTypeId, stageId, body.id(), body.displayName(), color, terminal));
    } catch (StatusOptionsStore.DuplicateStatusException dup) {
      throw new WksStageException(
          ErrorCode.WKS_STG_007,
          "status '" + body.id() + "' already exists on " + caseTypeId + " stage=" + stageId);
    }
    StatusView view =
        new StatusView(
            appended.id(),
            appended.displayName(),
            appended.color() == null ? null : appended.color().wire(),
            appended.terminal(),
            // Ordinal is illustrative on the wire — the resolver re-orders on read; for the POST
            // response we report 0 (the controller does not have access to the persisted ordinal
            // and a follow-up GET surfaces the canonical position).
            0);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(view));
  }

  @PatchMapping("/{statusId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<StatusView> rename(
      @PathVariable("caseTypeId") String caseTypeId,
      @PathVariable("stageId") String stageId,
      @PathVariable("statusId") String statusId,
      @RequestBody(required = false) RenameStatusRequest body) {
    requireBody(body);
    if (body.terminal() != null) {
      // Mutate-class — flipping the terminal flag requires Story 3.8's version-bump envelope.
      throw new WksStageException(
          ErrorCode.WKS_STG_009,
          "Changing the 'terminal' flag on an existing status is a mutate-class operation; "
              + "remove / terminal-flag changes require Story 3.8 mutate-class enforcement; deferred.");
    }
    if ((body.displayName() == null || body.displayName().isBlank())
        && (body.color() == null || body.color().isBlank())) {
      throw new WksValidationException(
          ErrorCode.WKS_API_001,
          "PATCH body must include at least one of 'displayName' or 'color'",
          "displayName");
    }
    String color = body.color() == null ? null : validateColor(body.color());
    StatusDefinition renamed =
        mapLookupErrors(
            () -> service.rename(caseTypeId, stageId, statusId, body.displayName(), color));
    return ApiResponse.success(
        new StatusView(
            renamed.id(),
            renamed.displayName(),
            renamed.color() == null ? null : renamed.color().wire(),
            renamed.terminal(),
            0));
  }

  @DeleteMapping("/{statusId}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(
      @PathVariable("caseTypeId") String caseTypeId,
      @PathVariable("stageId") String stageId,
      @PathVariable("statusId") String statusId) {
    throw new WksStageException(
        ErrorCode.WKS_STG_009,
        "DELETE on a status is a mutate-class operation; remove / terminal-flag changes "
            + "require Story 3.8 mutate-class enforcement; deferred.");
  }

  // --- helpers --------------------------------------------------------------

  private static StageStatusSetDto toDto(ResolvedStatusSet resolved) {
    List<StatusView> views = new ArrayList<>(resolved.statuses().size());
    for (int i = 0; i < resolved.statuses().size(); i++) {
      StatusDefinition s = resolved.statuses().get(i);
      views.add(
          new StatusView(
              s.id(),
              s.displayName(),
              s.color() == null ? null : s.color().wire(),
              s.terminal(),
              i));
    }
    return new StageStatusSetDto(resolved.stageId(), resolved.initialStatus(), views);
  }

  private static <T> T mapLookupErrors(java.util.function.Supplier<T> action) {
    try {
      return action.get();
    } catch (StatusAdminLookupFailure missing) {
      ErrorCode code =
          missing.kind() == LookupKind.STATUS ? ErrorCode.WKS_STG_013 : ErrorCode.WKS_STG_012;
      throw new WksStageException(code, missing.getMessage());
    }
  }

  private static void requireBody(Object body) {
    if (body == null) {
      throw new WksValidationException(ErrorCode.WKS_API_001, "Request body is required", null);
    }
  }

  private static void requireField(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new WksValidationException(
          ErrorCode.WKS_API_001, "Field '" + field + "' is required", field);
    }
  }

  private static String validateColor(String wire) {
    return StatusColor.fromWire(wire)
        .map(StatusColor::wire)
        .orElseThrow(
            () ->
                new WksValidationException(
                    ErrorCode.WKS_CFG_008, "Unknown status color: " + wire, "color"));
  }
}
