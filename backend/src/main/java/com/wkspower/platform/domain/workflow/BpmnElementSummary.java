package com.wkspower.platform.domain.workflow;

import java.util.Objects;
import java.util.Set;

/**
 * Engine-free summary of a parsed BPMN file's element id sets. Returned by {@code
 * BpmnElementInspector.summarize(byte[])}; consumed by {@code MappingValidator} (Story 4.2 AC3) to
 * cross-reference YAML mapping ids against the BPMN. The record carries plain Java collections so
 * the {@code org.cibseven..} import stays inside {@code engine/}.
 */
public record BpmnElementSummary(
    Set<String> userTaskIds, Set<String> endEventIds, Set<String> signalIds) {

  public BpmnElementSummary {
    Objects.requireNonNull(userTaskIds, "userTaskIds");
    Objects.requireNonNull(endEventIds, "endEventIds");
    Objects.requireNonNull(signalIds, "signalIds");
    userTaskIds = Set.copyOf(userTaskIds);
    endEventIds = Set.copyOf(endEventIds);
    signalIds = Set.copyOf(signalIds);
  }
}
