package com.wkspower.platform.domain.port;

import java.util.Optional;

/**
 * Outbound port that resolves the BPMN process-definition key associated with a deployed case-type.
 * The key is needed by {@code CaseService.create} to call {@code
 * WorkflowEngine.startProcessInstance(...)} — the YAML's {@code workflow.bpmn} field is the BPMN
 * file name, not the engine key.
 *
 * <p>Phase 0 implementation caches keys observed via the {@code ConfigDeployed} event stream; Phase
 * 1 may persist the mapping if the engine restart pattern requires it.
 */
public interface ProcessDefinitionKeyResolver {

  /** Returns the engine process-definition key for {@code caseTypeId}, if known. */
  Optional<String> resolve(String caseTypeId);
}
