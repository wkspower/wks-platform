package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.SignalMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.BackendSignalKind;
import com.wkspower.platform.domain.port.BpmnElementInspector;
import com.wkspower.platform.domain.workflow.BpmnElementSummary;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Story 4.2 — collect-all validator for the {@code attachments: [...]} block of a CaseType YAML.
 * Mirrors the {@link ConfigValidator} idiom: every check accumulates {@link ErrorDetail}s and the
 * validator never short-circuits on first error (architecture invariant — {@code BpmnValidator}
 * lines 124–129 reference).
 *
 * <p>This validator is I/O-free. Callers ({@link CaseTypeStartupLoader} / future admin deploy
 * controller) supply BPMN bytes via the {@code bpmnFiles} map; the validator never touches the
 * filesystem or deploys to the BPMN engine — Story 4.5 owns that lifecycle.
 *
 * <h2>Wire codes emitted</h2>
 *
 * <ul>
 *   <li>{@code WKS-MAP-001} — non-userTask BPMN element id missing in attached file
 *   <li>{@code WKS-MAP-003} — {@code scope: stage:<id>} (or {@code emits.scope}) references unknown
 *       stage
 *   <li>{@code WKS-MAP-004} — {@code attachments[].type} not in the {@link
 *       #SUPPORTED_ATTACHMENT_TYPES} allow-list
 *   <li>{@code WKS-MAP-005} — {@code attachments[].file} missing, blank, unreadable, or fails BPMN
 *       sniff
 *   <li>{@code WKS-MAP-006} — duplicate {@code scope} across attachments
 *   <li>{@code WKS-CFG-027} — userTask mapping reference missing in BPMN (architecture §828)
 *   <li>{@code WKS-CFG-028} — {@code stageTransition} adjacency invalid (architecture §832)
 * </ul>
 *
 * <p><b>Reserved-and-not-emitted</b>: {@code WKS-MAP-002} (precedence collisions — Phase-0
 * disallows two events targeting the same {@code (stage, status)}; the rule wires up in Story 4.4
 * once the runtime side has the precedence vocabulary), {@code WKS-MAP-007} (alias of {@code
 * WKS-CFG-028}), {@code WKS-CFG-029} (Story 3.8's blast-radius surface).
 *
 * <h2>Phase-0 type allow-list extension point</h2>
 *
 * <p>{@link #SUPPORTED_ATTACHMENT_TYPES} is the single enforcement point for Phase-0's BPMN-only
 * constraint (D22). Phase-1 stories extend this set to introduce additional adapter kinds — see
 * <b>Story 4.9</b> for the next allow-list addition (in-memory state-machine adapter).
 */
@Component
public class MappingValidator {

  /**
   * Phase-0 allow-list of {@code attachments[].type} values (AC6). Single enforcement point — see
   * Story 4.9 for the next allow-list addition (in-memory state-machine adapter).
   */
  private static final Set<String> SUPPORTED_ATTACHMENT_TYPES = Set.of("bpmn");

  /** Stage-scope syntax: {@code stage:<id>} where the id obeys the Story 3.1 stage id pattern. */
  private static final Pattern STAGE_SCOPE = Pattern.compile("stage:([a-z][a-z0-9-]{0,62})");

  /** {@code "<from> -> <to>"} adjacency syntax (the literal arrow is the separator). */
  private static final Pattern STAGE_TRANSITION =
      Pattern.compile("\\s*([a-z][a-z0-9-]*)\\s*->\\s*([a-z][a-z0-9-]*|completed|skipped)\\s*");

  /** {@code "userTask:<id>"} prefix on {@code map.properties[].on}. */
  private static final Pattern USER_TASK_REF = Pattern.compile("userTask:(.+)");

  private final BpmnElementInspector inspector;

  public MappingValidator(BpmnElementInspector inspector) {
    this.inspector = inspector;
  }

  /**
   * Validate the {@code attachments} block of {@code raw} and return a typed {@link
   * MappingDefinition} alongside the collected error list. {@code stageIds} is the set of stage ids
   * declared by the CaseType (Story 3.1) — used for {@code WKS-MAP-003} cross-references and {@code
   * WKS-CFG-028} adjacency checks. {@code bpmnFiles} maps filename → bytes; missing entries for
   * declared {@code attachments[].file} produce {@code WKS-MAP-005}.
   */
  public Result validate(
      RawCaseTypeConfig raw, Set<String> stageIds, Map<String, byte[]> bpmnFiles) {
    List<ErrorDetail> errors = new ArrayList<>();
    if (raw == null || raw.attachments() == null || raw.attachments().isEmpty()) {
      return new Result(MappingDefinition.empty(), List.copyOf(errors));
    }
    Map<String, byte[]> bpmnByName = bpmnFiles == null ? Map.of() : bpmnFiles;

    Set<String> seenScopes = new HashSet<>();
    List<AttachmentDefinition> definitions = new ArrayList<>();
    for (int i = 0; i < raw.attachments().size(); i++) {
      RawCaseTypeConfig.RawAttachment a = raw.attachments().get(i);
      String base = "/attachments/" + i;
      if (a == null) {
        errors.add(
            ErrorDetail.ofField(ErrorCode.WKS_CFG_001.wire(), "Attachment entry is empty", base));
        continue;
      }

      // --- type allow-list (WKS-MAP-004) ---
      String type = a.type();
      if (type == null || type.isBlank()) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_001.wire(),
                "Attachment requires 'type' (Phase-0 allowed: " + SUPPORTED_ATTACHMENT_TYPES + ")",
                base + "/type"));
      } else if (!SUPPORTED_ATTACHMENT_TYPES.contains(type)) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_MAP_004.wire(),
                "Unsupported attachment type '"
                    + type
                    + "' — Phase-0 allowed: "
                    + SUPPORTED_ATTACHMENT_TYPES,
                base + "/type"));
      }

      // --- scope syntax + stage cross-ref (WKS-MAP-003) + duplicate scope (WKS-MAP-006) ---
      String scope = a.scope();
      Optional<String> stageScopeId = Optional.empty();
      if (scope == null || scope.isBlank()) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_001.wire(),
                "Attachment requires 'scope' (case | stage:<id>)",
                base + "/scope"));
      } else if ("case".equals(scope)) {
        // legal
      } else {
        var m = STAGE_SCOPE.matcher(scope);
        if (!m.matches()) {
          errors.add(
              ErrorDetail.ofField(
                  ErrorCode.WKS_CFG_001.wire(),
                  "Attachment scope must be 'case' or 'stage:<id>' — got: " + scope,
                  base + "/scope"));
        } else {
          String stageId = m.group(1);
          stageScopeId = Optional.of(stageId);
          if (!stageIds.contains(stageId)) {
            errors.add(
                ErrorDetail.ofField(
                    ErrorCode.WKS_MAP_003.wire(),
                    "Attachment scope references unknown stage '" + stageId + "'",
                    base + "/scope"));
          }
        }
      }
      if (scope != null && !scope.isBlank() && !seenScopes.add(scope)) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_MAP_006.wire(),
                "Duplicate attachment scope: " + scope,
                base + "/scope"));
      }

      // --- file presence + BPMN sniff (WKS-MAP-005) ---
      String file = a.file();
      BpmnElementSummary summary = null;
      boolean bpmnTyped = "bpmn".equals(type);
      if (file == null || file.isBlank()) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_MAP_005.wire(), "Attachment requires 'file'", base + "/file"));
      } else if (bpmnTyped) {
        byte[] bytes = bpmnByName.get(file);
        if (bytes == null) {
          errors.add(
              ErrorDetail.ofField(
                  ErrorCode.WKS_MAP_005.wire(), "BPMN file not provided: " + file, base + "/file"));
        } else {
          try {
            summary = inspector.summarize(bytes);
          } catch (RuntimeException ex) {
            errors.add(
                ErrorDetail.ofField(
                    ErrorCode.WKS_MAP_005.wire(),
                    "BPMN parse failed for '" + file + "': " + ex.getMessage(),
                    base + "/file"));
          }
        }
      }

      // --- map.* cross-refs (WKS-CFG-027 / WKS-MAP-001 / WKS-CFG-028 / WKS-MAP-003) ---
      Map<String, UserTaskMapping> userTasksOut = new LinkedHashMap<>();
      Optional<EndEventMapping> endEventOut = Optional.empty();
      Map<String, SignalMapping> signalsOut = new LinkedHashMap<>();
      List<PropertyEmissionRule> propsOut = new ArrayList<>();

      RawCaseTypeConfig.RawAttachmentMap map = a.map();
      if (map != null) {
        // userTasks — emit WKS-CFG-027 when bpmn summary present and id not in BPMN
        if (map.userTasks() != null) {
          for (var entry : map.userTasks().entrySet()) {
            String taskId = entry.getKey();
            RawCaseTypeConfig.RawUserTaskMapping rut = entry.getValue();
            String fieldPath = base + "/map/userTasks/" + taskId;
            if (rut == null || rut.wksTask() == null || rut.wksTask().isBlank()) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_001.wire(),
                      "userTask mapping requires 'wksTask'",
                      fieldPath + "/wksTask"));
              continue;
            }
            if (summary != null && !summary.userTaskIds().contains(taskId)) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_027.wire(),
                      "BPMN userTask '"
                          + taskId
                          + "' referenced in mapping but not declared in "
                          + file,
                      fieldPath));
            }
            userTasksOut.put(taskId, new UserTaskMapping(rut.wksTask(), rut.form()));
          }
        }

        // events.endEvent + events.signal
        if (map.events() != null) {
          // endEvent
          RawCaseTypeConfig.RawEndEventMapping ree = map.events().endEvent();
          if (ree != null) {
            String fieldPath = base + "/map/events/endEvent/stageTransition";
            // AC3 rule 5: BPMN must declare at least one endEvent if any endEvent rule exists
            if (summary != null && summary.endEventIds().isEmpty()) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_MAP_001.wire(),
                      "endEvent rule declared but BPMN has no endEvents",
                      base + "/map/events/endEvent"));
            }
            String transition = ree.stageTransition();
            if (transition == null || transition.isBlank()) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_001.wire(),
                      "endEvent rule requires 'stageTransition'",
                      fieldPath));
            } else {
              if (!checkStageTransition(transition, stageIds, errors, fieldPath)) {
                // already reported
              }
              endEventOut = Optional.of(new EndEventMapping(transition));
            }
          }

          // signal mappings
          if (map.events().signal() != null) {
            for (var entry : map.events().signal().entrySet()) {
              String signalId = entry.getKey();
              RawCaseTypeConfig.RawSignalMapping rsm = entry.getValue();
              String sigField = base + "/map/events/signal/" + signalId;
              if (summary != null && !summary.signalIds().contains(signalId)) {
                errors.add(
                    ErrorDetail.ofField(
                        ErrorCode.WKS_MAP_001.wire(),
                        "BPMN signal '"
                            + signalId
                            + "' referenced in mapping but not declared in "
                            + file,
                        sigField));
              }
              if (rsm == null || rsm.stageTransition() == null || rsm.stageTransition().isBlank()) {
                errors.add(
                    ErrorDetail.ofField(
                        ErrorCode.WKS_CFG_001.wire(),
                        "signal mapping requires 'stageTransition'",
                        sigField + "/stageTransition"));
              } else {
                checkStageTransition(
                    rsm.stageTransition(), stageIds, errors, sigField + "/stageTransition");
                signalsOut.put(signalId, new SignalMapping(rsm.stageTransition()));
              }
            }
          }
        }

        // properties[]
        if (map.properties() != null) {
          for (int j = 0; j < map.properties().size(); j++) {
            RawCaseTypeConfig.RawPropertyEmissionRule rpr = map.properties().get(j);
            String pBase = base + "/map/properties/" + j;
            if (rpr == null) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_001.wire(), "property rule is empty", pBase));
              continue;
            }
            String on = rpr.on();
            if (on == null || on.isBlank()) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_001.wire(),
                      "property rule requires 'on' (e.g. userTask:<id>)",
                      pBase + "/on"));
            } else {
              var m = USER_TASK_REF.matcher(on);
              if (m.matches()) {
                String userTaskId = m.group(1);
                if (summary != null && !summary.userTaskIds().contains(userTaskId)) {
                  errors.add(
                      ErrorDetail.ofField(
                          ErrorCode.WKS_CFG_027.wire(),
                          "BPMN userTask '"
                              + userTaskId
                              + "' referenced in mapping but not declared in "
                              + file,
                          pBase + "/on"));
                }
              }
            }
            String prop = rpr.camundaProperty();
            if (prop == null || prop.isBlank()) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_001.wire(),
                      "property rule requires 'camunda:property'",
                      pBase + "/camunda:property"));
            }
            BackendSignalKind emitsKind = null;
            String emitScope = null;
            RawCaseTypeConfig.RawEmits remits = rpr.emits();
            if (remits == null) {
              errors.add(
                  ErrorDetail.ofField(
                      ErrorCode.WKS_CFG_001.wire(),
                      "property rule requires 'emits' block",
                      pBase + "/emits"));
            } else {
              emitsKind = parseEmitsKind(remits.type(), errors, pBase + "/emits/type");
              emitScope = remits.scope();
              if (emitScope != null && !emitScope.isBlank() && !"case".equals(emitScope)) {
                var sm = STAGE_SCOPE.matcher(emitScope);
                if (sm.matches() && !stageIds.contains(sm.group(1))) {
                  errors.add(
                      ErrorDetail.ofField(
                          ErrorCode.WKS_MAP_003.wire(),
                          "emits.scope references unknown stage '" + sm.group(1) + "'",
                          pBase + "/emits/scope"));
                }
              }
            }
            if (on != null
                && prop != null
                && emitsKind != null
                && emitScope != null
                && !on.isBlank()
                && !prop.isBlank()
                && !emitScope.isBlank()) {
              propsOut.add(new PropertyEmissionRule(on, prop, emitsKind, emitScope));
            }
          }
        }
      }

      // Build the AttachmentDefinition only when type/file/scope are non-blank — otherwise the
      // record's invariants would throw NPE. We still keep collecting errors for fields that did
      // pass.
      if (type != null
          && !type.isBlank()
          && file != null
          && !file.isBlank()
          && scope != null
          && !scope.isBlank()) {
        definitions.add(
            new AttachmentDefinition(
                type, file, scope, stageScopeId, userTasksOut, endEventOut, signalsOut, propsOut));
      }
    }

    return new Result(new MappingDefinition(definitions), List.copyOf(errors));
  }

  private boolean checkStageTransition(
      String transition, Set<String> stageIds, List<ErrorDetail> errors, String fieldPath) {
    var m = STAGE_TRANSITION.matcher(transition);
    if (!m.matches()) {
      errors.add(
          ErrorDetail.ofField(
              ErrorCode.WKS_CFG_001.wire(),
              "stageTransition must be '<from> -> <to>' — got: " + transition,
              fieldPath));
      return false;
    }
    String from = m.group(1);
    String to = m.group(2);
    boolean ok = true;
    if (!stageIds.contains(from)) {
      errors.add(
          ErrorDetail.ofField(
              ErrorCode.WKS_CFG_028.wire(),
              "stageTransition '" + transition + "' references unknown 'from' stage: " + from,
              fieldPath));
      ok = false;
    }
    if (!"completed".equals(to) && !"skipped".equals(to) && !stageIds.contains(to)) {
      errors.add(
          ErrorDetail.ofField(
              ErrorCode.WKS_CFG_028.wire(),
              "stageTransition '" + transition + "' references unknown 'to' stage: " + to,
              fieldPath));
      ok = false;
    }
    return ok;
  }

  private BackendSignalKind parseEmitsKind(
      String wire, List<ErrorDetail> errors, String fieldPath) {
    if (wire == null || wire.isBlank()) {
      errors.add(
          ErrorDetail.ofField(ErrorCode.WKS_CFG_001.wire(), "emits.type is required", fieldPath));
      return null;
    }
    return switch (wire) {
      case "status" -> BackendSignalKind.USER_TASK_PROPERTY;
      case "named-signal" -> BackendSignalKind.NAMED_SIGNAL;
      case "outcome" -> BackendSignalKind.OUTCOME;
      case "task-complete" -> BackendSignalKind.USER_TASK_PROPERTY;
      default -> {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_008.wire(),
                "Unknown emits.type '"
                    + wire
                    + "' — allowed: status|named-signal|outcome|task-complete",
                fieldPath));
        yield null;
      }
    };
  }

  /**
   * Result of {@link #validate(RawCaseTypeConfig, Set, Map)} — the parsed {@link MappingDefinition}
   * (always non-null; {@link MappingDefinition#empty()} when the YAML has no attachments) and the
   * collect-all error list.
   */
  public record Result(MappingDefinition definition, List<ErrorDetail> errors) {}
}
