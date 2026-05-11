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
    List<StageDefinition> stages,
    /**
     * Story 5.2 — form definitions declared in the YAML {@code forms[]} block. Empty list when
     * omitted (no-forms path is the zero-attachment equivalent for the forms surface).
     */
    List<FormDefinition> forms,
    /**
     * Story 5.6 AC4 — top-level setting controlling the default editability of fields that omit an
     * explicit {@code editableBy} declaration. Default {@link
     * DefaultFieldEditability#EDITABLE_BY_DEFAULT} preserves pre-5.6 behavior for every existing
     * seed YAML.
     */
    DefaultFieldEditability defaultFieldEditability,
    /**
     * Story 6.2 — internal validator-set marker that discriminates an author-declared top-level
     * {@code statuses:} block from {@link
     * com.wkspower.platform.infrastructure.config.ConfigValidator}'s injected {@code [open,
     * closed]} default. {@code true} when the YAML declared {@code statuses:} explicitly; {@code
     * false} when the validator injected the canonical default. Consumed by {@link
     * com.wkspower.platform.domain.service.CaseService#initialStatus(CaseTypeConfig)} so the
     * resolution order prefers explicitly-declared top-level statuses over per-stage initialStatus
     * while still preserving the gap-10 fix (stage-scoped types whose top-level statuses are
     * validator-injected defaults).
     *
     * <p>Not part of the external wire contract — never declared by users in YAML; never surfaced
     * in DTO mappers. Direct {@code new CaseTypeConfig(...)} callers default to {@code true} via
     * the compat constructors below, which preserves pre-6.2 semantics for tests that explicitly
     * construct a config with top-level statuses.
     */
    boolean explicitTopLevelStatuses) {

  public CaseTypeConfig {
    fields = List.copyOf(fields);
    statuses = List.copyOf(statuses);
    listColumns = List.copyOf(listColumns);
    roles = List.copyOf(roles);
    stages = stages == null ? List.of() : List.copyOf(stages);
    forms = forms == null ? List.of() : List.copyOf(forms);
    defaultFieldEditability =
        defaultFieldEditability == null
            ? DefaultFieldEditability.EDITABLE_BY_DEFAULT
            : defaultFieldEditability;
  }

  /**
   * Backwards-compatible constructor — pre-Story-6.2 callers that did not know about {@code
   * explicitTopLevelStatuses}. Defaults the slot to {@code true} (caller explicitly supplied
   * statuses).
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
      List<RoleDefinition> roles,
      List<StageDefinition> stages,
      List<FormDefinition> forms,
      DefaultFieldEditability defaultFieldEditability) {
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
        stages,
        forms,
        defaultFieldEditability,
        true);
  }

  /**
   * Backwards-compatible secondary constructor — pre-Story-5.6 callers that did not know about
   * {@code defaultFieldEditability}. Defaults the slot to {@link
   * DefaultFieldEditability#EDITABLE_BY_DEFAULT} (Phase-0 default — preserves current behavior).
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
      List<RoleDefinition> roles,
      List<StageDefinition> stages,
      List<FormDefinition> forms) {
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
        stages,
        forms,
        DefaultFieldEditability.EDITABLE_BY_DEFAULT);
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
        stages,
        forms,
        defaultFieldEditability,
        explicitTopLevelStatuses);
  }

  /** Returns a fresh {@link Builder} for programmatic construction (primarily in tests). */
  public static Builder builder() {
    return new Builder();
  }

  /** Fluent builder that delegates to the canonical 12-arg constructor on {@link #build()}. */
  public static final class Builder {
    private String id;
    private String displayName;
    private int version;
    private String description;
    private WorkflowRef workflow;
    private List<FieldDefinition> fields = List.of();
    private List<StatusDefinition> statuses = List.of();
    private List<String> listColumns = List.of();
    private List<RoleDefinition> roles = List.of();
    private List<StageDefinition> stages = List.of();
    private List<FormDefinition> forms = List.of();
    private DefaultFieldEditability defaultFieldEditability =
        DefaultFieldEditability.EDITABLE_BY_DEFAULT;
    private boolean explicitTopLevelStatuses = true;

    private Builder() {}

    public Builder id(String v) {
      this.id = v;
      return this;
    }

    public Builder displayName(String v) {
      this.displayName = v;
      return this;
    }

    public Builder version(int v) {
      this.version = v;
      return this;
    }

    public Builder description(String v) {
      this.description = v;
      return this;
    }

    public Builder workflow(WorkflowRef v) {
      this.workflow = v;
      return this;
    }

    public Builder fields(List<FieldDefinition> v) {
      this.fields = v;
      return this;
    }

    public Builder statuses(List<StatusDefinition> v) {
      this.statuses = v;
      return this;
    }

    public Builder listColumns(List<String> v) {
      this.listColumns = v;
      return this;
    }

    public Builder roles(List<RoleDefinition> v) {
      this.roles = v;
      return this;
    }

    public Builder stages(List<StageDefinition> v) {
      this.stages = v;
      return this;
    }

    public Builder forms(List<FormDefinition> v) {
      this.forms = v;
      return this;
    }

    public Builder defaultFieldEditability(DefaultFieldEditability v) {
      this.defaultFieldEditability = v == null ? DefaultFieldEditability.EDITABLE_BY_DEFAULT : v;
      return this;
    }

    /**
     * Story 6.2 — set the {@code explicitTopLevelStatuses} marker (Decision B). Defaults to {@code
     * true} on the builder; {@link com.wkspower.platform.infrastructure.config.ConfigValidator}
     * sets {@code false} only when it injects the canonical {@code [open, closed]} default for a
     * YAML that omitted {@code statuses:}.
     */
    public Builder explicitTopLevelStatuses(boolean v) {
      this.explicitTopLevelStatuses = v;
      return this;
    }

    public CaseTypeConfig build() {
      return new CaseTypeConfig(
          id,
          displayName,
          version,
          description,
          workflow,
          fields,
          statuses,
          listColumns,
          roles,
          stages,
          forms,
          defaultFieldEditability,
          explicitTopLevelStatuses);
    }
  }
}
