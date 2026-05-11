package com.wkspower.platform.api.dto.response;

import com.wkspower.platform.domain.model.RecentSignalEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Story 4.6 AC2 — wire shape for {@code GET /api/admin/case-types/{caseTypeId}/recent-signals}.
 *
 * <p>Projects {@link RecentSignalEntry} ring-buffer snapshots into the per-row admin view. Order is
 * newest-first (matches buffer semantics). When the buffer has no entries for {@code caseTypeId},
 * {@code signals} is the empty list (HTTP 200 with {@code []}, NOT 404 — empty buffer is a valid
 * state).
 *
 * @param caseTypeId case type id (echoed from the path variable)
 * @param signals ordered list of recent routing decisions, newest-first, capped at 50
 */
public record RecentSignalsDto(String caseTypeId, List<RecentSignalView> signals) {

  public RecentSignalsDto {
    signals = signals == null ? List.of() : List.copyOf(signals);
  }

  /** One row in the recent-signals table. */
  public record RecentSignalView(
      Instant timestamp,
      String kind,
      String source,
      String decision,
      String matchedRule,
      String effect,
      UUID caseId,
      String errorCode) {}

  public static RecentSignalsDto from(String caseTypeId, List<RecentSignalEntry> entries) {
    return new RecentSignalsDto(
        caseTypeId, entries.stream().map(RecentSignalsDto::project).toList());
  }

  private static RecentSignalView project(RecentSignalEntry e) {
    return new RecentSignalView(
        e.timestamp(),
        e.kind().name(),
        e.source(),
        e.decision(),
        e.matchedRule(),
        e.effect(),
        e.caseId(),
        e.errorCode());
  }
}
