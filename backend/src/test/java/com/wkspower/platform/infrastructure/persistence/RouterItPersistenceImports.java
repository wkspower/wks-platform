package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseStageHistoryJpaRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test-only helper config that re-exposes the package-private persistence adapters ({@code
 * CaseRepositoryAdapter}, {@code StageRepositoryAdapter}, {@code CaseStatusAdapter}) as top-level
 * beans for slice ITs that live outside this package (Story 4.3's {@code ExecutionSignalRouterIT}).
 * The adapters stay package-private in production; this file is the only test-side bridge.
 */
@TestConfiguration
public class RouterItPersistenceImports {

  @Bean
  public CaseRepository caseRepository(CaseEntityRepository entities) {
    return new CaseRepositoryAdapter(entities);
  }

  @Bean
  public StageRepository stageRepository(
      CaseStageHistoryJpaRepository history, CaseEntityRepository cases) {
    return new StageRepositoryAdapter(history, cases);
  }

  @Bean
  public CaseStatusUpdater caseStatusUpdater(CaseEntityRepository entities, Clock clock) {
    return new CaseStatusAdapter(entities, clock);
  }
}
