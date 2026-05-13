package com.wkspower.platform.api.mapper;

import com.wkspower.platform.api.dto.response.AuditEventViewDto;
import com.wkspower.platform.api.dto.response.AuditEventViewDto.AuditSourceView;
import com.wkspower.platform.audit.AuditEvent;
import com.wkspower.platform.audit.AuditEventMapper;

/**
 * Story 9-2 — translates a domain {@link AuditEvent} into its wire DTO. Reuses {@link
 * AuditEventMapper#sourceType(com.wkspower.platform.domain.model.AuditSource)} and {@link
 * AuditEventMapper#sourcePayload(com.wkspower.platform.domain.model.AuditSource)} so the wire
 * shape's {@code source.type} / {@code source.payload} mirror the {@code audit_events.source_type}
 * / {@code audit_events.source_payload} columns 1:1 — the round-trip contract pinned by 9-3.
 *
 * <p>This is intentionally not collapsed into {@link AuditEventMapper}: that class lives in the
 * {@code audit} package and serves persistence; this mapper lives in {@code api.mapper} alongside
 * the other wire mappers ({@code CaseDtoMapper}, {@code TaskDtoMapper}).
 */
public final class AuditEventViewMapper {

  private AuditEventViewMapper() {
    // utility
  }

  public static AuditEventViewDto toDto(AuditEvent event) {
    return new AuditEventViewDto(
        event.id(),
        event.eventType(),
        new AuditSourceView(
            AuditEventMapper.sourceType(event.source()),
            AuditEventMapper.sourcePayload(event.source())),
        event.result(),
        event.fieldId(),
        event.openTaskId(),
        event.formId(),
        event.occurredAt());
  }
}
