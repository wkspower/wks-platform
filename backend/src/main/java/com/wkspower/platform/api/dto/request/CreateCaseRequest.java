package com.wkspower.platform.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

/**
 * Request body for {@code POST /api/cases} (Story 2.3 AC5). {@code data} carries the dynamic
 * case-type field map (null is treated as empty by {@code CaseService.create}); {@code assignee} is
 * optional (queue placement when {@code null}).
 */
public record CreateCaseRequest(
    @NotBlank String caseTypeId, Map<String, Object> data, UUID assignee) {}
