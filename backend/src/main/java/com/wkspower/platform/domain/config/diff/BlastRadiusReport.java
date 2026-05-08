package com.wkspower.platform.domain.config.diff;

import com.wkspower.platform.domain.config.model.MappingChangeClass;
import java.util.List;

/**
 * Structured output of {@link CaseTypeDiff#classify}. Contains two segregated lists of change
 * units — append-class deltas (safe, no version bump required) and mutate-class deltas (require
 * {@code bumpVersion=true}).
 *
 * <p><b>Wire shape (stable — Admin UI Story 11.X depends on this):</b>
 *
 * <pre>
 * {
 *   "changeClass": "MUTATE_CLASS",
 *   "appendDeltas": [{"kind":"STATUS_ADDED","path":"/statuses/3/id","description":"..."}],
 *   "mutateDeltas": [{"kind":"STATUS_REMOVED","path":"/statuses/2/id","description":"status 'rejected' was removed"}]
 * }
 * </pre>
 *
 * <p>Jackson's default record serialisation is used — no custom serialiser needed. The {@code
 * changeClass} field is derived from the lists and serialised as a computed getter via the
 * {@link #changeClass()} accessor (Jackson includes it automatically for records when there is a
 * public method with a matching name and no-arg signature, but to be safe it is annotated with
 * {@link com.fasterxml.jackson.annotation.JsonProperty}).
 */
public record BlastRadiusReport(List<Delta> appendDeltas, List<Delta> mutateDeltas) {

  public BlastRadiusReport {
    appendDeltas = List.copyOf(appendDeltas);
    mutateDeltas = List.copyOf(mutateDeltas);
  }

  /**
   * Overall classification: {@link MappingChangeClass#MUTATE_CLASS} when {@code mutateDeltas} is
   * non-empty, otherwise {@link MappingChangeClass#APPEND_CLASS}. Mirrors {@code
   * MappingChangeClass} for symmetry with the mapping-layer classifier.
   */
  @com.fasterxml.jackson.annotation.JsonProperty("changeClass")
  public MappingChangeClass changeClass() {
    return mutateDeltas.isEmpty() ? MappingChangeClass.APPEND_CLASS : MappingChangeClass.MUTATE_CLASS;
  }

  /** Convenience: {@code true} when at least one mutate-class delta is present. */
  public boolean isMutateClass() {
    return !mutateDeltas.isEmpty();
  }
}
