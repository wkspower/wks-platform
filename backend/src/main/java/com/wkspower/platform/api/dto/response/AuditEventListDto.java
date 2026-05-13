package com.wkspower.platform.api.dto.response;

import java.util.List;

/**
 * Story 9-2 AC1 — wire shape for {@code GET /api/cases/{caseId}/audit-events}. Carries the
 * truncation flag computed by the controller via the {@code limit+1} probe described in the story
 * spec §Design Decision 2: the repository is asked for one more row than the caller requested; if
 * it returns more than {@code limit}, the extra row is dropped and {@code truncated} is set true.
 *
 * <p>Sprint 12 ships limit-only pagination (no cursor); {@code truncated=true} drives the UI banner
 * "Showing the {limit} most recent events". Cursor pagination is Sprint 13+ polish.
 */
public record AuditEventListDto(List<AuditEventViewDto> items, boolean truncated) {}
