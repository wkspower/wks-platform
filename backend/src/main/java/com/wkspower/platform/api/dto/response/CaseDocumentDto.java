package com.wkspower.platform.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire DTO for a document metadata record returned by {@code DocumentController} (Story 14.2 AC1 /
 * AC2). The {@code storageKey} and {@code checksum} fields are intentionally omitted from the
 * response — they are internal storage details.
 */
public record CaseDocumentDto(
    UUID id,
    UUID caseId,
    String fileName,
    String contentType,
    long sizeBytes,
    UUID uploadedBy,
    Instant uploadedAt) {}
