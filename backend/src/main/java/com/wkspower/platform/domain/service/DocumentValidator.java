package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksDocumentException;
import java.io.IOException;
import java.io.InputStream;
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

  /** MIME types that the preview endpoint can render inline (AC3). image/svg+xml excluded (D6). */
  public static final Set<String> PREVIEWABLE_CONTENT_TYPES =
      Set.of("application/pdf", "image/jpeg", "image/png", "image/gif", "image/webp");

  /** Executable / dangerous extensions blocked by the filename sanitizer (AC1). P3 patch. */
  private static final Set<String> BLOCKED_EXTENSIONS =
      Set.of(
          ".exe", ".sh", ".bat", ".cmd", ".ps1", ".js", ".py", ".html", ".htm", ".jar", ".vbs",
          ".hta", ".phtml", ".php");

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

  /** Returns true when the content type can be previewed inline (PDF or explicit image types). */
  public static boolean isPreviewable(String contentType) {
    // P11: explicit allowlist only — no contentType.startsWith("image/") wildcard.
    return contentType != null && PREVIEWABLE_CONTENT_TYPES.contains(contentType);
  }

  /**
   * P1 — Magic-byte verification. Reads the first 16 bytes from {@code stream} (without consuming
   * the stream beyond those bytes — the caller must use a {@link java.io.BufferedInputStream} or
   * pass a {@link java.io.PushbackInputStream} if re-reading is needed; in practice the service
   * wraps the stream with {@link java.io.PushbackInputStream} before calling this). Returns
   * normally when the declared content type is consistent with the detected magic bytes, or when no
   * magic signature applies (e.g. text/plain, text/csv). Throws {@link WksDocumentException}
   * ({@code WKS-DOC-002}) when the declared MIME type is inconsistent with the detected signature.
   *
   * @param headerBytes first bytes of the file (at least 16, or fewer for tiny files)
   * @param contentType declared MIME type (already validated against the allowlist)
   */
  public static void validateMagicBytes(byte[] headerBytes, String contentType) {
    if (headerBytes == null || headerBytes.length == 0) {
      return; // nothing to check
    }

    // Detect magic signature from the header bytes.
    String detected = detectMimeFromMagic(headerBytes);

    if (detected == null) {
      // No recognisable magic — pass through (covers text/plain, text/csv, etc.).
      return;
    }

    if (!detected.equals(contentType)) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_002,
          "Declared content type '"
              + contentType
              + "' does not match file magic bytes (detected: '"
              + detected
              + "')");
    }
  }

  /**
   * Reads up to 16 bytes from {@code stream} for magic-byte detection. The stream position is
   * advanced by the number of bytes read (callers should wrap with PushbackInputStream).
   */
  public static byte[] readHeaderBytes(InputStream stream) throws IOException {
    byte[] buf = new byte[16];
    int read = stream.readNBytes(buf, 0, buf.length);
    if (read < buf.length) {
      byte[] trimmed = new byte[read];
      System.arraycopy(buf, 0, trimmed, 0, read);
      return trimmed;
    }
    return buf;
  }

  /** Returns a MIME type string when the header bytes match a known magic signature, else null. */
  private static String detectMimeFromMagic(byte[] b) {
    int len = b.length;
    // PDF: 25 50 44 46
    if (len >= 4 && b[0] == 0x25 && b[1] == 0x50 && b[2] == 0x44 && b[3] == 0x46) {
      return "application/pdf";
    }
    // PNG: 89 50 4E 47 0D 0A 1A 0A
    if (len >= 8
        && (b[0] & 0xFF) == 0x89
        && b[1] == 0x50
        && b[2] == 0x4E
        && b[3] == 0x47
        && b[4] == 0x0D
        && b[5] == 0x0A
        && b[6] == 0x1A
        && b[7] == 0x0A) {
      return "image/png";
    }
    // JPEG: FF D8 FF
    if (len >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
      return "image/jpeg";
    }
    // ZIP / docx / xlsx / jar: 50 4B 03 04
    if (len >= 4 && b[0] == 0x50 && b[1] == 0x4B && b[2] == 0x03 && b[3] == 0x04) {
      // ZIP magic is shared by multiple formats; caller declared the specific subtype.
      // Return a sentinel so the caller can match Office types.
      return "application/zip-family";
    }
    // GIF87a / GIF89a: 47 49 46 38
    if (len >= 4 && b[0] == 0x47 && b[1] == 0x49 && b[2] == 0x46 && b[3] == 0x38) {
      return "image/gif";
    }
    return null;
  }

  /** ZIP-family MIME types that share the PK magic header (docx, xlsx, odt, …). */
  private static final Set<String> ZIP_FAMILY_TYPES =
      Set.of(
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "application/zip");

  /**
   * Validates declared content type against magic-byte detection, allowing ZIP-family types (docx,
   * xlsx) to pass when the magic resolves to {@code application/zip-family}.
   */
  public static void validateMagicBytesWithZipFamily(byte[] headerBytes, String contentType) {
    if (headerBytes == null || headerBytes.length == 0) {
      return;
    }
    String detected = detectMimeFromMagic(headerBytes);
    if (detected == null) {
      return; // no magic match — pass through (text/plain, text/csv, etc.)
    }
    if ("application/zip-family".equals(detected) && ZIP_FAMILY_TYPES.contains(contentType)) {
      return; // ZIP magic is consistent with declared Office/zip type
    }
    if (!detected.equals(contentType)) {
      throw new WksDocumentException(
          ErrorCode.WKS_DOC_002,
          "Declared content type '"
              + contentType
              + "' does not match file magic bytes (detected: '"
              + detected
              + "')");
    }
  }
}
