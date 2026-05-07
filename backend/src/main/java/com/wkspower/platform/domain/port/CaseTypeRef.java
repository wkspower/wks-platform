package com.wkspower.platform.domain.port;

import java.util.Objects;

/**
 * Immutable value reference to a CaseType plus its pinned version. Used by {@link WorkflowAdapter}
 * to identify which CaseType (and version) an attachment, signal, or instance belongs to without
 * pulling the {@code CaseType} domain aggregate into the port surface.
 *
 * <p>Story 3.4 will own the authoritative {@code CaseTypeVersion} registry; this record only
 * carries the pair as a value object, in line with Decision 22 (BPMN Attachment &amp; Mapping
 * Layer).
 *
 * <p>Both fields are non-null. {@code version} matches {@code CaseTypeVersion} semantics from
 * Decision 20 — adapters MUST treat it as opaque (do not parse).
 *
 * @param caseTypeId the CaseType id (stable across versions, e.g. {@code "loan-application"})
 * @param version the pinned CaseType version string (opaque, treated as identity)
 */
public record CaseTypeRef(String caseTypeId, String version) {

  public CaseTypeRef {
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(version, "version");
    if (caseTypeId.isBlank()) {
      throw new IllegalArgumentException("caseTypeId must not be blank");
    }
    if (version.isBlank()) {
      throw new IllegalArgumentException("version must not be blank");
    }
  }
}
