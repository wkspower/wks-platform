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
 *
 * <p>Story 4.5 AC3 — {@code bpmnContentHash} and {@code mappingHash} are SHA-256 hex fingerprints
 * added for forensic / integrity purposes (Decision 22). Both are {@code null} for zero-attachment
 * deploys (D22: zero-attachment is first-class; {@code NULL} is stored in the column).
 */
public record CaseTypeVersionRecord(
    String caseTypeId,
    int version,
    String hash,
    byte[] rawYaml,
    Instant publishedAt,
    String publishedBy,
    String bpmnContentHash,
    String mappingHash) {}
