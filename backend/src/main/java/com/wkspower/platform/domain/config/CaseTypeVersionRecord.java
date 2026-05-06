package com.wkspower.platform.domain.config;

import java.time.Instant;

/**
 * Immutable snapshot of a {@code case_type_versions} row (Story 3.4 / Decision 20). Returned by
 * {@link com.wkspower.platform.domain.port.CaseTypeVersionRegistry#findVersion(String, int)} so the
 * adapter does not leak the JPA entity.
 *
 * <p>{@code rawYaml} is the author-supplied YAML bytes verbatim (Q3 LOCKED). Callers that need the
 * parsed {@code CaseTypeConfig} re-run the loader/validator pipeline; this record is the durable
 * representation.
 */
public record CaseTypeVersionRecord(
    String caseTypeId,
    int version,
    String hash,
    byte[] rawYaml,
    Instant publishedAt,
    String publishedBy) {}
