package com.wkspower.platform.engine.properties;

import java.util.Collection;
import org.cibseven.bpm.model.bpmn.instance.BaseElement;
import org.cibseven.bpm.model.bpmn.instance.ExtensionElements;
import org.cibseven.bpm.model.bpmn.instance.cibseven.CamundaProperties;
import org.cibseven.bpm.model.bpmn.instance.cibseven.CamundaProperty;

/**
 * Story 4.4a — single ingress for {@code <camunda:property>} reads inside BPMN-bound code paths.
 * Consolidates the three near-identical readers previously inlined in {@link
 * com.wkspower.platform.engine.BpmnValidator} (archetype), {@link
 * com.wkspower.platform.engine.CibSevenWorkflowEngine} (archetype + actionLabel), and {@link
 * com.wkspower.platform.engine.listeners.CaseStatusListener} (status). Avoids the "fourth copy"
 * antipattern flagged in {@code feedback_consolidate_property_readers.md}.
 *
 * <p>Lives in {@code engine/properties/} because the canonical CIB seven model API ({@link
 * CamundaProperties}, {@link CamundaProperty}) sits in {@code org.cibseven..}, which is restricted
 * to the {@code engine/} package by {@code
 * ArchitectureTest.onlyEngineAdapterImportsTheBpmnEngineSdk}. The Story 4.4a spec suggested {@code
 * infrastructure/engine/properties/} as a default; deviated to {@code engine/properties/} to honour
 * the standing ArchUnit boundary (engine SDK imports stay inside {@code engine/}). Documented in PR
 * body and Dev Agent Record.
 *
 * <p>Single read-by-name method by default — expand surface only when a callsite proves it's
 * needed.
 */
public final class CamundaPropertyReader {

  private CamundaPropertyReader() {}

  /**
   * Read the value of {@code <camunda:property name="..."/>} from any BPMN element that supports
   * extension elements. Returns {@code null} when the element is null, has no extension elements,
   * or no property with that name is declared.
   */
  public static String read(BaseElement element, String propertyName) {
    if (element == null || propertyName == null) {
      return null;
    }
    return read(element.getExtensionElements(), propertyName);
  }

  /** Read the value from a pre-resolved {@link ExtensionElements} block. */
  public static String read(ExtensionElements extensionElements, String propertyName) {
    if (extensionElements == null || propertyName == null) {
      return null;
    }
    Collection<CamundaProperties> blocks =
        extensionElements.getChildElementsByType(CamundaProperties.class);
    for (CamundaProperties block : blocks) {
      for (CamundaProperty p : block.getCamundaProperties()) {
        if (propertyName.equals(p.getCamundaName())) {
          return p.getCamundaValue();
        }
      }
    }
    return null;
  }
}
