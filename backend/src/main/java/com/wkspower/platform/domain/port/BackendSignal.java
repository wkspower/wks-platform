package com.wkspower.platform.domain.port;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable signal emitted by a {@link BackendAdapter} for downstream routing (Story 4.3) into WKS
 * case-management semantics. Architecture §662 + §810 (Decision 22).
 *
 * <p>{@code payload} is a map of scalar values only — concrete shape per {@link BackendSignalKind}
 * is documented per-handler but not enforced at the type level (Story 4.3 router validates).
 *
 * @param kind one of the four declared kinds (precedence: see {@link BackendSignalKind} Javadoc)
 * @param adapterName adapter identity for FR8 source attribution (e.g. {@code "bpmn"}, {@code
 *     "state-machine"}, {@code "null"})
 * @param caseInstance the case instance the signal targets
 * @param source adapter-internal element id (BPMN element id, state-machine state name, etc.) for
 *     traceability
 * @param payload scalar-only payload; concrete shape per kind documented in handler Javadoc
 */
public record BackendSignal(
    BackendSignalKind kind,
    String adapterName,
    CaseInstanceRef caseInstance,
    String source,
    Map<String, Object> payload) {

  public BackendSignal {
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(adapterName, "adapterName");
    Objects.requireNonNull(caseInstance, "caseInstance");
    Objects.requireNonNull(source, "source");
    payload = payload == null ? Map.of() : Map.copyOf(payload);
  }

  /**
   * Static factory mirroring the canonical-form constructor with fail-fast null checks. Provided
   * because AC2 specifies a static-factory construction path; the canonical record constructor
   * already enforces the same null contract.
   */
  public static BackendSignal of(
      BackendSignalKind kind,
      String adapterName,
      CaseInstanceRef caseInstance,
      String source,
      Map<String, Object> payload) {
    return new BackendSignal(kind, adapterName, caseInstance, source, payload);
  }
}
