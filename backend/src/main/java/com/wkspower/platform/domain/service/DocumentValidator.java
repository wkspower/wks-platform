package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksDocumentException;
import java.util.Set;

/**
 * Pure-Java document upload validator (Story 14.2 AC1). Framework-free — no Spring, no JPA. Used by
 * the infrastructure-layer {@code DocumentApplicationService} which supplies the configured
 * max-size value from {@code @Value}.
 */
public final class DocumentValidator {

  /** MIME types allowed for upload (AC1). */
  public static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of(
          "application/pdf",
          "image/jpeg",
          "image/png",
          "image/gif",
          "image/webp",
          "image/svg+xml",
          "application/msword",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "application/vnd.ms-excel",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "text/plain",
          "text/csv");

  /** MIME types that the preview endpoint can render inline (AC3). */
  public static final Set<String> PREVIEWABLE_CONTENT_TYPES =
      Set.of(
          "application/pdf", "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml");

  /** Executable / dangerous extensions blocked by the filename sanitizer (AC1). */
  private static final Set<String> BLOCKED_EXTENSIONS =
      Set.of(".exe", ".sh", ".bat", ".cmd", ".ps1", ".js", ".py");

  private DocumentValidator() {}

  /**
   * Validates declared size against the configured max.
   *
   * @throws WksDocumentException (WKS-DOC-001) if size exceeds maxBytes
   */
  public static void validateSize(long sizeBytes, long maxSizeMb) {
    long maxBytes = maxSizeMb * 1024L * 1024L;
    if (sizeBytes > maxBytes) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_001,
          "File size " + sizeBytes + " bytes exceeds the " + maxSizeMb + " MB limit");
    }
  }

  /**
   * Validates the content type against the upload allowlist.
   *
   * @throws WksDocumentException (WKS-DOC-002) if not in allowlist
   */
  public static void validateContentType(String contentType) {
    if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_002,
          "Content type '" + contentType + "' is not in the upload allowlist");
    }
  }

  /**
   * Validates and returns the sanitized filename (no path traversal, no blocked extensions).
   *
   * @throws WksDocumentException (WKS-DOC-003) if filename is invalid
   */
  public static String sanitizeFileName(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      throw new WksDocumentException(ErrorCode.WKS_DOC_003, "Filename must not be blank");
    }
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_003, "Filename contains path traversal characters");
    }
    String lower = fileName.toLowerCase();
    for (String ext : BLOCKED_EXTENSIONS) {
      if (lower.endsWith(ext)) {
        throw new WksDocumentException(
            ErrorCode.WKS_DOC_003, "Filename has a blocked extension: " + ext);
      }
    }
    return fileName;
  }

  /** Returns true when the content type can be previewed inline (PDF or images). */
  public static boolean isPreviewable(String contentType) {
    return contentType != null
        && (PREVIEWABLE_CONTENT_TYPES.contains(contentType) || contentType.startsWith("image/"));
  }
}
