/**
 * Immutable domain records describing a case-type configuration: fields, statuses, roles, the
 * workflow reference, and the Mapping Layer (Story 4.2 / D22 — {@link
 * com.wkspower.platform.domain.config.model.MappingDefinition} + {@link
 * com.wkspower.platform.domain.config.model.AttachmentDefinition}). Pure Java — no Jackson, no
 * SnakeYAML, no Spring, no JPA. The infrastructure layer maps {@code RawCaseTypeConfig} (nullable
 * transport shape) into these records only after validation succeeds.
 *
 * <p>The single {@code domain/port/} import allowed in this package is {@link
 * com.wkspower.platform.domain.port.BackendSignalKind} (Story 4.1) — reused by {@link
 * com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule#emits()} so
 * the property-emission vocabulary stays unified across the Mapping Layer (validator output) and
 * the Backend Adapter port (router input). Architecture D22 cite.
 */
package com.wkspower.platform.domain.config.model;
