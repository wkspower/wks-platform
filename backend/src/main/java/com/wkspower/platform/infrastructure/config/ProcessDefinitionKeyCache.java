package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * In-memory mapping of {@code caseTypeId → processDefinitionKey}. Populated by listening to {@code
 * ConfigDeployed} events from {@code ConfigService.deploy} and {@code CaseTypeStartupLoader}. Reads
 * are lock-free via {@link ConcurrentHashMap}.
 *
 * <p>Phase 0 trade-off: the cache is rebuilt at boot from the startup-loader's deploy events, so a
 * cold start always re-publishes every observed mapping. If an operator deploys a case-type via
 * HTTP and then restarts the JVM without the YAML on disk, the case-type stays registered (via
 * persistence) but the cache loses the key — Story 2.4 will need a persistent mapping if that
 * restart-survival case becomes load-bearing.
 */
@Component
class ProcessDefinitionKeyCache implements ProcessDefinitionKeyResolver {

  private final Map<String, String> keysByCaseTypeId = new ConcurrentHashMap<>();

  @EventListener
  public void onConfigDeployed(ConfigDeployed event) {
    if (event.processDefinitionKey() != null) {
      keysByCaseTypeId.put(event.caseTypeId(), event.processDefinitionKey());
    }
  }

  @Override
  public Optional<String> resolve(String caseTypeId) {
    return Optional.ofNullable(keysByCaseTypeId.get(caseTypeId));
  }
}
