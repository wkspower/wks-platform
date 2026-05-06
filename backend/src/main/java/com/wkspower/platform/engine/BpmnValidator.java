package com.wkspower.platform.engine;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.BpmnValidationService;
import com.wkspower.platform.domain.workflow.BpmnValidationResult;
import com.wkspower.platform.engine.properties.CamundaPropertyReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.cibseven.bpm.model.bpmn.BpmnModelInstance;
import org.cibseven.bpm.model.bpmn.instance.ConditionExpression;
import org.cibseven.bpm.model.bpmn.instance.FlowNode;
import org.cibseven.bpm.model.bpmn.instance.Process;
import org.cibseven.bpm.model.bpmn.instance.SequenceFlow;
import org.cibseven.bpm.model.bpmn.instance.ServiceTask;
import org.cibseven.bpm.model.bpmn.instance.UserTask;
import org.cibseven.bpm.model.bpmn.instance.cibseven.CamundaIn;
import org.cibseven.bpm.model.bpmn.instance.cibseven.CamundaOut;
import org.springframework.stereotype.Component;

/**
 * Story 2.2 BPMN validator. Collect-all (mirrors Story 2.1's {@code ConfigValidator}) — every check
 * accumulates {@link ErrorDetail}s rather than failing fast. Output errors carry the exact wire
 * string that the public contract documents.
 *
 * <p>Per architectural guardrail this class lives in {@code engine/} (NOT {@code
 * infrastructure/workflow/}) because it imports the CIB seven model API.
 */
@Component
public class BpmnValidator implements BpmnValidationService {

  /** Recognised archetype literals for Story 2.2 user tasks. */
  private static final String DRAFT_SECTION = "draft_section";

  private static final String SUBMIT_FOR_PROCESSING = "submit_for_processing";
  private static final String BUSINESS_FINAL = "business_final";

  private static final Set<String> VALID_ARCHETYPES =
      Set.of(DRAFT_SECTION, SUBMIT_FOR_PROCESSING, BUSINESS_FINAL);

  /**
   * Variable names CIB seven supplies on its own — not required to be declared in YAML. Order
   * matches the wire-shape documented in Dev Notes §Variable binding.
   */
  private static final Set<String> WELL_KNOWN_VARIABLES =
      Set.of("taskAssignee", "caseId", "caseTypeId", "caseStatus");

  private static final Pattern EXPRESSION_TOKEN = Pattern.compile("[\\$#]\\{([^}]+)\\}");

  /**
   * Identifier inside a {@code ${...}} expression — captures bare names ({@code amount}) and
   * leading qualifier ({@code amount.currency}). Only the head identifier is checked against the
   * field set.
   */
  private static final Pattern HEAD_IDENTIFIER = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)");

  private final BpmnParser parser;

  public BpmnValidator(BpmnParser parser) {
    this.parser = parser;
  }

  @Override
  public BpmnValidationResult validate(byte[] bpmnXml, CaseTypeConfig caseType) {
    BpmnModelInstance model;
    try {
      model = parser.parse(bpmnXml);
    } catch (BpmnParseException ex) {
      return BpmnValidationResult.invalid(
          List.of(ErrorDetail.of(ErrorCode.WKS_CFG_010.wire(), ex.getMessage())));
    }

    Collection<Process> processes = model.getModelElementsByType(Process.class);
    if (processes.isEmpty()) {
      return BpmnValidationResult.invalid(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_CFG_010.wire(), "BPMN contains no <bpmn:process> element")));
    }

    List<Process> executable =
        processes.stream()
            .filter(p -> Boolean.TRUE.equals(p.isExecutable()))
            .collect(Collectors.toList());

    List<ErrorDetail> errors = new ArrayList<>();

    if (executable.isEmpty()) {
      // No executable process — engine would deploy but no instance can ever start.
      errors.add(
          ErrorDetail.of(
              ErrorCode.WKS_CFG_010.wire(),
              "BPMN declares no executable <bpmn:process> (set isExecutable=\"true\" on the"
                  + " process WKS should drive)"));
      return BpmnValidationResult.invalid(errors);
    }
    if (executable.size() > 1) {
      String ids = executable.stream().map(Process::getId).collect(Collectors.joining(","));
      errors.add(
          ErrorDetail.ofField(
              ErrorCode.WKS_CFG_022.wire(),
              "BPMN declares more than one executable <bpmn:process> (ids: "
                  + ids
                  + "). Exactly one must be marked isExecutable=true; participant lanes in a"
                  + " collaboration must keep isExecutable=false.",
              "processes"));
      return BpmnValidationResult.invalid(errors);
    }
    Process process = executable.get(0);
    String processKey = process.getId();

    Collection<UserTask> userTasks = model.getModelElementsByType(UserTask.class);
    Collection<SequenceFlow> allFlows = model.getModelElementsByType(SequenceFlow.class);
    Set<String> declaredFieldIds =
        caseType == null
            ? Set.of()
            : caseType.fields().stream()
                .map(FieldDefinition::id)
                .collect(Collectors.toCollection(HashSet::new));

    for (UserTask task : userTasks) {
      String archetype = readArchetype(task);
      if (archetype == null) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_020.wire(),
                "User task '"
                    + safeId(task)
                    + "' is missing the required 'archetype' camunda:property "
                    + "(expected one of: draft_section, submit_for_processing, business_final)",
                "userTasks[" + safeId(task) + "]"));
        continue;
      }
      if (!VALID_ARCHETYPES.contains(archetype)) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_020.wire(),
                "User task '"
                    + safeId(task)
                    + "' declares unknown archetype '"
                    + archetype
                    + "' (allowed: draft_section, submit_for_processing, business_final)",
                "userTasks[" + safeId(task) + "]"));
        continue;
      }

      if (BUSINESS_FINAL.equals(archetype) && task.isCamundaAsyncAfter()) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_021.wire(),
                "User task '"
                    + safeId(task)
                    + "' archetype 'business_final' must not declare camunda:asyncAfter=true "
                    + "(rule: business_final tasks are terminal and synchronous)",
                "userTasks[" + safeId(task) + "].archetype"));
      }
      // Story 4.4a AC5 — when the CaseType declares stage-scoped statuses, every userTask MUST
      // carry an explicit <camunda:property name="status"> declaration. The legacy Phase-0
      // fallback (resolveNewStatus → first non-self active activity id) was non-deterministic
      // under parallel gateways; rejecting the BPMN at deploy-time is the replacement contract.
      // WKS-CFG-024 — wire-locked per feedback_error_codes_are_wire_contract.md.
      if (caseType != null
          && hasStageScopedStatuses(caseType)
          && readStatusProperty(task) == null) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_024.wire(),
                "User task '"
                    + safeId(task)
                    + "' is missing the required '<camunda:property name=\"status\"/>'"
                    + " declaration. CaseType '"
                    + caseType.id()
                    + "' declares stage-scoped statuses, so every userTask must declare its status"
                    + " explicitly (Story 4.4a AC5 — Phase-0 fallback retired)",
                "userTasks[" + safeId(task) + "].properties.status"));
      }
      if (DRAFT_SECTION.equals(archetype) && hasDownstreamTaskNode(task, allFlows)) {
        errors.add(
            ErrorDetail.ofField(
                ErrorCode.WKS_CFG_021.wire(),
                "User task '"
                    + safeId(task)
                    + "' archetype 'draft_section' must not have outgoing sequence flows targeting "
                    + "another task (rule: draft_section is terminal-only)",
                "userTasks[" + safeId(task) + "].archetype"));
      }
    }

    // Variable-binding check — collect all expression sites then walk tokens. Skipped when YAML
    // already failed (caseType null) — there's no field set to compare against, so every token
    // would surface as a missing variable, drowning the operator in noise.
    if (caseType == null) {
      // D4: surface a single aggregated 012 marker so collect-all reports something rather than
      // silently skipping the binding pass when YAML failed catastrophically. Per-token expansion
      // would drown the operator since every token would be unknown.
      errors.add(
          ErrorDetail.ofField(
              ErrorCode.WKS_CFG_012.wire(),
              "Variable-binding checks skipped — YAML invalid; fix YAML errors and redeploy to see"
                  + " per-expression results",
              "process." + processKey));
    } else {
      List<ExpressionSite> sites = collectExpressionSites(model);
      int missingIndex = 0;
      for (ExpressionSite site : sites) {
        Matcher m = EXPRESSION_TOKEN.matcher(site.expression());
        while (m.find()) {
          String inner = m.group(1).trim();
          Matcher head = HEAD_IDENTIFIER.matcher(inner);
          if (!head.find()) {
            continue;
          }
          String name = head.group(1);
          if (WELL_KNOWN_VARIABLES.contains(name) || declaredFieldIds.contains(name)) {
            continue;
          }
          errors.add(
              ErrorDetail.ofField(
                  ErrorCode.WKS_CFG_012.wire(),
                  "Variable '"
                      + name
                      + "' not found in case context for case type '"
                      + caseType.id()
                      + "'",
                  "process." + processKey + ".expression[" + missingIndex + "]"));
          missingIndex++;
        }
      }
    }

    if (!errors.isEmpty()) {
      return BpmnValidationResult.invalid(errors);
    }
    return BpmnValidationResult.ok(processKey);
  }

  private static String readArchetype(UserTask task) {
    return CamundaPropertyReader.read(task, "archetype");
  }

  /**
   * Story 4.4a AC5 — read the optional {@code <camunda:property name="status"/>} declaration on a
   * user task. Returns {@code null} when absent. Used by callers (deploy-time validators in
   * stage-scoped contexts) to enforce the explicit-status rule that replaced the legacy Phase-0
   * fallback {@code resolveNewStatus → first non-self active activity id}.
   */
  static String readStatusProperty(UserTask task) {
    return CamundaPropertyReader.read(task, "status");
  }

  /**
   * Story 4.4a AC5 — true when the CaseType declares one or more stage-scoped status sets (any
   * stage with a non-null, non-empty {@code statuses:} list). Stage-scoped contexts are the only
   * surface where the absence of an explicit userTask status must be a deploy-time failure; flat
   * statuses keep their existing Phase-0 acceptance until the broader migration in Story 4.4b.
   */
  private static boolean hasStageScopedStatuses(CaseTypeConfig caseType) {
    return caseType.stages().stream()
        .anyMatch(s -> s.statuses() != null && !s.statuses().isEmpty());
  }

  /**
   * Walks the flow graph downstream from {@code task}. Returns {@code true} if any reachable node
   * (after transparently traversing gateways) is a UserTask or ServiceTask. Gateways and call
   * activities pass through — earlier versions returned {@code false} when a gateway sat between
   * {@code draft_section} and another user task, leaking the WKS-CFG-021 invariant.
   */
  private static boolean hasDownstreamTaskNode(UserTask task, Collection<SequenceFlow> allFlows) {
    String taskId = task.getId();
    if (taskId == null) {
      return false;
    }
    java.util.Deque<String> queue = new java.util.ArrayDeque<>();
    Set<String> seen = new HashSet<>();
    queue.add(taskId);
    seen.add(taskId);
    while (!queue.isEmpty()) {
      String currentId = queue.poll();
      for (SequenceFlow flow : allFlows) {
        FlowNode source = flow.getSource();
        if (source == null || !currentId.equals(source.getId())) {
          continue;
        }
        FlowNode target = flow.getTarget();
        if (target == null || target.getId() == null) {
          continue;
        }
        // For the originating user task, any directly outgoing UserTask/ServiceTask trips the
        // invariant. For nodes reached via gateway-walk, only non-task nodes get re-queued — so
        // {draft_section -> end-event} stays clean while {draft_section -> gateway -> task}
        // surfaces the contradiction.
        if (target instanceof UserTask || target instanceof ServiceTask) {
          return true;
        }
        if (seen.add(target.getId())) {
          queue.add(target.getId());
        }
      }
    }
    return false;
  }

  private static String safeId(UserTask task) {
    String id = task.getId();
    return id == null || id.isEmpty() ? "<unnamed>" : id;
  }

  private static List<ExpressionSite> collectExpressionSites(BpmnModelInstance model) {
    List<ExpressionSite> sites = new ArrayList<>();
    for (SequenceFlow flow : model.getModelElementsByType(SequenceFlow.class)) {
      ConditionExpression expr = flow.getConditionExpression();
      if (expr == null) {
        continue;
      }
      String text = expr.getTextContent();
      if (text != null && !text.isBlank()) {
        sites.add(new ExpressionSite("conditionExpression", text));
      }
    }
    for (CamundaIn in : model.getModelElementsByType(CamundaIn.class)) {
      addIfPresent(sites, "camundaIn.source", in.getCamundaSource());
      addIfPresent(sites, "camundaIn.sourceExpression", in.getCamundaSourceExpression());
    }
    for (CamundaOut out : model.getModelElementsByType(CamundaOut.class)) {
      addIfPresent(sites, "camundaOut.source", out.getCamundaSource());
      addIfPresent(sites, "camundaOut.sourceExpression", out.getCamundaSourceExpression());
    }
    return sites;
  }

  private static void addIfPresent(List<ExpressionSite> sites, String origin, String expression) {
    if (expression != null && !expression.isBlank()) {
      sites.add(new ExpressionSite(origin, expression));
    }
  }

  private record ExpressionSite(String origin, String expression) {}
}
