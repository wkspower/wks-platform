package com.wkspower.platform.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Wire shape for a single stage row exposed by {@link CaseDto#stages()} (Story 3.3 AC3 / AC4).
 *
 * <p>The {@code state} field carries the uppercase enum string of {@link
 * com.wkspower.platform.domain.model.StageState} (e.g. {@code "PENDING"}, {@code "ACTIVE"}, {@code
 * "COMPLETED"}, {@code "SKIPPED"}). The frontend maps to a lowercase CSS class name client-side.
 * Renaming any of these enum strings is a multi-PR cascade — they are a wire contract per memory
 * note {@code feedback_error_codes_are_wire_contract.md}.
 *
 * <p>Skipped stages appear in the list at their declared ordinal — the timeline never omits them.
 * The list is always ordered by {@code ordinal} ASC; for zero-stage CaseTypes the list is empty.
 *
 * <p>{@code source} is one of {@code "wks-auto-rule"}, {@code "manual"}, {@code "backend-signal"}
 * (Decision 1) — nullable on initial {@code PENDING} rows before the first transition. {@code
 * sourceRef} is a free-form correlation string and never includes PII.
 *
 * @param stageId YAML-declared stage id (e.g. {@code "intake"})
 * @param displayName human-readable label from the bound CaseType@version (Title-cased fallback per
 *     Story 3.1 AC1)
 * @param ordinal 0-based position in the declared stage list
 * @param state {@link com.wkspower.platform.domain.model.StageState} as wire string
 * @param enteredAt timestamp the row entered {@code ACTIVE}; {@code null} for {@code PENDING} and
 *     {@code SKIPPED} rows that never went active
 * @param exitedAt timestamp the row left {@code ACTIVE}; {@code null} while {@code PENDING} or
 *     {@code ACTIVE}
 * @param source attribution; {@code null} on initial {@code PENDING}
 * @param sourceRef free-form correlation; never PII
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record StageView(
    String stageId,
    String displayName,
    int ordinal,
    String state,
    Instant enteredAt,
    Instant exitedAt,
    String source,
    String sourceRef) {}
