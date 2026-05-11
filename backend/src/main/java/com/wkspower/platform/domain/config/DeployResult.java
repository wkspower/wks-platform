package com.wkspower.platform.domain.config;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Outcome of {@code ConfigService.deploy(byte[], byte[])}. Either a non-empty {@code errors}
 * aggregate (YAML, BPMN, or registry/engine failure) OR a successful {@code (caseType, deployment)}
 * pair — never both, never neither.
 *
 * <p>Story 3.8 — adds {@link #responseMeta()} to carry blast-radius report metadata on WKS-CFG-029
 * rejections (AC2: {@code meta.blastRadius} must be present in the error response envelope).
 *
 * <p>Story 3.9 CF#1 — adds {@link #gateOutcome()} and {@link #priorVersionNum()} to carry the
 * blast-radius gate result back to the controller layer for audit-log threading. Both default to
 * {@code NO_OVERRIDE_USED} / {@code 0} on all backward-compat construction paths.
 */
public record DeployResult(
    List<ErrorDetail> errors,
    Optional<CaseTypeConfig> caseType,
    Optional<DeploymentResult> deployment,
    Map<String, Object> responseMeta,
    GateOutcome gateOutcome,
    int priorVersionNum) {

  public DeployResult {
    errors = List.copyOf(errors);
    responseMeta = responseMeta == null ? Map.of() : Map.copyOf(responseMeta);
    gateOutcome = gateOutcome == null ? GateOutcome.NO_OVERRIDE_USED : gateOutcome;
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

  /** Backward-compat 4-arg constructor — defaults CF#1 fields. */
  public DeployResult(
      List<ErrorDetail> errors,
      Optional<CaseTypeConfig> caseType,
      Optional<DeploymentResult> deployment,
      Map<String, Object> responseMeta) {
    this(errors, caseType, deployment, responseMeta, GateOutcome.NO_OVERRIDE_USED, 0);
  }

  /** Backward-compat 3-arg constructor — defaults {@link #responseMeta()} and CF#1 fields. */
  public DeployResult(
      List<ErrorDetail> errors,
      Optional<CaseTypeConfig> caseType,
      Optional<DeploymentResult> deployment) {
    this(errors, caseType, deployment, null, GateOutcome.NO_OVERRIDE_USED, 0);
  }

  public static DeployResult ok(CaseTypeConfig caseType, DeploymentResult deployment) {
    return new DeployResult(List.of(), Optional.of(caseType), Optional.of(deployment));
  }

  /** Story 3.9 CF#1 — success result carrying the gate outcome for audit-log threading. */
  public static DeployResult ok(
      CaseTypeConfig caseType,
      DeploymentResult deployment,
      GateOutcome gateOutcome,
      int priorVersionNum) {
    return new DeployResult(
        List.of(),
        Optional.of(caseType),
        Optional.of(deployment),
        null,
        gateOutcome,
        priorVersionNum);
  }

  public static DeployResult invalid(List<ErrorDetail> errors) {
    return new DeployResult(errors, Optional.empty(), Optional.empty());
  }

  /** Story 3.8 — invalid result with response-level metadata (e.g. {@code blastRadius} report). */
  public static DeployResult invalidWithMeta(List<ErrorDetail> errors, Map<String, Object> meta) {
    return new DeployResult(errors, Optional.empty(), Optional.empty(), meta);
  }

  public boolean isInvalid() {
    return !errors.isEmpty();
  }
}
