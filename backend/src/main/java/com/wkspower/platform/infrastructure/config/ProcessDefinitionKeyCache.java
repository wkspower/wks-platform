package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import com.wkspower.platform.infrastructure.persistence.entity.CaseTypeDeploymentEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeDeploymentJpaRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Write-through cache mapping {@code caseTypeId → processDefinitionKey}. The hot path is a
 * lock-free {@link ConcurrentHashMap} read; misses fall back to the JPA-backed {@code
 * case_type_deployments} table populated on every {@link ConfigDeployed} event.
 *
 * <p>Story 2.4 folded debt #1: before this story the cache was in-memory only, so a JVM restart
 * lost the mapping for a case-type that had been admin-deployed without YAML on disk. The Flyway
 * table {@code case_type_deployments} (V202604270001) makes the mapping durable; this class hides
 * the table behind the {@link ProcessDefinitionKeyResolver} port so callers don't change.
 */
@Component
class ProcessDefinitionKeyCache implements ProcessDefinitionKeyResolver {

  private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionKeyCache.class);

  private final Map<String, String> keysByCaseTypeId = new ConcurrentHashMap<>();
  private final CaseTypeDeploymentJpaRepository repository;

  ProcessDefinitionKeyCache(CaseTypeDeploymentJpaRepository repository) {
    this.repository = repository;
  }

  /**
   * Populate the cache from the durable mapping after the application context is fully ready.
   * Driven by {@link ApplicationReadyEvent} (rather than {@code @PostConstruct}) so that (a) the
   * Spring transactional proxy is wired by the time we touch JPA, and (b) any {@link
   * ConfigDeployed} event already fired during boot has had a chance to populate the cache —
   * {@code putIfAbsent} avoids clobbering an entry the deploy stream has already advanced past
   * the row we are about to read (Story 2.4 review).
   */
  @EventListener(ApplicationReadyEvent.class)
  @Transactional(readOnly = true)
  public void hydrateFromDatabase() {
    int hydrated = 0;
    for (CaseTypeDeploymentEntity row : repository.findAll()) {
      String previous =
          keysByCaseTypeId.putIfAbsent(row.getCaseTypeId(), row.getProcessDefinitionKey());
      if (previous == null) {
        hydrated++;
      }
    }
    if (hydrated > 0) {
      log.info(
          "ProcessDefinitionKeyCache hydrated {} caseType→processDefinitionKey rows", hydrated);
    }
  }

  /**
   * On every successful deploy: write the mapping through to the table (upsert by primary key) and
   * update the in-memory cache. The repository's {@code save} performs the upsert because the
   * primary key is the caller-supplied {@code caseTypeId}.
   */
  @EventListener
  @Transactional
  public void onConfigDeployed(ConfigDeployed event) {
    if (event.processDefinitionKey() == null) {
      return;
    }
    Instant deployedAt = event.timestamp() == null ? Instant.now() : event.timestamp();
    String deploymentId = event.deploymentId() == null ? "" : event.deploymentId();
    repository
        .findById(event.caseTypeId())
        .ifPresentOrElse(
            existing -> {
              existing.setCaseTypeVersion(event.version());
              existing.setProcessDefinitionKey(event.processDefinitionKey());
              existing.setDeploymentId(deploymentId);
              existing.setDeployedAt(deployedAt);
              repository.save(existing);
            },
            () ->
                repository.save(
                    new CaseTypeDeploymentEntity(
                        event.caseTypeId(),
                        event.version(),
                        event.processDefinitionKey(),
                        deploymentId,
                        deployedAt)));
    keysByCaseTypeId.put(event.caseTypeId(), event.processDefinitionKey());
  }

  @Override
  public Optional<String> resolve(String caseTypeId) {
    String cached = keysByCaseTypeId.get(caseTypeId);
    if (cached != null) {
      return Optional.of(cached);
    }
    // Cold-cache miss after boot — defensive fallback that re-reads the table. Hot-path stays
    // lock-free; this branch only fires if the @PostConstruct hydration was bypassed (e.g. test
    // harnesses that wire the bean manually) or the row landed after hydration via another node.
    return repository
        .findById(caseTypeId)
        .map(
            row -> {
              keysByCaseTypeId.put(row.getCaseTypeId(), row.getProcessDefinitionKey());
              return row.getProcessDefinitionKey();
            });
  }
}
