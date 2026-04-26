package com.wkspower.platform.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request body for {@code POST /api/cases/{id}/transition} (Story 2.4 AC1). {@code action} is the
 * BPMN message name (Phase 0 supports message correlation only — see Story 2.4 Dev Notes
 * §Transition dispatch); {@code variables} are merged into the engine's process variables and may
 * be {@code null} or empty.
 */
public record TransitionRequest(@NotBlank String action, Map<String, Object> variables) {}
