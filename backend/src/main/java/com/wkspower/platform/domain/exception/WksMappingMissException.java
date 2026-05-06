package com.wkspower.platform.domain.exception;

import java.util.Objects;
import java.util.UUID;

/**
 * Story 4.3 AC4 — runtime miss in {@code BackendSignalRouter}: an incoming {@code BackendSignal}
 * did not resolve to any rule in the active {@code MappingDefinition} (or the CaseInstance's pinned
 * {@code (caseTypeId, version)} is not registered, or a {@code USER_TASK_PROPERTY} signal attempted
 * to drive a stage transition). Carries {@link ErrorCode#WKS_MAP_404}.
 *
 * <p>Distinct from deploy-time {@code WKS-MAP-001..006} (Story 4.2). The router catches this
 * exception at its boundary, writes an audit row, and does NOT propagate to the adapter — see AC4.
 *
 * <p>Carries the originating adapter name and the case instance id explicitly so the audit row's
 * {@code originAdapter} detail and the {@code caseInstance} reference can be filled without
 * re-deriving from the signal.
 */
public class WksMappingMissException extends WksException {

  private final String originAdapter;
  private final UUID caseInstanceId;

  public WksMappingMissException(String originAdapter, UUID caseInstanceId, String reason) {
    super(ErrorCode.WKS_MAP_404, reason);
    this.originAdapter = Objects.requireNonNull(originAdapter, "originAdapter");
    this.caseInstanceId = Objects.requireNonNull(caseInstanceId, "caseInstanceId");
  }

  public String originAdapter() {
    return originAdapter;
  }

  public UUID caseInstanceId() {
    return caseInstanceId;
  }
}
