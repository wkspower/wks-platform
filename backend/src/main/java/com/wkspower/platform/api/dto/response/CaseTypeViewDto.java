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
    List<String> listColumns) {

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
}
