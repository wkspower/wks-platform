package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Outbound port for case persistence. Implementations live in {@code infrastructure/persistence/}
 * only — enforced by the ArchUnit rule {@code caseRepositoryImplementationsLiveInInfrastructure}
 * (Story 2.3 AC9).
 */
public interface CaseRepository {

  /**
   * Insert or update — the optimistic-lock {@code version} on {@link Case} drives the semantics.
   * Returns the persisted case with system fields populated ({@code updatedAt}, bumped {@code
   * version}).
   */
  Case save(Case caseToSave);

  /** O(1) lookup by id. */
  Optional<Case> findById(UUID id);

  /**
   * Paginated list of cases by case-type. Returns lightweight {@link CaseSummary} projections — the
   * JSON {@code data} column is never fetched on this path (perf NFR16).
   */
  Page<CaseSummary> findByCaseType(CaseQuery query, PageRequest pageRequest);

  /**
   * Bulk-fetch the {@code data} JSON for a set of case ids, returning each row's data filtered to
   * only the keys listed in {@code projectedFieldIds}. Used by the list path to enrich {@link
   * CaseSummary#fields} from {@code caseType.listColumns} without dragging the full data column
   * into the projection query (Story 2.3 AC7 / D4).
   */
  Map<UUID, Map<String, Object>> findDataByIds(Collection<UUID> ids, Set<String> projectedFieldIds);
}
