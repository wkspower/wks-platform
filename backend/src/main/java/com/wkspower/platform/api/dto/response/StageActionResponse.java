package com.wkspower.platform.api.dto.response;

import java.util.UUID;

/**
 * Wire shape for the response of {@code POST /api/cases/{id}/advance-stage} and {@code POST
 * /api/cases/{id}/skip-stage} (Story 3.1 AC10). {@code currentStageId} and {@code
 * currentStageOrdinal} are {@code null} after last-stage completion.
 */
public record StageActionResponse(
    UUID caseId, String currentStageId, Integer currentStageOrdinal) {}
