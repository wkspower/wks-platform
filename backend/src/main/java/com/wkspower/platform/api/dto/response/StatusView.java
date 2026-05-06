package com.wkspower.platform.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Wire shape for one entry in {@link CaseDto#availableStatuses()} (Story 3.6 AC8).
 *
 * <p>The list is the resolved status set for the case's {@code currentStageId} — stage-scoped when
 * the active stage declares its own {@code statuses:}, flat fallback otherwise (Decision 19's
 * unbranched-paths invariant: callers iterate without a presence check). The DTO is a render hint —
 * transitions still re-validate against the persisted {@code CaseTypeConfig} server-side
 * (anti-pattern #11 in the Story 3.6 dev notes).
 *
 * @param id status id (kebab-case, matches {@code [a-z][a-z0-9-]{1,62}})
 * @param displayName human-readable label, never blank
 * @param color status color enum string (lowercase wire form, e.g. {@code "blue"}); nullable when
 *     YAML omitted the slot
 * @param terminal stage-scoped terminal flag — when {@code true}, transitions to other statuses on
 *     the same stage are blocked (Story 3.6 AC1 / AC6)
 * @param ordinal 0-based position in the resolved status list (declaration order)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record StatusView(
    String id, String displayName, String color, boolean terminal, int ordinal) {}
