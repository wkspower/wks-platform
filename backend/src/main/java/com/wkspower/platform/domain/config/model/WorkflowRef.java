package com.wkspower.platform.domain.config.model;

/**
 * Reference to the BPMN definition that drives transitions for this case type. Story 2.1 does NOT
 * validate BPMN file existence — that is Story 2.2. Here {@code bpmn} is just a required string
 * slot.
 */
public record WorkflowRef(String bpmn) {}
