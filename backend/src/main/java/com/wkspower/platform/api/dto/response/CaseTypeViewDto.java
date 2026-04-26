package com.wkspower.platform.api.dto.response;

import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import java.util.List;

/**
 * Subset of {@code CaseTypeConfig} embedded into {@link CaseDto} so the case-detail UI renders in
 * one round-trip (architecture.md §Decision 12 — config-driven rendering pipeline).
 *
 * <p>Roles and the workflow {@code bpmn} reference are intentionally NOT echoed — leaking the
 * role/permission matrix to the client would expose authorization metadata; the BPMN file path is
 * an internal concern.
 */
public record CaseTypeViewDto(
    String id,
    String displayName,
    int version,
    List<FieldDefinition> fields,
    List<StatusDefinition> statuses,
    List<String> listColumns) {}
