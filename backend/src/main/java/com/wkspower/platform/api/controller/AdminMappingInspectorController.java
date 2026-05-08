package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.response.MappingInspectorDto;
import com.wkspower.platform.api.dto.response.RecentSignalsDto;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.RecentSignalEntry;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.domain.service.SignalAuditRingBuffer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Story 4.6 — Admin Mapping Inspector read-only API surface. Sibling controller to {@link
 * AdminController} (NOT a modification of it) to keep merge-disjoint from Story 3.11. All endpoints
 * gated by {@code ROLE_ADMIN}; failure paths (403/404) are NOT logged here — Spring Security
 * infrastructure already logs the rejections.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>{@code GET /api/admin/case-types/{caseTypeId}/mapping-inspector} — AC1, returns the active
 *       mapping projected as {@link MappingInspectorDto}.
 *   <li>{@code GET /api/admin/case-types/{caseTypeId}/recent-signals} — AC2, returns the ring
 *       buffer snapshot as {@link RecentSignalsDto} (capped at 50, newest-first, empty when no
 *       signals have been recorded for {@code caseTypeId}).
 * </ul>
 *
 * <p>AC7 — every successful response emits an INFO log line tagged with the controller name and
 * caller email for the admin-action audit trail.
 *
 * <p>Helper note: {@code currentActorEmail()} is duplicated from {@link AdminController} (4 lines).
 * Per memory {@code feedback_consolidate_property_readers.md} the 3+-instance threshold is not yet
 * hit; consolidation deferred to a Sprint 10 follow-up. This duplication is the deliberate
 * merge-disjoint guarantee with Story 3.11 (which modifies {@code AdminController}).
 */
@RestController
@RequestMapping("/api/admin/case-types")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMappingInspectorController {

  private static final Logger log = LoggerFactory.getLogger(AdminMappingInspectorController.class);

  private final MappingRegistry mappingRegistry;
  private final CaseTypeVersionRegistry versionRegistry;
  private final SignalAuditRingBuffer ringBuffer;

  public AdminMappingInspectorController(
      MappingRegistry mappingRegistry,
      CaseTypeVersionRegistry versionRegistry,
      SignalAuditRingBuffer ringBuffer) {
    this.mappingRegistry = mappingRegistry;
    this.versionRegistry = versionRegistry;
    this.ringBuffer = ringBuffer;
  }

  @GetMapping("/{caseTypeId}/mapping-inspector")
  public ApiResponse<MappingInspectorDto> mappingInspector(@PathVariable String caseTypeId) {
    int version =
        versionRegistry
            .currentVersion(caseTypeId)
            .orElseThrow(() -> new WksNotFoundException("Case type " + caseTypeId + " not found"));
    String versionStr = String.valueOf(version);
    MappingDefinition mapping =
        mappingRegistry
            .resolve(new CaseTypeRef(caseTypeId, versionStr), versionStr)
            .orElse(MappingDefinition.empty());
    log.info(
        "AdminMappingInspectorController: caller={} accessed mapping-inspector for caseTypeId={}",
        currentActorEmail(),
        caseTypeId);
    return ApiResponse.success(MappingInspectorDto.from(caseTypeId, versionStr, mapping));
  }

  @GetMapping("/{caseTypeId}/recent-signals")
  public ApiResponse<RecentSignalsDto> recentSignals(@PathVariable String caseTypeId) {
    List<RecentSignalEntry> entries = ringBuffer.recent(caseTypeId);
    log.info(
        "AdminMappingInspectorController: caller={} accessed recent-signals for caseTypeId={}",
        currentActorEmail(),
        caseTypeId);
    return ApiResponse.success(RecentSignalsDto.from(caseTypeId, entries));
  }

  /**
   * Story 4.6 AC7 — current authenticated actor email for admin-action audit logging. Duplicated
   * from {@link AdminController#currentActorEmail()} (4 lines) per the merge-disjoint guarantee
   * with Story 3.11; the 3+-instance threshold for extraction is not yet reached.
   */
  private static String currentActorEmail() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth instanceof AnonymousAuthenticationToken) {
      return null;
    }
    return auth.getName();
  }
}
