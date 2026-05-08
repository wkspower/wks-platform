package com.wkspower.platform.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import java.util.List;

/**
 * Subset of {@code CaseTypeConfig} embedded into {@link CaseDto} so the case-detail UI renders in
 * one round-trip (architecture.md §Decision 12 — config-driven rendering pipeline).
 *
 * <p>Roles and the workflow {@code bpmn} reference are intentionally NOT echoed — leaking the
 * role/permission matrix to the client would expose authorization metadata; the BPMN file path is
 * an internal concern.
 *
 * <p>Story 2.7 widens {@code fields[]} from {@code FieldDefinition} to {@link FieldView}: the
 * frontend's runtime Zod builder ({@code lib/buildZodFromFieldDefs.ts}) needs every per-type
 * validation slot ({@code minLength}, {@code maxLength}, {@code min}, {@code max}, {@code step},
 * {@code options[]}, {@code requiredOnCreate}, etc.). The wire field names match the YAML grammar
 * tokens exactly so the validation contract is one shape end-to-end.
 */
public record CaseTypeViewDto(
    String id,
    String displayName,
    int version,
    List<FieldView> fields,
    List<StatusDefinition> statuses,
    List<String> listColumns,
    List<StageDefinitionView> stages,
    /**
     * Story 5.2 — form definitions declared in the case-type YAML {@code forms[]} block. Empty list
     * when omitted. Frontend uses this to render {@code SinglePageFormRenderer} without a second
     * round-trip.
     */
    List<FormDefinitionView> forms) {

  /**
   * Compact constructor — defensive-copy {@code stages} and {@code forms} so the wire shape is
   * immutable end-to-end. Story 3.3 added {@code stages}; Story 5.2 adds {@code forms}.
   */
  public CaseTypeViewDto {
    stages = stages == null ? List.of() : List.copyOf(stages);
    forms = forms == null ? List.of() : List.copyOf(forms);
  }

  /**
   * Backward-compat constructor for callers (and tests) that predate Story 5.2's {@code forms} slot
   * — defaults forms to {@link List#of()}.
   */
  public CaseTypeViewDto(
      String id,
      String displayName,
      int version,
      List<FieldView> fields,
      List<StatusDefinition> statuses,
      List<String> listColumns,
      List<StageDefinitionView> stages) {
    this(id, displayName, version, fields, statuses, listColumns, stages, List.of());
  }

  /**
   * Wire-shape projection of {@code StageDefinition} for the case-type detail endpoint and the
   * embedded {@link CaseDto#caseType()} sub-object. Mirrors the YAML grammar 1:1 so the timeline
   * has the declared display name + ordinal without a second round-trip. Story 3.3.
   *
   * <p>Story 6.1 — adds {@code archetype} for UI specialization (nullable; omitted means default
   * affordance).
   */
  public record StageDefinitionView(String id, String displayName, int ordinal,
      @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
      String archetype) {

    /** Backward-compat constructor for callers that pre-date Story 6.1's archetype slot. */
    public StageDefinitionView(String id, String displayName, int ordinal) {
      this(id, displayName, ordinal, null);
    }
  }

  /**
   * Wire-shape projection of {@code FieldDefinition} for the case-type detail endpoint. Every
   * type-specific slot is nullable on the wire — only the slots relevant to {@link #type()} are
   * populated. The frontend renderer dispatches on {@code type} and reads only the matching slots.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record FieldView(
      String id,
      String displayName,
      String type,
      boolean required,
      boolean requiredOnCreate,
      int order,
      List<OptionView> options,
      Integer minLength,
      Integer maxLength,
      Number min,
      Number max,
      Number step,
      String dateMin,
      String dateMax,
      Long maxBytes,
      List<String> allowedMimeTypes) {}

  /** Wire-shape for a single {@code select}-field option. */
  public record OptionView(String label, String value) {}

  /**
   * Story 5.2 — wire-shape projection of {@link
   * com.wkspower.platform.domain.config.model.FormDefinition} for the case-type view endpoint.
   * Carries the three-axis vocabulary and the form's field list so the frontend renderer has
   * everything it needs in the {@code GET /api/case-types/{id}} response.
   *
   * <p>Story 5.3 — adds {@code sections[]} for {@code dataModel: sectioned} forms.
   */
  public record FormDefinitionView(
      String id,
      String topology,
      String dataModel,
      String rendering,
      List<FieldView> fields,
      List<FormSectionView> sections,
      /**
       * Story 6.1 — optional archetype from the closed catalog. {@code null} means omitted;
       * the frontend falls back to the default affordance. Excluded from JSON when null to avoid
       * surfacing implementation details to unaware clients.
       */
      @JsonInclude(JsonInclude.Include.NON_NULL) String archetype) {

    public FormDefinitionView {
      fields = fields == null ? List.of() : List.copyOf(fields);
      sections = sections == null ? List.of() : List.copyOf(sections);
    }

    /** Backward-compat constructor for callers that pre-date Story 6.1's archetype slot. */
    public FormDefinitionView(
        String id,
        String topology,
        String dataModel,
        String rendering,
        List<FieldView> fields,
        List<FormSectionView> sections) {
      this(id, topology, dataModel, rendering, fields, sections, null);
    }
  }

  /**
   * Story 5.3 — wire-shape for one section in a {@code dataModel: sectioned} form. Groups a set of
   * fields under a labelled expandable panel on the frontend.
   */
  public record FormSectionView(String id, String label, List<FieldView> fields) {

    public FormSectionView {
      fields = fields == null ? List.of() : List.copyOf(fields);
    }
  }
}
