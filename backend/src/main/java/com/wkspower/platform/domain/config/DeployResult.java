package com.wkspower.platform.domain.config;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.util.List;
import java.util.Optional;

/**
 * Outcome of {@code ConfigService.deploy(byte[], byte[])}. Either a non-empty {@code errors}
 * aggregate (YAML, BPMN, or registry/engine failure) OR a successful {@code (caseType, deployment)}
 * pair — never both, never neither.
 */
public record DeployResult(
    List<ErrorDetail> errors,
    Optional<CaseTypeConfig> caseType,
    Optional<DeploymentResult> deployment) {

  public DeployResult {
    errors = List.copyOf(errors);
    boolean ok = caseType.isPresent() && deployment.isPresent();
    boolean failed = !errors.isEmpty();
    if (ok == failed) {
      throw new IllegalStateException(
          "DeployResult invariant: exactly one of (errors non-empty) and (caseType + deployment "
              + "present) must hold (errors="
              + errors.size()
              + ", caseType="
              + (caseType.isPresent() ? "present" : "absent")
              + ", deployment="
              + (deployment.isPresent() ? "present" : "absent")
              + ")");
    }
  }

  public static DeployResult ok(CaseTypeConfig caseType, DeploymentResult deployment) {
    return new DeployResult(List.of(), Optional.of(caseType), Optional.of(deployment));
  }

  public static DeployResult invalid(List<ErrorDetail> errors) {
    return new DeployResult(errors, Optional.empty(), Optional.empty());
  }

  public boolean isInvalid() {
    return !errors.isEmpty();
  }
}
