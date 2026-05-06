package com.wkspower.platform.domain.config.model;

import java.util.List;
import java.util.Optional;

/**
 * Validated, immutable case-type configuration. Every collection is defensively copied so the
 * registry cannot be mutated through a reference leaked to callers. Only {@code description} is
 * optional.
 *
 * <p>{@code stages} is the post-pivot foundational primitive (Story 3.1 / Decision 19). When the
 * YAML omits {@code stages:} or declares an empty list, this field is {@link List#of()} and every
 * downstream service treats the absence as a no-op (Decision 19: "stage-less paths must remain
 * unbranched").
 *
 * <p>Story 3.2 — {@code workflow} is now <strong>nullable</strong>. A CaseType may declare no
 * {@code workflow:} block in YAML; the component is then {@code null} and {@link #workflowOpt()}
 * returns {@link Optional#empty()}. Process-less paths must remain unbranched in code by the same
 * rule as stage-less paths (Decision 19 — Story 3.2 extension): {@link #workflowOpt()} is iterated
 * via {@code Optional.ifPresent(...)}, never via {@code if (caseType.workflow() != null)}.
 */
public record CaseTypeConfig(
    String id,
    String displayName,
    int version,
    String description,
    WorkflowRef workflow,
    List<FieldDefinition> fields,
    List<StatusDefinition> statuses,
    List<String> listColumns,
    List<RoleDefinition> roles,
    List<StageDefinition> stages) {

  /**
   * Backward-compat constructor for callers (and tests) that predate Story 3.1's {@code stages}
   * slot — defaults to {@link List#of()}.
   */
  public CaseTypeConfig(
      String id,
      String displayName,
      int version,
      String description,
      WorkflowRef workflow,
      List<FieldDefinition> fields,
      List<StatusDefinition> statuses,
      List<String> listColumns,
      List<RoleDefinition> roles) {
    this(
        id,
        displayName,
        version,
        description,
        workflow,
        fields,
        statuses,
        listColumns,
        roles,
        List.of());
  }

  public CaseTypeConfig {
    fields = List.copyOf(fields);
    statuses = List.copyOf(statuses);
    listColumns = List.copyOf(listColumns);
    roles = List.copyOf(roles);
    stages = stages == null ? List.of() : List.copyOf(stages);
  }

  /** Convenience lookup used by the JSON Schema generator and (future) case-data validation. */
  public Optional<FieldDefinition> field(String fieldId) {
    return fields.stream().filter(f -> f.id().equals(fieldId)).findFirst();
  }

  /**
   * Story 3.6 AC2 / AC8 — locate a declared stage by id.
   *
   * <p>Returns {@link Optional#empty()} when the case type has no stages or none match. Read-only
   * convenience; never throws.
   */
  public Optional<StageDefinition> stage(String stageId) {
    if (stageId == null) {
      return Optional.empty();
    }
    return stages.stream().filter(s -> s.id().equals(stageId)).findFirst();
  }

  /**
   * Story 3.6 AC2 / AC8 — resolve the effective status set for a given stage.
   *
   * <p>Returns the stage-scoped {@code statuses:} when declared, otherwise falls back to the flat
   * case-type-level {@link #statuses()} (which itself defaults to {@code [open, closed]} when YAML
   * omits the slot — Story 3.2 default). Single source of truth for Decision 19's "stage-less paths
   * must remain unbranched" — callers iterate the returned list without branching on stage
   * presence.
   */
  public List<StatusDefinition> statusesFor(String stageId) {
    return stage(stageId).flatMap(StageDefinition::statusesOpt).orElse(statuses);
  }

  /**
   * Story 3.2 — {@link Optional} view of the (now nullable) {@link #workflow()} component. Call
   * sites that need to skip engine work for process-less CaseTypes use {@code
   * workflowOpt().ifPresent(...)} per Decision 19's unbranched-paths invariant.
   */
  public Optional<WorkflowRef> workflowOpt() {
    return Optional.ofNullable(workflow);
  }

  /**
   * Story 3.4 / Decision 20 — return a copy of this config with the {@code version} component
   * replaced. Used by {@code ConfigService} after the version registry assigns the
   * registry-authoritative version, overriding any author-supplied {@code version:} key from the
   * YAML (Q1 LOCKED). All other components are copied unchanged.
   *
   * <p>If {@code newVersion} equals the current {@link #version()}, an equivalent copy is still
   * returned (records do not detect identity here — the canonical-constructor invariant defensively
   * copies collections regardless).
   */
  public CaseTypeConfig withVersion(int newVersion) {
    return new CaseTypeConfig(
        id,
        displayName,
        newVersion,
        description,
        workflow,
        fields,
        statuses,
        listColumns,
        roles,
        stages);
  }
}
