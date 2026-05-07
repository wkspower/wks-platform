package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.response.CaseDocumentDto;
import com.wkspower.platform.api.dto.response.PreviewResponse;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksDocumentException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseDocument;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.DocumentService;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.WksUserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST surface for case document operations (Story 14.2).
 *
 * <p>RBAC: document access inherits case RBAC. Every handler loads the associated case and checks
 * the caller's {@code view} verb on the case-type before proceeding.
 *
 * <ul>
 *   <li>{@code POST /api/cases/{caseId}/documents} — upload (AC1)
 *   <li>{@code GET /api/cases/{caseId}/documents} — list (AC2)
 *   <li>{@code GET /api/documents/{documentId}/download} — stream download (AC2)
 *   <li>{@code GET /api/documents/{documentId}/preview} — preview URL or inline stream (AC3)
 * </ul>
 */
@RestController
public class DocumentController {

  private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

  private final DocumentService documentService;
  private final CaseService caseService;
  private final CaseTypePermissionEvaluator evaluator;

  public DocumentController(
      DocumentService documentService,
      CaseService caseService,
      CaseTypePermissionEvaluator evaluator) {
    this.documentService = documentService;
    this.caseService = caseService;
    this.evaluator = evaluator;
  }

  // --- AC1: Upload ---

  /**
   * {@code POST /api/cases/{caseId}/documents} — multipart {@code file} field. Returns {@code 201
   * Created} with the document metadata envelope.
   */
  @PostMapping("/api/cases/{caseId}/documents")
  public ResponseEntity<ApiResponse<CaseDocumentDto>> upload(
      @PathVariable UUID caseId,
      @RequestParam("file") MultipartFile file,
      @AuthenticationPrincipal WksUserPrincipal actor)
      throws Exception {

    Case found = caseService.findById(caseId);
    requireView(actor, found.caseTypeId());

    String contentType =
        file.getContentType() != null ? file.getContentType() : "application/octet-stream";

    CaseDocument doc;
    try (InputStream stream = file.getInputStream()) {
      doc =
          documentService.upload(
              caseId,
              file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload",
              stream,
              contentType,
              file.getSize(),
              actor.id());
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(toDto(doc)));
  }

  // --- AC2: List ---

  /** {@code GET /api/cases/{caseId}/documents} — ordered by uploadedAt DESC. */
  @GetMapping("/api/cases/{caseId}/documents")
  public ApiResponse<List<CaseDocumentDto>> list(
      @PathVariable UUID caseId, @AuthenticationPrincipal WksUserPrincipal actor) {

    Case found = caseService.findById(caseId);
    requireView(actor, found.caseTypeId());

    List<CaseDocumentDto> dtos =
        documentService.listByCase(caseId).stream().map(DocumentController::toDto).toList();
    return ApiResponse.success(dtos);
  }

  // --- AC2: Download ---

  /**
   * {@code GET /api/documents/{documentId}/download} — streams the file. Default disposition is
   * {@code attachment}. Pass {@code ?inline=true} to serve with {@code Content-Disposition: inline}
   * so browsers can render PDF/images inside an {@code <iframe>} or {@code <img>} (P8 — local-store
   * preview path). The download endpoint always uses {@code attachment}; the preview endpoint URLs
   * use {@code ?inline=true} for previewable types in local-store mode.
   */
  @GetMapping("/api/documents/{documentId}/download")
  public void download(
      @PathVariable UUID documentId,
      @RequestParam(name = "inline", required = false, defaultValue = "false") boolean inline,
      @AuthenticationPrincipal WksUserPrincipal actor,
      HttpServletResponse response)
      throws Exception {

    CaseDocument doc = requireDocument(documentId);
    requireDocumentAccess(actor, doc);

    response.setContentType(doc.contentType());
    // P2: RFC 5987 encoding — eliminates injection via quotes/CRLF in filenames.
    String encoded = URLEncoder.encode(doc.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
    // P8: inline disposition for preview path, attachment for direct downloads.
    String disposition = inline ? "inline" : "attachment";
    response.setHeader("Content-Disposition", disposition + "; filename*=UTF-8''" + encoded);
    response.setHeader("Content-Length", String.valueOf(doc.sizeBytes()));

    try (InputStream stream = documentService.openStream(doc);
        OutputStream out = response.getOutputStream()) {
      stream.transferTo(out);
    } catch (WksDocumentException e) {
      throw e;
    } catch (Exception e) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_005, "Failed to stream document: " + e.getMessage(), e);
    }
  }

  // --- AC3: Preview ---

  /**
   * {@code GET /api/documents/{documentId}/preview} — returns a {@link PreviewResponse}. For
   * PDF/image types: presigned URL (MinIO) or download URL (local). For office types: {@code
   * previewable=false} with a download URL.
   */
  @GetMapping(
      value = "/api/documents/{documentId}/preview",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<PreviewResponse> preview(
      @PathVariable UUID documentId, @AuthenticationPrincipal WksUserPrincipal actor) {

    CaseDocument doc = requireDocument(documentId);
    requireDocumentAccess(actor, doc);

    boolean previewable = documentService.isPreviewable(doc.contentType());
    String url;

    if (previewable) {
      String presigned = documentService.getPresignedUrl(doc);
      if (presigned != null) {
        // P7: log audit line when presigned URL is generated.
        log.info(
            "Presigned preview URL generated: documentId={} actorId={}", documentId, actor.id());
        url = presigned;
      } else {
        // P8: local-store preview — use inline disposition so browsers render in-frame.
        url = "/api/documents/" + documentId + "/download?inline=true";
      }
    } else {
      url = "/api/documents/" + documentId + "/download";
    }

    return ApiResponse.success(new PreviewResponse(previewable, url));
  }

  // --- helpers ---

  private CaseDocument requireDocument(UUID documentId) {
    return documentService
        .findById(documentId)
        .orElseThrow(
            () ->
                new WksDocumentException(
                    ErrorCode.WKS_DOC_004, "Document " + documentId + " not found"));
  }

  private void requireDocumentAccess(WksUserPrincipal actor, CaseDocument doc) {
    // Access check: load the case (throws 404 if gone), then verify view verb.
    Case owningCase = caseService.findById(doc.caseId());
    requireView(actor, owningCase.caseTypeId());
  }

  private void requireView(WksUserPrincipal actor, String caseTypeId) {
    if (actor == null || !evaluator.hasVerb(actor.authenticated(), caseTypeId, "view")) {
      throw new AccessDeniedException("Forbidden: missing 'view' verb on case type " + caseTypeId);
    }
  }

  private static CaseDocumentDto toDto(CaseDocument doc) {
    return new CaseDocumentDto(
        doc.id(),
        doc.caseId(),
        doc.fileName(),
        doc.contentType(),
        doc.sizeBytes(),
        doc.uploadedBy(),
        doc.uploadedAt());
  }
}
