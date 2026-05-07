package com.wkspower.platform.domain.config.model;

import com.wkspower.platform.domain.port.ExecutionSignalKind;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Validated, immutable description of one {@code attachments[]} entry inside a CaseType YAML (Story
 * 4.2 AC1 / AC4). Pure Java — no Spring, no Jackson, no JPA.
 *
 * <p>The single {@code domain/port/} import allowed in this package is {@link ExecutionSignalKind}
 * (Story 4.1) — reused by {@link PropertyEmissionRule#emits()} so the property-emission vocabulary
 * stays unified across the Mapping Layer (validator output) and the Backend Adapter port (router
 * input).
 *
 * <p>{@link #type()} is a {@code String}, NOT an enum — so adding a Phase-1 attachment type (Story
 * 4.9 introduces {@code state-machine}) does not require an enum migration with downstream switch
 * statements. The validator (single allow-list constant) is the only enforcement point.
 *
 * @param type adapter kind. Phase-0: only {@code bpmn} (validated by {@code MappingValidator})
 * @param file relative filename of the attached resource (e.g. {@code claim-underwriting.bpmn})
 * @param scope raw scope string from YAML — {@code "case"} or {@code "stage:<stage-id>"}
 * @param stageScopeId parsed stage id when {@code scope} is {@code stage:<id>}; empty for
 *     case-scoped attachments
 * @param userTaskMappings BPMN userTask id → {@link UserTaskMapping}
 * @param endEventMapping optional {@code events.endEvent} rule (single per attachment)
 * @param signalMappings BPMN signal id → {@link SignalMapping}
 * @param propertyEmissionRules ordered {@code map.properties[]} list
 */
public record AttachmentDefinition(
    String type,
    String file,
    String scope,
    Optional<String> stageScopeId,
    Map<String, UserTaskMapping> userTaskMappings,
    Optional<EndEventMapping> endEventMapping,
    Map<String, SignalMapping> signalMappings,
    List<PropertyEmissionRule> propertyEmissionRules) {

  public AttachmentDefinition {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(scope, "scope");
    Objects.requireNonNull(stageScopeId, "stageScopeId");
    Objects.requireNonNull(userTaskMappings, "userTaskMappings");
    Objects.requireNonNull(endEventMapping, "endEventMapping");
    Objects.requireNonNull(signalMappings, "signalMappings");
    Objects.requireNonNull(propertyEmissionRules, "propertyEmissionRules");
    userTaskMappings = Map.copyOf(userTaskMappings);
    signalMappings = Map.copyOf(signalMappings);
    propertyEmissionRules = List.copyOf(propertyEmissionRules);
  }

  /** YAML {@code map.userTasks.<id>} entry. */
  public record UserTaskMapping(String wksTask, String form) {
    public UserTaskMapping {
      Objects.requireNonNull(wksTask, "wksTask");
    }
  }

  /** YAML {@code map.events.endEvent} entry — single per attachment by spec. */
  public record EndEventMapping(String stageTransition) {
    public EndEventMapping {
      Objects.requireNonNull(stageTransition, "stageTransition");
    }
  }

  /** YAML {@code map.events.signal.<id>} entry. */
  public record SignalMapping(String stageTransition) {
    public SignalMapping {
      Objects.requireNonNull(stageTransition, "stageTransition");
    }
  }

  /**
   * YAML {@code map.properties[]} entry. {@link #emits()} is a {@link ExecutionSignalKind} — the sole
   * {@code domain/port/} import allowed in this package.
   */
  public record PropertyEmissionRule(
      String on, String camundaProperty, ExecutionSignalKind emits, String emitScope) {
    public PropertyEmissionRule {
      Objects.requireNonNull(on, "on");
      Objects.requireNonNull(camundaProperty, "camundaProperty");
      Objects.requireNonNull(emits, "emits");
      Objects.requireNonNull(emitScope, "emitScope");
    }
  }
}
