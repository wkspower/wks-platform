package com.wkspower.platform.engine;

import java.io.ByteArrayInputStream;
import org.cibseven.bpm.model.bpmn.Bpmn;
import org.cibseven.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.stereotype.Component;

/**
 * Parse-without-deploy wrapper over CIB seven's BPMN model API. Uses {@link
 * Bpmn#readModelFromStream(java.io.InputStream)} — model-API parsing does not touch the engine, so
 * this is safe to invoke from validation.
 */
@Component
public class BpmnParser {

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
}
