package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.response.DeployResponseDto;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
              + " deploy and return null deployment fields.")
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
            description = "YAML or BPMN validation failed (multi-error envelope)",
            content = @Content)
      })
  public ApiResponse<DeployResponseDto> deploy(
      @RequestPart("caseType") MultipartFile caseTypePart,
      @RequestPart(name = "bpmn", required = false) MultipartFile bpmnPart,
      HttpServletRequest request)
      throws Exception {
    rejectDuplicateParts(request);
    byte[] yaml = caseTypePart.getBytes();
    String actorEmail = currentActorEmail();

    // Story 3.2: zero-process case types deploy YAML-only — no BPMN part means no engine deploy.
    // An attached but empty `bpmn` part is treated the same as absent.
    if (bpmnPart == null || bpmnPart.isEmpty()) {
      ValidationResult yamlResult = configService.validateAndRegister("api-deploy.yaml", yaml);
      if (yamlResult.isInvalid()) {
        throw new WksConfigException(yamlResult.errors());
      }
      var caseType = yamlResult.config().orElseThrow();
      return ApiResponse.success(
          new DeployResponseDto(
              caseType.id(),
              caseType.version(),
              null,
              null,
              "/api/admin/case-types/" + caseType.id() + "/schema"));
    }

    byte[] bpmn = bpmnPart.getBytes();
    DeployResult result = configService.deploy(yaml, bpmn, actorEmail);
    if (result.isInvalid()) {
      throw new WksConfigException(result.errors());
    }

    var caseType = result.caseType().orElseThrow();
    var deployment = result.deployment().orElseThrow();
    return ApiResponse.success(
        new DeployResponseDto(
            caseType.id(),
            caseType.version(),
            deployment.deploymentId(),
            deployment.processDefinitionId(),
            "/api/admin/case-types/" + caseType.id() + "/schema"));
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
