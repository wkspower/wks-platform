package com.wkspower.platform.api.dto.response;

import java.util.List;

/**
 * Response DTO for {@code GET /api/admin/case-types/{caseTypeId}/stages/{stageId}/statuses} (Story
 * 3.7 AC1). Wire shape: {@code { stageId, initialStatus, statuses: [...] }}.
 *
 * <p>Reuses {@link StatusView} from Story 3.6 — same wire shape on the runtime case-detail path and
 * the admin read path keeps the frontend palette logic unified.
 */
public record StageStatusSetDto(String stageId, String initialStatus, List<StatusView> statuses) {}
