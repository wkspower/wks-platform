package com.wkspower.platform.domain.port;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable value reference to a Case instance plus its CaseType binding. Used by {@link
 * BackendAdapter#start} and {@link BackendAdapter#cancel} so the port never pulls in the {@code
 * Case} domain aggregate.
 *
 * <p>The {@code id} is the WKS-side case instance UUID — adapters MUST treat any backend-side
 * instance id (e.g. CIB process instance id) as opaque adapter state and only return it as the
 * result of {@link BackendAdapter#start}.
 *
 * @param id the WKS case instance UUID
 * @param caseType the CaseType + version this instance is bound to (pinned for the life of the
 *     instance; see Story 4.5)
 */
public record CaseInstanceRef(UUID id, CaseTypeRef caseType) {

  public CaseInstanceRef {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(caseType, "caseType");
  }
}
