package com.wkspower.platform.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.UUID;

/**
 * Constructor-projection target for the case-list query. The JSON {@code data} column is omitted by
 * design (story 2.3 AC7 — never fetched on list path). Field-level projections from {@code
 * caseType.listColumns} are filled in by the adapter from a follow-up id-list lookup of the
 * lightweight rows; Phase 0 simplification: the listColumns values are looked up via the JSON path
 * once we add server-side JSON queries (Phase 1). For Phase 0 the {@code fields} map on the adapter
 * side is filled by reading the entity's full {@code data} map for visible rows only — but the list
 * endpoint avoids that via id-bounded fetch (or accepts the trade-off — see Story 2.5).
 *
 * <p>For 2.3 we expose a domain {@link com.wkspower.platform.domain.model.CaseSummary} with empty
 * field projections; Story 2.5 fills the field map via a post-query enrichment step.
 */
public record CaseSummaryProjection(
    UUID id,
    String caseTypeId,
    String status,
    UUID assignee,
    Instant createdAt,
    Instant updatedAt) {}
