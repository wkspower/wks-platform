package com.wkspower.platform.api.dto.request;

import java.util.Map;

/**
 * Request body for {@code POST /api/tasks/{id}/complete} (Story 2.4 AC2). {@code variables} are
 * merged into the engine's process variables on completion and may be {@code null} or empty.
 */
public record CompleteTaskRequest(Map<String, Object> variables) {}
