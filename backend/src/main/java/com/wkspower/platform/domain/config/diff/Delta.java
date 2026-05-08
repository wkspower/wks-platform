package com.wkspower.platform.domain.config.diff;

/**
 * Single change unit inside a {@link BlastRadiusReport}. Immutable record — Jackson serialises it
 * by default using record component names.
 *
 * <p><b>Wire shape:</b>
 *
 * <pre>
 * { "kind": "STATUS_REMOVED", "path": "/statuses/2/id", "description": "status 'rejected' was removed" }
 * </pre>
 *
 * <p>{@code path} follows JSON-pointer-style notation already used by {@code ConfigValidator} (e.g.
 * {@code /statuses/2/id}, {@code /fields/4/required}, {@code /stages/1/statuses/0/id}).
 */
public record Delta(DeltaKind kind, String path, String description) {}
