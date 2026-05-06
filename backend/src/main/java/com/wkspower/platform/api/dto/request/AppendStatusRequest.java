package com.wkspower.platform.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/admin/case-types/{caseTypeId}/stages/{stageId}/statuses} (Story
 * 3.7 AC1). Append-class status add per Decision 21 — no version bump.
 *
 * @param id status id (kebab-case, matches {@code [a-z][a-z0-9-]{1,62}})
 * @param displayName human-readable label, never blank, ≤ 40 chars
 * @param color one of the ten {@code StatusColor} palette tokens (lowercase wire form)
 * @param terminal optional — defaults to {@code false} when omitted; Decision 21 / Story 3.6
 *     terminal flag (controller treats this as APPEND-only because it is being SET, not CHANGED;
 *     mutate-class flips of an existing status's flag remain rejected per Story 3.8)
 */
public record AppendStatusRequest(
    @NotBlank String id, @NotBlank String displayName, @NotBlank String color, Boolean terminal) {}
