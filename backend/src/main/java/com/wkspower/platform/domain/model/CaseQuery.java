package com.wkspower.platform.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Query criteria for {@code CaseRepository.findByCaseType}. The case-type id is required (case
 * lists are always scoped to a single type per AC7); optional status narrows by exact match.
 */
public record CaseQuery(String caseTypeId, Optional<String> status) {

  public CaseQuery {
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    if (caseTypeId.isBlank()) {
      throw new IllegalArgumentException("caseTypeId must not be blank");
    }
    Objects.requireNonNull(status, "status");
  }

  public static CaseQuery of(String caseTypeId) {
    return new CaseQuery(caseTypeId, Optional.empty());
  }

  public static CaseQuery of(String caseTypeId, String status) {
    Optional<String> normalized =
        (status == null || status.isBlank()) ? Optional.empty() : Optional.of(status);
    return new CaseQuery(caseTypeId, normalized);
  }
}
