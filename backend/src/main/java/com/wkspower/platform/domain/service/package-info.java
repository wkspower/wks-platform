/**
 * Domain services — business rules coordinated via ports.
 *
 * <p>Story 4.3 (Decision 22 — Mapping Layer): {@link
 * com.wkspower.platform.domain.service.ExecutionSignalRouter} is the single runtime routing surface
 * for every {@link com.wkspower.platform.domain.port.ExecutionSignal} emitted by any {@link
 * com.wkspower.platform.domain.port.WorkflowAdapter}. {@link
 * com.wkspower.platform.domain.service.MappingRegistry} indexes validated {@link
 * com.wkspower.platform.domain.config.model.MappingDefinition} values by {@code (caseTypeId,
 * version)} for D20 frozen-on-version routing.
 */
package com.wkspower.platform.domain.service;
