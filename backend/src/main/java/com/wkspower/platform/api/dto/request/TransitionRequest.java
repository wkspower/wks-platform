package com.wkspower.platform.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request body for {@code POST /api/cases/{id}/transition} (Story 2.4 AC1). {@code action} is the
 * target status id; on the BPMN path it is the value forwarded as the {@code TASK_STATUS_CHANGED}
 * signal payload (see {@code CaseService.transition} BPMN routing decision). On the zero-process
 * path {@code action} must be a declared status id for the case type.
 *
 * <p>{@code variables} is accepted in the request body but is <b>not yet forwarded</b> on BPMN-path
 * calls — it is silently discarded at the signal-emit site. Propagation of {@code variables} and
 * {@code actorId} into the BPMN signal payload is tracked under TODO(4-5). Zero-process callers
 * similarly do not consume {@code variables}.
 */
public record TransitionRequest(@NotBlank String action, Map<String, Object> variables) {}
