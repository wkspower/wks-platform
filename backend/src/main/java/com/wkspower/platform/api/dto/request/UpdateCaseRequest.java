package com.wkspower.platform.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request body for {@code PUT /api/cases/{id}} (Story 2.3 AC8). {@code version} is the optimistic
 * lock — mismatch surfaces as {@code WKS-RTM-409}.
 */
public record UpdateCaseRequest(@NotNull Map<String, Object> data, long version) {}
