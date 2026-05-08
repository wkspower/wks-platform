package com.wkspower.platform.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Story 5.4 — domain record for a saved form draft. Carries the in-progress {@code payload}, scroll
 * position, and section-expansion state (multi-section renderer only). The {@code
 * caseTypeVersionAtSave} component captures the case-type version the draft was last written
 * against; a mismatch with the case's current {@code caseTypeVersion} triggers the AC3 discard
 * prompt at the renderer.
 */
public record FormDraft(
    UUID id,
    UUID caseId,
    String formId,
    UUID userId,
    Map<String, Object> payload,
    int scrollY,
    Map<String, Boolean> sectionExpanded,
    int caseTypeVersionAtSave,
    Instant createdAt,
    Instant updatedAt) {}
