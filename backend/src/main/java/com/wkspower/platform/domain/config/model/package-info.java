/**
 * Immutable domain records describing a case-type configuration: fields, statuses, roles, the
 * workflow reference. Pure Java — no Jackson, no SnakeYAML, no Spring. The infrastructure layer
 * maps {@code RawCaseTypeConfig} (nullable transport shape) into these records only after
 * validation succeeds.
 */
package com.wkspower.platform.domain.config.model;
