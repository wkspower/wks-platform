package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    RawWorkflow workflow,
    List<RawField> fields,
    List<RawStatus> statuses,
    List<String> listColumns,
    List<RawRole> roles,
    List<RawStage> stages,
    List<RawAttachment> attachments) {

  /**
   * Backward-compat constructor for callers (and tests) authored before Story 4.2 introduced the
   * {@code attachments} slot. Treats absent attachments as the empty list — equivalent to a YAML
   * with no {@code attachments:} key (AC1).
   */
  public RawCaseTypeConfig(
      String id,
      String displayName,
      Integer version,
      String description,
      RawWorkflow workflow,
      List<RawField> fields,
      List<RawStatus> statuses,
      List<String> listColumns,
      List<RawRole> roles,
      List<RawStage> stages) {
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
        null);
  }

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
      String id, String displayName, List<RawStatus> statuses, String initialStatus) {

    /**
     * Backward-compat constructor — pre-Story 3.6 callers that didn't know about stage-scoped
     * statuses. Defaults both new slots to {@code null} ("not declared in YAML"; resolver falls
     * back to the flat case-type-level status set per Story 3.6 AC2).
     */
    public RawStage(String id, String displayName) {
      this(id, displayName, null, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RawStage fromString(String id) {
      return new RawStage(id, null, null, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public static RawStage fromObject(
        @JsonProperty("id") String id,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("statuses") List<RawStatus> statuses,
        @JsonProperty("initialStatus") String initialStatus) {
      return new RawStage(id, displayName, statuses, initialStatus);
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
  public record RawAttachment(String type, String file, String scope, RawAttachmentMap map) {}

  /** {@code map: { userTasks, events, properties }} block inside an attachment. */
  public record RawAttachmentMap(
      Map<String, RawUserTaskMapping> userTasks,
      RawEventMappings events,
      List<RawPropertyEmissionRule> properties) {}

  /** {@code map.userTasks.<id>} entry. */
  public record RawUserTaskMapping(String wksTask, String form) {}

  /** {@code map.events: { endEvent, signal }} block. */
  public record RawEventMappings(
      RawEndEventMapping endEvent, Map<String, RawSignalMapping> signal) {}

  /** {@code map.events.endEvent} entry. */
  public record RawEndEventMapping(String stageTransition) {}

  /** {@code map.events.signal.<id>} entry. */
  public record RawSignalMapping(String stageTransition) {}

  /**
   * {@code map.properties[]} entry. The YAML key {@code camunda:property} is colon-bearing; Jackson
   * needs an explicit {@link JsonProperty} alias.
   */
  public record RawPropertyEmissionRule(
      @JsonProperty("on") String on,
      @JsonProperty("camunda:property") String camundaProperty,
      RawEmits emits) {}

  /** {@code emits: { type, scope }} block inside a property emission rule. */
  public record RawEmits(String type, String scope) {}
}
