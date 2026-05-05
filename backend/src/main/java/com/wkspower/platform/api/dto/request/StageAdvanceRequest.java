package com.wkspower.platform.api.dto.request;

/**
 * Body for {@code POST /api/cases/{id}/advance-stage} (Story 3.1 AC10). The server fills {@code
 * source = "manual"} for HTTP-driven advances; the request only carries an optional correlation
 * string the caller wants stamped into the audit row (e.g. "approved by reviewer Alice").
 */
public record StageAdvanceRequest(String sourceRef) {}
