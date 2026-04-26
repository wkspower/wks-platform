package com.wkspower.platform.domain.workflow;

import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Optional;

/**
 * Outcome of running BPMN bytes through the {@link
 * com.wkspower.platform.domain.port.BpmnValidationService}. Mirrors Story 2.1's {@code
 * ValidationResult} XOR invariant: exactly one of "errors empty + processDefinitionKey present" or
 * "errors non-empty + processDefinitionKey absent" holds.
 */
public record BpmnValidationResult(
    List<ErrorDetail> errors, Optional<String> processDefinitionKey) {

  public BpmnValidationResult {
    errors = List.copyOf(errors);
    if (errors.isEmpty() == processDefinitionKey.isEmpty()) {
      throw new IllegalStateException(
          "BpmnValidationResult invariant: exactly one of errors-empty and processDefinitionKey-"
              + "present must hold (errors="
              + errors.size()
              + ", processDefinitionKey="
              + (processDefinitionKey.isPresent() ? "present" : "absent")
              + ")");
    }
  }

  public static BpmnValidationResult ok(String processDefinitionKey) {
    if (processDefinitionKey == null || processDefinitionKey.isBlank()) {
      throw new IllegalStateException(
          "BpmnValidationResult.ok() requires a non-blank processDefinitionKey — a missing key is"
              + " a WKS-CFG-010 invalid result, not an ok one");
    }
    return new BpmnValidationResult(List.of(), Optional.of(processDefinitionKey));
  }

  public static BpmnValidationResult invalid(List<ErrorDetail> errors) {
    return new BpmnValidationResult(errors, Optional.empty());
  }

  public boolean isInvalid() {
    return !errors.isEmpty();
  }
}
