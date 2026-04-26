package com.wkspower.platform.api.dto.response;

/**
 * Wire shape returned on a successful {@code POST /api/admin/deploy} (Story 2.2). Carries the
 * registered case-type id + version, the engine deployment id + process definition id, and a
 * pointer to the generated JSON Schema for the case type.
 */
public record DeployResponseDto(
    String caseTypeId,
    int version,
    String deploymentId,
    String processDefinitionId,
    String schemaUri) {}
