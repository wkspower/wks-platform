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

  /**
   * Story 3.9 review remediation — version-checked update of {@code cases.case_type_version} that
   * closes the TOCTOU window between {@code findById} and {@code save} on the rebase apply path.
   * Implemented as a JPQL {@code @Modifying} UPDATE; bumps the {@code @Version} column atomically
   * with the data write. Returns the number of rows affected (0 ⇒ another transaction bumped the
   * version between this caller's read and write).
   *
   * @param caseId the case row to update
   * @param toCaseTypeVersion the target CaseType version number
   * @param expectedVersion the optimistic-lock {@code @Version} value the caller observed at read
   *     time
   * @return {@code 1} on successful update, {@code 0} when no row matched (caller MUST treat this as
   *     a concurrent-modification signal and throw {@code WksConcurrentModificationException})
   */
  int updateCaseTypeVersion(UUID caseId, int toCaseTypeVersion, long expectedVersion);
}
