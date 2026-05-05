package com.wkspower.platform.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for {@code POST /api/cases/{id}/skip-stage} (Story 3.1 AC10). The server fills {@code source
 * = "manual"}; the request supplies the target stage id and an optional correlation ref.
 */
public record StageSkipRequest(@NotBlank String targetStageId, String sourceRef) {}
