package com.wkspower.platform.domain.workflow;

/**
 * Engine-agnostic deploy request. Carries the BPMN bytes plus the case-type linkage the registry
 * needs to correlate the resulting deployment back to its config.
 */
public record DeploymentRequest(
    String name,
    String processDefinitionKey,
    byte[] bpmnXml,
    String caseTypeId,
    int caseTypeVersion) {}
