package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.model.StatusDefinition;
import java.util.List;
import java.util.Optional;

/**
 * Read + write port for the {@code status_options} table (Story 3.7). Stores per-{@code
 * (caseTypeId, version, stageId)} status edits made via the admin REST API after deploy. Backs
 * Decision 21's "live append" promise — appended statuses persist durably and overlay on top of the
 * frozen-on-version YAML base at read time.
 *
 * <p>Hexagonal port: the domain-layer admin service depends on this interface, never the JPA
 * adapter directly. Tests can swap in an in-memory implementation.
 *
 * <p>{@code stageId == FLAT_SENTINEL} represents case-type-level (non-stage-scoped) statuses —
 * Story 3.6's flat fallback. Using a sentinel rather than nullable stageId keeps the composite key
 * non-nullable and lookups uniform.
 */
public interface StatusOptionsStore {

  /**
   * Sentinel stageId for case-type-level statuses (no stage scope). Story 3.7 Q3 — the persistence
   * shape uses a non-null sentinel to keep the composite primary key uniform; the in-memory
   * resolver from Story 3.6 distinguishes "stage declared but no statuses" from "no stage" via
   * {@code Optional<List<StatusDefinition>>}, which translates here as: the sentinel row only
   * exists when a flat status was edited via append, never auto-created on YAML deploy.
   */
  String FLAT_SENTINEL = "__flat__";

  /**
   * List the appended/edited statuses for a {@code (caseTypeId, version, stageId)}, ordered by
   * ordinal ASC. Empty list when no admin edits have landed for the key — callers fall back to the
   * frozen-on-version YAML status list.
   */
  List<StatusDefinition> listFor(String caseTypeId, int version, String stageId);

  /**
   * Look up a single status by id. Empty when no row exists for the key — includes "not yet
   * appended" and "never declared".
   */
  Optional<StatusDefinition> findOne(
      String caseTypeId, int version, String stageId, String statusId);

  /**
   * Append a new status to the stage's option set. Returns the persisted definition with its
   * assigned ordinal (max-existing + 1; 0 for the first row).
   *
   * @throws DuplicateStatusException when {@code statusId} already exists for the key
   */
  StatusDefinition append(
      String caseTypeId,
      int version,
      String stageId,
      String statusId,
      String displayName,
      String color,
      boolean terminal);

  /**
   * Persist a rename — updates {@code displayName} and/or {@code color} on an existing row. Returns
   * empty when no row exists for the key (caller maps to {@code WKS-STG-013}).
   */
  Optional<StatusDefinition> rename(
      String caseTypeId,
      int version,
      String stageId,
      String statusId,
      String displayName,
      String color);

  /**
   * Thrown by {@link #append} when the {@code (caseTypeId, version, stageId, statusId)} key is
   * already taken. Caller maps to {@code WKS-STG-007}.
   */
  class DuplicateStatusException extends RuntimeException {
    public DuplicateStatusException(String message) {
      super(message);
    }
  }
}
