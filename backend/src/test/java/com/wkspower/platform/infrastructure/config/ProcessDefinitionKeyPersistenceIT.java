package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import com.wkspower.platform.infrastructure.persistence.entity.CaseTypeDeploymentEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeDeploymentJpaRepository;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Story 2.4 folded debt #1 — verifies the {@code case_type_deployments} mapping is durable across
 * the in-memory cache. Two cases:
 *
 * <ul>
 *   <li>Pre-existing row in {@code case_type_deployments} resolves via the {@code
 *       ProcessDefinitionKeyResolver} (proves boot-time hydration / cold-cache fallback).
 *   <li>{@link ConfigDeployed} event upserts the table AND the cache, so subsequent calls hit the
 *       in-memory map.
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("dev")
class ProcessDefinitionKeyPersistenceIT {

  @TempDir static Path dbDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry reg) {
    reg.add(
        "spring.datasource.url",
        () -> "jdbc:h2:file:" + dbDir.resolve("pdk-it") + ";DB_CLOSE_DELAY=-1");
    reg.add("wks.case-types.dir", () -> "");
    reg.add("camunda.bpm.generic-properties.properties.enforceHistoryTimeToLive", () -> "false");
  }

  @Autowired private CaseTypeDeploymentJpaRepository deployments;
  @Autowired private ProcessDefinitionKeyResolver resolver;
  @Autowired private ApplicationEventPublisher events;

  @Test
  void rowsLandedDirectlyAreResolvedViaCacheFallback() {
    CaseTypeDeploymentEntity row =
        new CaseTypeDeploymentEntity(
            "pre-seeded", 1, "pre-seeded-process", "deployment-x", Instant.now());
    deployments.save(row);

    assertThat(resolver.resolve("pre-seeded")).contains("pre-seeded-process");
  }

  @Test
  void configDeployedEventPersistsAndPopulatesCache() {
    events.publishEvent(
        new ConfigDeployed(
            "event-deployed",
            2,
            "deployment-y",
            "event-deployed-process",
            "procDef-y",
            null,
            Instant.now()));

    assertThat(resolver.resolve("event-deployed")).contains("event-deployed-process");
    assertThat(deployments.findById("event-deployed"))
        .as("durable mapping persisted via the event listener")
        .isPresent()
        .hasValueSatisfying(
            row -> {
              assertThat(row.getCaseTypeVersion()).isEqualTo(2);
              assertThat(row.getProcessDefinitionKey()).isEqualTo("event-deployed-process");
              assertThat(row.getDeploymentId()).isEqualTo("deployment-y");
            });
  }

  @Test
  void configDeployedSecondTimeUpdatesExistingRow() {
    events.publishEvent(
        new ConfigDeployed(
            "double-deployed",
            1,
            "deployment-1",
            "double-deployed-process-v1",
            "procDef-1",
            null,
            Instant.now()));
    events.publishEvent(
        new ConfigDeployed(
            "double-deployed",
            2,
            "deployment-2",
            "double-deployed-process-v2",
            "procDef-2",
            null,
            Instant.now()));

    assertThat(resolver.resolve("double-deployed")).contains("double-deployed-process-v2");
    assertThat(deployments.findById("double-deployed"))
        .hasValueSatisfying(row -> assertThat(row.getCaseTypeVersion()).isEqualTo(2));
  }
}
