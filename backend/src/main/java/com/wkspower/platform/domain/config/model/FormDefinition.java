package com.wkspower.platform.domain.config.model;

import java.util.List;

/**
 * Validated, immutable form definition. One entry per {@code forms[]} item in the CaseType YAML.
 * The three-axis vocabulary captures topology, dataModel, and rendering.
 *
 * <p>Story 5.2 — surfaced as part of the {@link CaseTypeConfig} domain model; exposed through the
 * {@code GET /api/case-types/{id}} response via {@link
 * com.wkspower.platform.api.dto.response.CaseTypeViewDto}.
 */
public record FormDefinition(
    /** Stable identifier matching the YAML {@code id} key. */
    String id,
    /**
     * Structural shape of the form session. Phase-0 valid value: {@code single}. Phase-1 will add
     * {@code parallel}; emitting WKS-FORM-001 on any non-{@code single} value at deploy time.
     */
    String topology,
    /**
     * How form data is partitioned. Phase-0 valid values: {@code monolithic}, {@code sectioned}.
     */
    String dataModel,
    /**
     * How the UI renders the form. Phase-0 valid values: {@code single-page}, {@code
     * multi-section}.
     */
    String rendering,
    /**
     * Fields rendered by this form. May be a subset of the case-type's overall fields when the form
     * targets specific field ids.
     */
    List<FieldDefinition> fields,
    /**
     * Story 5.3 — sections declared for {@code dataModel: sectioned} forms. Empty for monolithic
     * forms.
     */
    List<FormSection> sections,
    /**
     * Story 6.1 — optional archetype from the closed catalog ({@code draft_section}, {@code
     * submit_for_processing}, {@code business_final}). {@code null} means omitted — the frontend
     * falls back to default affordance behavior.
     */
    String archetype) {

  public FormDefinition {
    fields = fields == null ? List.of() : List.copyOf(fields);
    sections = sections == null ? List.of() : List.copyOf(sections);
  }

}
