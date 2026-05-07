package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.port.BpmnValidationService;
import com.wkspower.platform.domain.port.CaseDataValidator;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.domain.port.StatusOptionsStore;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.ConfigService;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.domain.service.StatusOptionsAdminService;
import com.wkspower.platform.domain.service.TaskService;
import com.wkspower.platform.domain.service.WksStageAdvancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the pure-Java {@link ConfigService} (domain) with its infrastructure ports so Spring can
 * inject it into the startup loader and the admin controller.
 */
@Configuration
public class ConfigServiceConfig {

  @Bean
  public ConfigService configService(
      CaseTypeSource source,
      CaseTypeRegistrar registrar,
      CaseTypeReader reader,
      BpmnValidationService bpmnValidator,
      WorkflowEngine workflowEngine,
      EventPublisher eventPublisher,
      CaseTypeVersionRegistry versionRegistry,
      MappingRegistry mappingRegistry,
      CaseTypeContentHasher caseTypeContentHasher) {
    return new ConfigService(
        source,
        registrar,
        reader,
        bpmnValidator,
        workflowEngine,
        eventPublisher,
        versionRegistry,
        mappingRegistry,
        CaseTypeContentHasher::hashBytes);
  }

  /**
   * Story 3.4 — expose {@link CaseTypeContentHasher} as a bean so the JPA-backed registry adapter
   * can constructor-inject it.
   */
  @Bean
  public CaseTypeContentHasher caseTypeContentHasher() {
    return new CaseTypeContentHasher();
  }

  @Bean
  public TaskService wksTaskService(WorkflowEngine workflowEngine) {
    return new TaskService(workflowEngine);
  }

  @Bean
  public CaseService wksCaseService(
      CaseRepository caseRepository,
      CaseTypeReader caseTypeReader,
      CaseDataValidator caseDataValidator,
      WorkflowEngine workflowEngine,
      ProcessDefinitionKeyResolver processKeyResolver,
      EventPublisher eventPublisher,
      Clock clock,
      WksStageAdvancer stageAdvancer,
      CaseTypeVersionRegistry versionRegistry,
      ExecutionSignalHandler backendSignalRouter,
      CaseStatusUpdater caseStatusUpdater) {
    return new CaseService(
        caseRepository,
        caseTypeReader,
        caseDataValidator,
        workflowEngine,
        processKeyResolver,
        eventPublisher,
        clock,
        stageAdvancer,
        versionRegistry,
        backendSignalRouter,
        caseStatusUpdater);
  }

  /**
   * Story 3.1 — wires the framework-free {@link WksStageAdvancer}. The {@code @Transactional}
   * boundary is applied at this layer (via {@link
   * org.springframework.transaction.annotation.Transactional} on the calling code paths — {@code
   * CaseService.create} sits inside Spring's transactional proxy already, and the stage-advance
   * HTTP endpoints declare {@code @Transactional} on the controller bodies). The domain class
   * itself stays Spring-free per Decision 4 / NFR36.
   */
  /**
   * Story 3.7 — admin status CRUD service. Lives in the domain layer so it stays Spring-free;
   * this @Bean wires the ports from infrastructure (CaseTypeReader, CaseTypeVersionRegistry, the
   * JPA StatusOptionsStore adapter).
   */
  @Bean
  public StatusOptionsAdminService statusOptionsAdminService(
      CaseTypeReader caseTypeReader,
      CaseTypeVersionRegistry versionRegistry,
      StatusOptionsStore statusOptionsStore) {
    return new StatusOptionsAdminService(caseTypeReader, versionRegistry, statusOptionsStore);
  }

  @Bean
  public WksStageAdvancer wksStageAdvancer(
      StageRepository stageRepository, EventPublisher eventPublisher, Clock clock) {
    return new WksStageAdvancer(stageRepository, eventPublisher, clock);
  }
}
