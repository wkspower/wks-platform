package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.workflow.BpmnElementSummary;

/**
 * Outbound port for engine-free BPMN element introspection — reads userTask / endEvent / signal id
 * sets from BPMN bytes without exposing the embedded engine SDK. The single implementation lives in
 * {@code engine/} (Story 4.2 AC12 — reuse {@code BpmnParser}, no new BPMN model dep). Callers in
 * {@code infrastructure/config} (e.g. {@code MappingValidator}) depend on the port, not the parser.
 */
public interface BpmnElementInspector {

  /**
   * Parse {@code bpmnXml} and return the id sets of its userTask / endEvent / signal elements.
   * Throws an unchecked exception on parse failure — callers translate to {@code WKS-MAP-005}.
   */
  BpmnElementSummary summarize(byte[] bpmnXml);
}
