package com.wkspower.platform.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable domain record representing a document attached to a case (Story 14.2). Maps to the
 * {@code case_documents} table; JPA details live in {@code CaseDocumentEntity}.
 */
public record CaseDocument(
    UUID id,
    UUID caseId,
    String fileName,
    String contentType,
    long sizeBytes,
    String storageKey,
    String checksum,
    UUID uploadedBy,
    Instant uploadedAt) {}
