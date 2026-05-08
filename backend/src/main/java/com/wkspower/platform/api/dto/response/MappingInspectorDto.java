package com.wkspower.platform.api.dto.response;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import java.util.ArrayList;
import java.util.List;

/**
 * Story 4.6 AC1 — wire shape for {@code GET /api/admin/case-types/{caseTypeId}/mapping-inspector}.
 *
 * <p>Projects a validated {@link MappingDefinition} into a flat per-attachment / per-element view
 * suitable for the admin Mapping Inspector page. The {@code emptyMapping} flag is {@code true} iff
 * {@code attachments: []} (Story 4.7 zero-attachment first-class). The {@code UNMAPPED} row is
 * reserved for runtime-miss visualisation in the recent-signals panel — see {@link
 * RecentSignalsDto} — and is never emitted from the static mapping endpoint (the validator at
 * deploy time already cross-referenced BPMN against {@code routing:}).
 *
 * @param caseTypeId case type id (echoed from the path variable)
 * @param version active version of this case type
 * @param attachments per-attachment mapping projections; one section per attachment
 * @param emptyMapping {@code true} for zero-attachment case types
 */
public record MappingInspectorDto(
    String caseTypeId, String version, List<AttachmentView> attachments, boolean emptyMapping) {

  public MappingInspectorDto {
    attachments = attachments == null ? List.of() : List.copyOf(attachments);
  }

  /** One attachment block in the inspector view. */
  public record AttachmentView(String name, String bpmnSource, List<ElementMappingRow> elements) {
    public AttachmentView {
      elements = elements == null ? List.of() : List.copyOf(elements);
    }
  }

  /** One row in the per-attachment mapping table. */
  public record ElementMappingRow(
      String bpmnElement, String wksEffect, String target, String rule) {}

  /**
   * Project {@code mapping} into a wire DTO for {@code caseTypeId} at {@code version}. Iterates
   * {@code attachments[]} in declared order and emits one row per declared mapping element. The
   * {@code rule} pointer encodes the path within the attachment ({@code endEventMapping}, {@code
   * signalMappings.<id>}, {@code propertyEmissionRules[<idx>]}).
   */
  public static MappingInspectorDto from(
      String caseTypeId, String version, MappingDefinition mapping) {
    List<AttachmentView> views = new ArrayList<>();
    int attIdx = 0;
    for (AttachmentDefinition a : mapping.attachments()) {
      List<ElementMappingRow> rows = new ArrayList<>();
      // endEventMapping (single per attachment).
      a.endEventMapping()
          .ifPresent(
              eem ->
                  rows.add(
                      new ElementMappingRow(
                          "endEvent",
                          "stageTransition",
                          eem.stageTransition(),
                          "endEventMapping")));
      // signalMappings — sorted by signal id for stable wire order.
      a.signalMappings().entrySet().stream()
          .sorted(java.util.Map.Entry.comparingByKey())
          .forEach(
              e ->
                  rows.add(
                      new ElementMappingRow(
                          "signal:" + e.getKey(),
                          "stageTransition",
                          e.getValue().stageTransition(),
                          "signalMappings." + e.getKey())));
      // userTaskMappings — sorted by task id.
      a.userTaskMappings().entrySet().stream()
          .sorted(java.util.Map.Entry.comparingByKey())
          .forEach(
              e ->
                  rows.add(
                      new ElementMappingRow(
                          "userTask:" + e.getKey(),
                          "taskCompleted",
                          e.getValue().wksTask(),
                          "userTaskMappings." + e.getKey())));
      // propertyEmissionRules — preserve declared list order (semantic per Story 4.2).
      List<PropertyEmissionRule> rules = a.propertyEmissionRules();
      for (int i = 0; i < rules.size(); i++) {
        PropertyEmissionRule r = rules.get(i);
        String wksEffect =
            switch (r.emits()) {
              case TASK_COMPLETED -> "taskCompleted";
              case TASK_STATUS_CHANGED -> "taskStatusChanged";
              default -> r.emits().name();
            };
        rows.add(
            new ElementMappingRow(
                r.on(), wksEffect, r.camundaProperty(), "propertyEmissionRules[" + i + "]"));
      }
      String name = "attachment-" + attIdx++;
      views.add(new AttachmentView(name, a.file(), rows));
    }
    boolean empty = mapping.attachments().isEmpty();
    return new MappingInspectorDto(caseTypeId, version, views, empty);
  }
}
