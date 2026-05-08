package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Transport-shaped mirror of the case-type YAML tree. Every field is a boxed nullable so the
 * validator can observe missing required keys and report them as {@code WKS-CFG-001}. Unknown
 * properties are silently ignored for forward-compatibility — new slots added in future stories
 * must not break older YAML files.
 *
 * <p>This record lives in the infrastructure layer on purpose. Domain records ({@link
 * com.wkspower.platform.domain.config.model.CaseTypeConfig}) are built by a mapper only after
 * validation succeeds.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawCaseTypeConfig(
    String id,
    String displayName,
    Integer version,
    String description,
    @JsonProperty("workflows") RawWorkflow workflow,
    List<RawField> fields,
    List<RawStatus> statuses,
    List<String> listColumns,
    List<RawRole> roles,
    List<RawStage> stages,
    List<RawAttachment> attachments,
    @JsonProperty("forms") @JsonInclude(JsonInclude.Include.NON_NULL) RawFormConfig forms) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawWorkflow(String bpmn) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawField(
      String id,
      String displayName,
      String type,
      Boolean required,
      Boolean requiredOnCreate,
      Integer order,
      List<RawOption> options,
      Integer minLength,
      Integer maxLength,
      Double min,
      Double max,
      Double step,
      String dateMin,
      String dateMax,
      Long maxBytes,
      List<String> allowedMimeTypes) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawOption(String label, String value) {}

  /**
   * One {@code statuses[]} entry. Story 3.6 AC1 — adds {@code terminal} (boxed {@link Boolean};
   * {@code null} means "key omitted in YAML" and is treated as {@code false} downstream).
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawStatus(String id, String displayName, String color, Boolean terminal) {

    /**
     * Backward-compat constructor — pre-Story 3.6 callers that didn't know about the {@code
     * terminal} slot. Defaults {@code terminal} to {@code null} (loader treats absence as {@code
     * false}).
     */
    public RawStatus(String id, String displayName, String color) {
      this(id, displayName, color, null);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawRole(String name, List<String> permissions) {}

  /**
   * Story 3.1 — wrapper record that supports BOTH YAML forms in a single list:
   *
   * <pre>
   *   stages: [intake, underwriting]            # bare-string form
   *   stages:
   *     - id: intake
   *       displayName: "Intake"                  # rich-object form
   * </pre>
   *
   * <p>The string form lands via {@link #fromString} (single-arg DELEGATING creator); the object
   * form lands via {@link #fromObject} (PROPERTIES creator). Both produce the same record shape so
   * the validator can iterate uniformly.
   */
  public record RawStage(
      String id,
      String displayName,
      List<RawStatus> statuses,
      String initialStatus,
      /** Story 6.1 — optional archetype from the closed catalog. */
      String archetype) {

    /**
     * Backward-compat constructor — pre-Story 3.6 callers that didn't know about stage-scoped
     * statuses. Defaults both new slots to {@code null} ("not declared in YAML"; resolver falls
     * back to the flat case-type-level status set per Story 3.6 AC2).
     */
    public RawStage(String id, String displayName) {
      this(id, displayName, null, null, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RawStage fromString(String id) {
      return new RawStage(id, null, null, null, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public static RawStage fromObject(
        @JsonProperty("id") String id,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("statuses") List<RawStatus> statuses,
        @JsonProperty("initialStatus") String initialStatus,
        @JsonProperty("archetype") String archetype) {
      return new RawStage(id, displayName, statuses, initialStatus, archetype);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Story 4.2 — attachments + mapping (architecture §790–809)
  // ---------------------------------------------------------------------------------------------

  // Story 4.3.1 AC8 — mapping subtree records intentionally OMIT
  // @JsonIgnoreProperties(ignoreUnknown = true) so unknown YAML keys (typos like
  // events.signl: or emits: { typ: status }) surface as WKS-MAP-008 instead of being silently
  // dropped. The top-level RawCaseTypeConfig keeps the annotation for forward-compat with future
  // case-type-level keys; only the mapping block enforces strict schema.

  /** One {@code attachments[]} entry. */
  public record RawAttachment(String type, String file, String scope, RawRoutingBlock routing) {}

  /** {@code routing: { userTasks, events, properties }} block inside an attachment. */
  public record RawRoutingBlock(
      Map<String, RawUserTaskMapping> userTasks,
      RawEventMappings events,
      List<RawPropertyEmissionRule> properties) {}

  /** {@code routing.userTasks.<id>} entry. */
  public record RawUserTaskMapping(String wksTask, String form) {}

  /** {@code routing.events: { endEvent, signal }} block. */
  public record RawEventMappings(
      RawEndEventMapping endEvent, Map<String, RawSignalMapping> signal) {}

  /** {@code routing.events.endEvent} entry. */
  public record RawEndEventMapping(String stageTransition) {}

  /** {@code routing.events.signal.<id>} entry. */
  public record RawSignalMapping(String stageTransition) {}

  /**
   * {@code routing.properties[]} entry. The YAML key {@code camunda:property} is colon-bearing;
   * Jackson needs an explicit {@link JsonProperty} alias.
   */
  public record RawPropertyEmissionRule(
      @JsonProperty("on") String on,
      @JsonProperty("camunda:property") String camundaProperty,
      RawEmits emits) {}

  /** {@code emits: { type, scope }} block inside a property emission rule. */
  public record RawEmits(String type, String scope) {}

  // ---------------------------------------------------------------------------
  // Story 5.2 — Builder: prevents constructor-explosion when new slots are
  // added. The 12-arg canonical constructor (Jackson @JsonCreator target) is
  // kept; the Builder delegates to it. Test call-sites can use Builder instead
  // of the multi-arg constructors.
  // ---------------------------------------------------------------------------

  /** Returns a fresh {@link Builder} for programmatic construction (primarily in tests). */
  public static Builder builder() {
    return new Builder();
  }

  /** Fluent builder that delegates to the canonical 12-arg constructor on {@link #build()}. */
  public static final class Builder {
    private String id;
    private String displayName;
    private Integer version;
    private String description;
    private RawWorkflow workflow;
    private List<RawField> fields;
    private List<RawStatus> statuses;
    private List<String> listColumns;
    private List<RawRole> roles;
    private List<RawStage> stages;
    private List<RawAttachment> attachments;
    private RawFormConfig forms;

    private Builder() {}

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder displayName(String v) {
      this.displayName = v;
      return this;
    }

    public Builder version(Integer v) {
      this.version = v;
      return this;
    }

    public Builder description(String v) {
      this.description = v;
      return this;
    }

    public Builder workflow(RawWorkflow v) {
      this.workflow = v;
      return this;
    }

    public Builder fields(List<RawField> v) {
      this.fields = v;
      return this;
    }

    public Builder statuses(List<RawStatus> v) {
      this.statuses = v;
      return this;
    }

    public Builder listColumns(List<String> v) {
      this.listColumns = v;
      return this;
    }

    public Builder roles(List<RawRole> v) {
      this.roles = v;
      return this;
    }

    public Builder stages(List<RawStage> v) {
      this.stages = v;
      return this;
    }

    public Builder attachments(List<RawAttachment> v) {
      this.attachments = v;
      return this;
    }

    public Builder forms(RawFormConfig v) {
      this.forms = v;
      return this;
    }

    public RawCaseTypeConfig build() {
      return new RawCaseTypeConfig(
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
          attachments,
          forms);
    }
  }
}
