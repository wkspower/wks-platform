package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.workflow.BpmnValidationResult;

/**
 * Outbound port for BPMN parse + structural validation. The single implementation lives in {@code
 * engine/} because the model API it leans on is part of the embedded BPMN engine SDK; the port
 * keeps domain-side {@code ConfigService} engine-agnostic.
 */
public interface BpmnValidationService {

  /**
   * Validate {@code bpmnXml} against {@code caseType}. When {@code caseType} is {@code null}
   * (because YAML validation already failed), structural checks (parse / archetype / contradiction)
   * still run; variable-binding checks are skipped because the field set is unknown.
   */
  BpmnValidationResult validate(byte[] bpmnXml, CaseTypeConfig caseType);
}
