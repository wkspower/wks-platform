package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.response.DeployResponseDto;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private static final Logger log = LoggerFactory.getLogger(AdminController.class);

  private final ConfigService configService;

  public AdminController(ConfigService configService) {
    this.configService = configService;
  }

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
      // Story 3.11 AC5 — ACCEPTED audit log fires after caseTypeId is known. Whether the
      // service-side WARN fired depends on whether the gate actually bypassed the classifier
      // (lenient succeeded → WARN suppressed). The controller log is the audit trail; the
      // service log is the gate-bypass record. Distinct entries by design.
      if (force) {
        emitAcceptedAuditLog(caller, caseType.id());
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
    // Story 3.11 AC5 — ACCEPTED audit log on the BPMN-attached deploy path.
    if (force) {
      emitAcceptedAuditLog(caller, caseType.id());
    }
    return ApiResponse.success(
        new DeployResponseDto(
            caseType.id(),
            caseType.version(),
            deployment.deploymentId(),
            deployment.processDefinitionId(),
            "/api/admin/case-types/" + caseType.id() + "/schema"));
  }

  /**
   * Story 3.11 AC5 — single-line audit entry for an accepted force-override deploy. caseTypeId is
   * known here (post-parse); priorVersion is left as a dynamic value the service-layer log trail
   * surfaces. The accepted entry fires whenever {@code force=true} is honored at the controller
   * layer, even when the service-side WKS-CFG-030 bypass did NOT engage (e.g. lenient parse
   * succeeded — the controller still saw a force request and audits it).
   */
  private static void emitAcceptedAuditLog(String caller, String caseTypeId) {
    log.info(
        "event=admin.deploy.force_override caller={} caseTypeId={} priorVersion=DYNAMIC"
            + " outcome=ACCEPTED reason=WKS-CFG-030-unparseable",
        caller,
        caseTypeId);
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
