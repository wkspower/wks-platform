package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.page.SortOrder;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.infrastructure.persistence.entity.CaseEntity;
import com.wkspower.platform.infrastructure.persistence.mapper.CaseMapper;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseSummaryProjection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter for {@link CaseRepository}. Maps domain ↔ JPA via {@link CaseMapper}. The list path uses
 * a constructor projection so the JSON {@code data} column is never fetched on list reads (Story
 * 2.3 AC7).
 *
 * <p>Sort whitelisting is handled at the api layer; this adapter trusts the {@link SortOrder}
 * properties and translates them straight into Spring Data's {@link Sort} (camelCase JPA property
 * names match the entity field names).
 */
@Component
class CaseRepositoryAdapter implements CaseRepository {

  private final CaseEntityRepository cases;

  CaseRepositoryAdapter(CaseEntityRepository cases) {
    this.cases = cases;
  }

  @Override
  @Transactional
  public Case save(Case caseToSave) {
    CaseEntity entity =
        cases
            .findById(caseToSave.id())
            .map(existing -> applyUpdate(existing, caseToSave))
            .orElseGet(() -> CaseMapper.toEntity(caseToSave));
    try {
      CaseEntity saved = cases.saveAndFlush(entity);
      return CaseMapper.toDomain(saved);
    } catch (ObjectOptimisticLockingFailureException ex) {
      throw new WksConflictException(
          "Case " + caseToSave.id() + " was modified by another transaction; reload and retry", ex);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Case> findById(UUID id) {
    return cases.findById(id).map(CaseMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CaseSummary> findByCaseType(CaseQuery query, PageRequest pageRequest) {
    Pageable pageable =
        org.springframework.data.domain.PageRequest.of(
            pageRequest.page(), pageRequest.size(), toSpringSort(pageRequest.sort()));
    org.springframework.data.domain.Page<CaseSummaryProjection> projection =
        cases.findSummaryByCaseType(query.caseTypeId(), query.status().orElse(null), pageable);

    List<CaseSummary> content =
        projection.getContent().stream().map(CaseRepositoryAdapter::toDomainSummary).toList();
    return new Page<>(
        content, projection.getTotalElements(), pageRequest.page(), pageRequest.size());
  }

  private static CaseEntity applyUpdate(CaseEntity existing, Case domain) {
    existing.setCaseTypeId(domain.caseTypeId());
    existing.setCaseTypeVersion(domain.caseTypeVersion());
    existing.setStatus(domain.status());
    existing.setAssignee(domain.assignee());
    existing.setData(domain.data());
    existing.setProcessInstanceId(domain.processInstanceId());
    // Pin the optimistic-lock version to the caller's expected value (P1) — Hibernate's @Version
    // comparison will then fire on flush against caller intent, closing the TOCTOU window between
    // the service's read and this adapter's write when no enclosing transaction binds them.
    existing.setExpectedVersion(domain.version());
    // createdBy / createdAt are immutable post-create; never overwritten on update.
    // Story 4.4b AC6 (review I13) — LOAD-BEARING SKIP: currentStageId and currentStageOrdinal are
    // intentionally NOT updated here. These two cache fields are maintained exclusively by
    // WksStageAdvancer via a dedicated JPQL UPDATE in CaseEntityRepository (see
    // CaseEntityRepository:46). Any attempt to drive them through applyUpdate would collide with
    // WksStageAdvancer's write path and risk wiping the post-advance values on the next
    // CaseService.update call. Safe-by-design, not safe-by-coincidence.
    return existing;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<UUID, Map<String, Object>> findDataByIds(
      Collection<UUID> ids, Set<String> projectedFieldIds) {
    if (ids == null || ids.isEmpty()) {
      return Map.of();
    }
    Set<String> projection = projectedFieldIds == null ? Set.of() : projectedFieldIds;
    return cases.findDataByIds(ids).stream()
        .collect(
            Collectors.toUnmodifiableMap(
                CaseEntity::getId, e -> filterFields(e.getData(), projection)));
  }

  private static Map<String, Object> filterFields(
      Map<String, Object> data, Set<String> projectedFieldIds) {
    if (data == null || data.isEmpty() || projectedFieldIds.isEmpty()) {
      return Map.of();
    }
    Map<String, Object> filtered = new HashMap<>();
    for (String key : projectedFieldIds) {
      if (data.containsKey(key)) {
        filtered.put(key, data.get(key));
      }
    }
    return Map.copyOf(filtered);
  }

  @Override
  @Transactional
  public int updateCaseTypeVersion(UUID caseId, int toCaseTypeVersion, long expectedVersion) {
    return cases.updateCaseTypeVersion(caseId, toCaseTypeVersion, expectedVersion);
  }

  @Override
  @Transactional
  public int updateCaseTypeVersionAndStage(
      UUID caseId,
      int toCaseTypeVersion,
      String toStageId,
      int toStageOrdinal,
      long expectedVersion) {
    return cases.updateCaseTypeVersionAndStage(
        caseId, toCaseTypeVersion, toStageId, toStageOrdinal, expectedVersion);
  }

  private static CaseSummary toDomainSummary(CaseSummaryProjection p) {
    // The list path returns empty fields here; the service post-enriches via findDataByIds using
    // caseType.listColumns (Story 2.3 D4).
    return new CaseSummary(
        p.id(), p.caseTypeId(), p.status(), p.assignee(), p.createdAt(), p.updatedAt(), Map.of());
  }

  private static Sort toSpringSort(List<SortOrder> sort) {
    if (sort == null || sort.isEmpty()) {
      return Sort.unsorted();
    }
    return Sort.by(
        sort.stream()
            .map(s -> s.ascending() ? Sort.Order.asc(s.property()) : Sort.Order.desc(s.property()))
            .toList());
  }
}
