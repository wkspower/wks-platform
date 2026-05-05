package com.wkspower.platform.engine;

import com.wkspower.platform.domain.port.BpmnElementInspector;
import com.wkspower.platform.domain.workflow.BpmnElementSummary;
import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;
import org.cibseven.bpm.model.bpmn.Bpmn;
import org.cibseven.bpm.model.bpmn.BpmnModelInstance;
import org.cibseven.bpm.model.bpmn.instance.EndEvent;
import org.cibseven.bpm.model.bpmn.instance.Signal;
import org.cibseven.bpm.model.bpmn.instance.UserTask;
import org.springframework.stereotype.Component;

/**
 * Parse-without-deploy wrapper over CIB seven's BPMN model API. Uses {@link
 * Bpmn#readModelFromStream(java.io.InputStream)} — model-API parsing does not touch the engine, so
 * this is safe to invoke from validation.
 *
 * <p>Story 4.2 added the {@link BpmnElementInspector} implementation — engine-free element id
 * enumeration consumed by {@code MappingValidator} for cross-reference checks (AC3). Returning a
 * domain-shaped {@link BpmnElementSummary} keeps the {@code org.cibseven..} import confined to
 * {@code engine/} (architecture invariant — see {@code ArchitectureTest.onlyEngineAdapter
 * ImportsTheBpmnEngineSdk}).
 */
@Component
public class BpmnParser implements BpmnElementInspector {

  /**
   * Parse BPMN bytes into a model instance. Throws {@link BpmnParseException} on any failure (empty
   * bytes, malformed XML, not a BPMN 2.0 document) — callers convert into a {@code WKS-CFG-010}
   * error.
   */
  public BpmnModelInstance parse(byte[] bpmnXml) {
    if (bpmnXml == null || bpmnXml.length == 0) {
      throw new BpmnParseException("BPMN bytes empty", null);
    }
    try {
      return Bpmn.readModelFromStream(new ByteArrayInputStream(bpmnXml));
    } catch (RuntimeException ex) {
      throw new BpmnParseException("BPMN parse failed: " + ex.getMessage(), ex);
    }
  }

  /**
   * Story 4.2 AC3 / AC12 — parse BPMN bytes and return a CIB-seven-free summary of the userTask /
   * endEvent / signal id sets the mapping validator cross-references. Returning {@link
   * BpmnElementSummary} (rather than {@link BpmnModelInstance}) keeps the {@code org.cibseven..}
   * import confined to {@code engine/}, satisfying {@code ArchitectureTest.onlyEngineAdapter
   * ImportsTheBpmnEngineSdk}. Throws {@link BpmnParseException} on parse failure — callers convert
   * to {@code WKS-MAP-005}.
   */
  @Override
  public BpmnElementSummary summarize(byte[] bpmnXml) {
    BpmnModelInstance model = parse(bpmnXml);
    Set<String> userTasks = new HashSet<>();
    for (UserTask t : model.getModelElementsByType(UserTask.class)) {
      if (t.getId() != null) {
        userTasks.add(t.getId());
      }
    }
    Set<String> endEvents = new HashSet<>();
    for (EndEvent e : model.getModelElementsByType(EndEvent.class)) {
      if (e.getId() != null) {
        endEvents.add(e.getId());
      }
    }
    Set<String> signals = new HashSet<>();
    for (Signal s : model.getModelElementsByType(Signal.class)) {
      if (s.getId() != null) {
        signals.add(s.getId());
      }
    }
    return new BpmnElementSummary(userTasks, endEvents, signals);
  }
}
