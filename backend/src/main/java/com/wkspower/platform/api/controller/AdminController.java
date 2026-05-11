package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.response.DeployResponseDto;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.GateOutcome;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.rebase.CaseRebaseReport;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.service.CaseRebaseService;
import com.wkspower.platform.domain.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin endpoints. Story 2.2 ships {@code POST /api/admin/deploy} — the developer / SI ops shape
 * for joint case-type + BPMN deploy. Thin: extracts multipart parts, calls {@link
 * ConfigService#deploy(byte[], byte[], String)}, maps the result into the wire DTO.
 *
 * <p>Story 3.9 — adds two rebase endpoints ({@code GET} dry-run + {@code POST} apply) for
 * single-case CaseType version migration.
 *
 * <p>Story 3.9 CF#1 — rewires {@link #emitAcceptedAuditLog} to accept the actual {@code
 * priorVersionNum} int and a {@link ForceOverrideReason} enum, replacing the hardcoded {@code
 * priorVersion=DYNAMIC} / {@code reason=WKS-CFG-030-unparseable} literals.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private static final Logger log = LoggerFactory.getLogger(AdminController.class);

  private final ConfigService configService;
  private final CaseRebaseService caseRebaseService;

  public AdminController(ConfigService configService, CaseRebaseService caseRebaseService) {
    this.configService = configService;
    this.caseRebaseService = caseRebaseService;
  }

  // -------------------------------------------------------------------------
  // Story 3.9 CF#1 — ForceOverrideReason enum (wire strings for audit log)
  // -------------------------------------------------------------------------

  /**
   * Story 3.9 CF#1 — reason enum for the {@code admin.deploy.force_override} audit log entry.
   * Private nested because the values are wire strings consumed only by audit log emission here;
   * extract to a top-level type if a second consumer materializes (YAGNI for now per story spec).
   *
   * <p>Wire strings are the public contract — never change them (memory {@code
   * feedback_error_codes_are_wire_contract.md} analogy applies here too).
   */
  enum ForceOverrideReason {
    /**
     * Force-override accepted; prior YAML was re-parsed (strictly or leniently) and classifier ran
     * normally. No WKS-CFG-030 bypass engaged.
     */
    LENIENT_SUCCESS("lenient-success"),

    /**
     * Force-override engaged the WKS-CFG-030 bypass path: prior YAML was unparseable even with the
     * lenient loader. Wire string preserved verbatim from Story 3-11 for grep-stability.
     */
    UNPARSEABLE_BYPASS("WKS-CFG-030-unparseable"),

    /** Rebase migration path. Distinct event ({@code admin.case.rebase}) uses this reason. */
    MIGRATION_REBASE("migration-rebase");

    private final String wireString;

    ForceOverrideReason(String wireString) {
      this.wireString = wireString;
    }

    String wireString() {
      return wireString;
    }
  }

  // -------------------------------------------------------------------------
  // Deploy endpoint
  // -------------------------------------------------------------------------

  @PostMapping(path = "/deploy", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Deploy a case-type, optionally with a BPMN",
      description =
          "Multipart body. Required: 'caseType' (YAML, ≤1 MB). Optional: 'bpmn' (BPMN 2.0 XML, ≤1"
              + " MB) — omit for zero-process case types (Story 3.2). Both validators run before"
              + " the registry is touched; on success the case type is registered, and when a BPMN"
              + " is present it is deployed to the embedded engine. A ConfigDeployed event is"
              + " published when a BPMN deployment occurs; YAML-only registrations skip the engine"
              + " deploy and return null deployment fields. Story 3.11: ?force=true is a"
              + " dev/test-only override for the WKS-CFG-030 unparseable-prior path, requires"
              + " ?bumpVersion=true alongside, and is rejected with WKS-API-006 in production"
              + " regardless of caller authority.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Deployed",
            content = @Content(schema = @Schema(implementation = DeployResponseDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Caller lacks ROLE_ADMIN",
            content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "413",
            description = "Multipart part exceeds the 1 MB cap (WKS-API-413)",
            content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description =
                "YAML or BPMN validation failed (multi-error envelope). When the failure code is"
                    + " WKS-CFG-029 (mutate-class change without bumpVersion), the response"
                    + " meta.blastRadius field carries the full BlastRadiusReport.",
            content = @Content)
      })
  public ApiResponse<DeployResponseDto> deploy(
      @RequestPart("caseType") MultipartFile caseTypePart,
      @RequestPart(name = "bpmn", required = false) MultipartFile bpmnPart,
      @RequestParam(name = "bumpVersion", required = false, defaultValue = "false")
          boolean bumpVersion,
      @RequestParam(name = "force", required = false, defaultValue = "false") boolean force,
      HttpServletRequest request)
      throws Exception {
    rejectDuplicateParts(request);
    String actorEmail = currentActorEmail();
    String caller = actorEmail == null ? "ANONYMOUS" : actorEmail;

    // Story 3.11 AC3 / AC4 / AC5 — pre-parse force-override gating. Reject in production
    // regardless of caller authority (production-policy); reject without bumpVersion=true even
    // in dev/test. Both rejection paths fail-fast BEFORE the multipart body is consumed so
    // YAML/BPMN bytes are never logged or read on rejected force requests.
    if (force) {
      if (configService.isProductionProfile()) {
        log.info(
            "event=admin.deploy.force_override caller={} caseTypeId=UNKNOWN priorVersion=NONE"
                + " outcome=REJECTED_PRODUCTION reason=production-policy",
            caller);
        throw new WksConfigException(
            List.of(
                ErrorDetail.of(
                    ErrorCode.WKS_API_006.wire(),
                    "Force-override deploy is not permitted in production profile — remove"
                        + " ?force=true and bump version explicitly.")),
            null);
      }
      if (!bumpVersion) {
        log.info(
            "event=admin.deploy.force_override caller={} caseTypeId=UNKNOWN priorVersion=NONE"
                + " outcome=REJECTED_NO_BUMP reason=missing-bumpVersion",
            caller);
        throw new WksConfigException(
            List.of(
                ErrorDetail.of(
                    ErrorCode.WKS_API_006.wire(),
                    "force=true requires bumpVersion=true — refusing to bypass blast-radius"
                        + " gate without explicit version bump.")),
            null);
      }
    }

    byte[] yaml = caseTypePart.getBytes();

    // Story 3.2: zero-process case types deploy YAML-only — no BPMN part means no engine deploy.
    // An attached but empty `bpmn` part is treated the same as absent.
    if (bpmnPart == null || bpmnPart.isEmpty()) {
      ValidationResult yamlResult =
          configService.validateAndRegister(
              "api-deploy.yaml", yaml, actorEmail, bumpVersion, force);
      if (yamlResult.isInvalid()) {
        // Story 3.8 — forward blast-radius meta (if any) into the exception so it surfaces in the
        // ApiResponse.meta field (AC2: meta.blastRadius must be present on WKS-CFG-029 rejections).
        throw new WksConfigException(
            yamlResult.errors(),
            yamlResult.responseMeta().isEmpty() ? null : yamlResult.responseMeta());
      }
      var caseType = yamlResult.config().orElseThrow();
      // Story 3.9 CF#1 — emit audit log only when the force path actually engaged and used a
      // non-NO_OVERRIDE_USED outcome. The reason is derived from the gate outcome threaded back
      // through ValidationResult.gateOutcome().
      if (force && yamlResult.gateOutcome() != GateOutcome.NO_OVERRIDE_USED) {
        ForceOverrideReason reason = toForceOverrideReason(yamlResult.gateOutcome());
        emitAcceptedAuditLog(caller, caseType.id(), yamlResult.priorVersionNum(), reason);
      }
      return ApiResponse.success(
          new DeployResponseDto(
              caseType.id(),
              caseType.version(),
              null,
              null,
              "/api/admin/case-types/" + caseType.id() + "/schema"));
    }

    byte[] bpmn = bpmnPart.getBytes();
    String bpmnFilename = bpmnPart.getOriginalFilename();
    DeployResult result =
        configService.deploy(yaml, bpmn, bpmnFilename, actorEmail, bumpVersion, force);
    if (result.isInvalid()) {
      // P10 — WKS-CFG-025 is an engine-side runtime failure (the input was valid; the engine
      // itself failed), not a client-input quality problem. Map it to HTTP 502 Bad Gateway via
      // WksWorkflowEngineException so the caller understands this is transient and retryable.
      // All other invalid results (YAML/BPMN validation errors) remain HTTP 422 via
      // WksConfigException (unprocessable entity — client must fix the input).
      boolean isEngineFailure =
          result.errors().stream().anyMatch(e -> ErrorCode.WKS_CFG_025.wire().equals(e.code()));
      if (isEngineFailure) {
        throw new WksWorkflowEngineException(
            "BPMN engine deployment failed (WKS-CFG-025) — retry or check engine health");
      }
      // Story 3.8 — forward blast-radius meta (if any)
      throw new WksConfigException(
          result.errors(), result.responseMeta().isEmpty() ? null : result.responseMeta());
    }

    var caseType = result.caseType().orElseThrow();
    var deployment = result.deployment().orElseThrow();
    // Story 3.9 CF#1 — emit audit log only when the force path actually engaged a non-NO_OVERRIDE
    // outcome.
    if (force && result.gateOutcome() != GateOutcome.NO_OVERRIDE_USED) {
      ForceOverrideReason reason = toForceOverrideReason(result.gateOutcome());
      emitAcceptedAuditLog(caller, caseType.id(), result.priorVersionNum(), reason);
    }
    return ApiResponse.success(
        new DeployResponseDto(
            caseType.id(),
            caseType.version(),
            deployment.deploymentId(),
            deployment.processDefinitionId(),
            "/api/admin/case-types/" + caseType.id() + "/schema"));
  }

  // -------------------------------------------------------------------------
  // Story 3.9 — Rebase endpoints
  // -------------------------------------------------------------------------

  /**
   * Story 3.9 AC1 — dry-run rebase: compute the structured field/status mapping report for a single
   * in-flight Case transitioning from its current CaseType version to {@code toVersion}, without
   * mutating any DB state.
   *
   * <p>The caller must supply {@code ?to=N} where N is the target CaseType version. Any validation
   * failure (case not found, caseTypeId mismatch, invalid toVersion) returns HTTP 422 with {@code
   * WKS-API-007}.
   */
  @GetMapping("/case-types/{caseTypeId}/cases/{caseId}/rebase")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Dry-run rebase of a Case to a newer CaseType version",
      description =
          "Computes the field/status mapping report for rebasing the specified Case from its current"
              + " caseTypeVersion to the requested ?to=N version. Does NOT mutate DB state."
              + " Returns HTTP 200 with a CaseRebaseReport (applied=false). Returns HTTP 422"
              + " (WKS-API-007) for invalid toVersion arguments or caseTypeId mismatch.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Dry-run report computed successfully",
            content = @Content(schema = @Schema(implementation = CaseRebaseReport.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Case not found",
            content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description = "Invalid toVersion argument or caseTypeId mismatch (WKS-API-007)",
            content = @Content)
      })
  public ApiResponse<CaseRebaseReport> rebaseDryRun(
      @PathVariable String caseTypeId,
      @PathVariable UUID caseId,
      @RequestParam(name = "to") int toVersion) {
    CaseRebaseReport report = caseRebaseService.dryRun(caseTypeId, caseId, toVersion);
    return ApiResponse.success(report);
  }

  /**
   * Story 3.9 AC2 / AC3 — apply rebase: atomically update {@code cases.case_type_version} from the
   * current version to the requested {@code ?to=N} version, but ONLY when there are no
   * irreconcilable items. When irreconcilable items exist, the request is rejected with HTTP 422 +
   * {@code WKS-CFG-034} (no DB mutation, no audit entry).
   *
   * <p>When the apply succeeds, an INFO-level audit log entry is emitted with {@code
   * event=admin.case.rebase} AFTER the transaction commits. The audit entry is NOT emitted on
   * failure.
   *
   * <p><b>Transactional contract:</b> the {@code @Transactional} boundary wraps the {@code
   * caseRebaseService.apply()} call. The audit log emission MUST be OUTSIDE the transaction
   * (post-return from this method's transactional boundary) so that a DB exception inside the
   * transaction does not produce a spurious audit entry. The audit log is emitted after {@code
   * apply()} returns successfully — see memory {@code
   * feedback_transactional_db_exception_postgres.md}.
   */
  @PostMapping("/case-types/{caseTypeId}/cases/{caseId}/rebase")
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Operation(
      summary = "Apply rebase of a Case to a newer CaseType version",
      description =
          "Atomically updates cases.case_type_version to the requested ?to=N version. Rejected with"
              + " HTTP 422 + WKS-CFG-034 when irreconcilable items exist (no DB mutation). Returns"
              + " HTTP 200 with CaseRebaseReport (applied=true) on success and emits an audit log"
              + " entry.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Rebase applied successfully",
            content = @Content(schema = @Schema(implementation = CaseRebaseReport.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Case not found",
            content = @Content),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description =
                "Invalid toVersion (WKS-API-007) or irreconcilable items present (WKS-CFG-034)",
            content = @Content)
      })
  public ApiResponse<CaseRebaseReport> rebaseApply(
      @PathVariable String caseTypeId,
      @PathVariable UUID caseId,
      @RequestParam(name = "to") int toVersion) {
    String actorEmail = currentActorEmail();
    String caller = actorEmail == null ? "ANONYMOUS" : actorEmail;

    CaseRebaseReport report = caseRebaseService.apply(caseTypeId, caseId, toVersion);

    // Audit log is emitted AFTER apply() returns successfully (post-transaction, as required by
    // memory feedback_transactional_db_exception_postgres.md). If apply() throws, we never reach
    // this line — no spurious audit entry.
    log.info(
        "event=admin.case.rebase caller={} caseTypeId={} caseId={} priorVersionNum={} toVersion={}"
            + " outcome=ACCEPTED reason={}",
        caller,
        caseTypeId,
        caseId,
        report.fromVersion(),
        report.toVersion(),
        ForceOverrideReason.MIGRATION_REBASE.wireString());

    return ApiResponse.success(report);
  }

  // -------------------------------------------------------------------------
  // CF#1 audit log emitter (rewired by Story 3.9)
  // -------------------------------------------------------------------------

  /**
   * Story 3.11 AC5 / Story 3.9 CF#1 — single-line audit entry for an accepted force-override
   * deploy. Emits the actual {@code priorVersionNum} int (not the former {@code DYNAMIC} literal)
   * and the {@code reason} enum wire string (not the former hardcoded {@code
   * WKS-CFG-030-unparseable}).
   *
   * <p><b>Invocation rule:</b> this method MUST be called OUTSIDE any active {@code @Transactional}
   * boundary (or in a {@code REQUIRES_NEW} propagation) so that a DB exception inside the prior
   * transaction does not produce a spurious audit entry. On the deploy path the controller method
   * is not itself {@code @Transactional}, so this holds by construction.
   *
   * @param caller authenticated email or {@code "ANONYMOUS"}
   * @param caseTypeId the CaseType id from the deployed YAML
   * @param priorVersionNum the prior version number BEFORE the deploy incremented it (0 when no
   *     prior version existed — should be filtered by the {@code gateOutcome != NO_OVERRIDE_USED}
   *     check at each invocation site)
   * @param reason the reason enum value for the audit log entry
   */
  private static void emitAcceptedAuditLog(
      String caller, String caseTypeId, int priorVersionNum, ForceOverrideReason reason) {
    log.info(
        "event=admin.deploy.force_override caller={} caseTypeId={} priorVersion={}"
            + " outcome=ACCEPTED reason={}",
        caller,
        caseTypeId,
        priorVersionNum,
        reason.wireString());
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private static ForceOverrideReason toForceOverrideReason(GateOutcome outcome) {
    return switch (outcome) {
      case LENIENT_SUCCESS -> ForceOverrideReason.LENIENT_SUCCESS;
      case UNPARSEABLE_BYPASS -> ForceOverrideReason.UNPARSEABLE_BYPASS;
      case NO_OVERRIDE_USED ->
          // Should not be reached — callers guard with gateOutcome != NO_OVERRIDE_USED
          ForceOverrideReason.LENIENT_SUCCESS;
    };
  }

  /**
   * Reject requests where multiple parts share the {@code caseType} or {@code bpmn} name — Spring's
   * {@code @RequestPart} silently keeps one and discards the rest. Surfacing the ambiguity as 422
   * is the only safe option; the operator must pick one.
   */
  private static void rejectDuplicateParts(HttpServletRequest request) throws Exception {
    long caseTypeCount =
        request.getParts().stream().filter(p -> "caseType".equals(p.getName())).count();
    long bpmnCount = request.getParts().stream().filter(p -> "bpmn".equals(p.getName())).count();
    if (caseTypeCount > 1 || bpmnCount > 1) {
      throw new WksConfigException(
          List.of(
              ErrorDetail.ofField(
                  ErrorCode.WKS_CFG_000.wire(),
                  "Multipart request contains duplicate part names — exactly one 'caseType' and one"
                      + " 'bpmn' are required (got caseType="
                      + caseTypeCount
                      + ", bpmn="
                      + bpmnCount
                      + ")",
                  caseTypeCount > 1 ? "caseType" : "bpmn")));
    }
  }

  private static String currentActorEmail() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth instanceof AnonymousAuthenticationToken) {
      return null;
    }
    return auth.getName();
  }
}
