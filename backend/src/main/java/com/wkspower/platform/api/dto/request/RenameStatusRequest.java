package com.wkspower.platform.api.dto.request;

/**
 * Request body for {@code PATCH
 * /api/admin/case-types/{caseTypeId}/stages/{stageId}/statuses/{statusId}} (Story 3.7 AC1).
 * Append-class rename — only {@code displayName} and {@code color} may be changed. Setting {@code
 * terminal} on this path is treated as a mutate-class change and rejected with {@code WKS-STG-009 /
 * 405} (Story 3.8 owns the version-bump enforcement).
 *
 * <p>Both fields are optional but at least one MUST be present (the controller validates).
 */
public record RenameStatusRequest(String displayName, String color, Boolean terminal) {}
