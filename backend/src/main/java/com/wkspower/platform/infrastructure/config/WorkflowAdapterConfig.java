package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.service.WorkflowAdapterBinder;
import com.wkspower.platform.domain.service.ExecutionSignalRouter;
import com.wkspower.platform.domain.service.MappingRegistry;
import com.wkspower.platform.domain.service.NullAdapter;
import com.wkspower.platform.domain.service.WksStageAdvancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Story 4.1 — wires the pure-Java {@link NullAdapter} and {@link WorkflowAdapterBinder} (both in
 * {@code domain/service}, framework-free per NFR36) as Spring singletons so call sites (Stories 4.4
 * / 4.5) can inject {@code WorkflowAdapterBinder}.
 *
 * <p>Story 4.3 — extends with two new beans: {@link MappingRegistry} (runtime index) and {@link
 * ExecutionSignalRouter} (single subscriber for every {@code WorkflowAdapter.onExecutionSignal}
 * surface).
 *
 * <p>Story 4.4a — wires the binder with the production {@link ExecutionSignalRouter} as its
 * single-subscriber handler so every adapter registered through {@link
 * WorkflowAdapterBinder#register(com.wkspower.platform.domain.port.CaseTypeRef,
 * com.wkspower.platform.domain.port.WorkflowAdapter)} is automatically subscribed to the router.
 * Routes {@code BpmnWorkflowAdapter}'s emissions through the Mapping Layer (Decision 22).
 */
@Configuration
public class WorkflowAdapterConfig {

  @Bean
  public NullAdapter nullAdapter() {
    return new NullAdapter();
  }

  @Bean
  public WorkflowAdapterBinder backendAdapterBinder(
      NullAdapter nullAdapter, ExecutionSignalRouter backendSignalRouter) {
    return new WorkflowAdapterBinder(nullAdapter, backendSignalRouter);
  }

  /**
   * Story 4.3 AC3 — singleton runtime registry of validated {@code MappingDefinition}s, populated
   * by {@code ConfigService} after every successful CaseType registration.
   */
  @Bean
  public MappingRegistry mappingRegistry() {
    return new MappingRegistry();
  }

  /**
   * Story 4.3 AC1 — single routing surface for every {@code ExecutionSignal}. Domain-side dependency
   * on {@link WksStageAdvancer} (Story 3.1), {@link CaseStatusUpdater} (Story 2.4), {@link
   * CaseRepository}, {@link EventPublisher}, and {@link Clock} — all already wired by other
   * infrastructure configs.
   */
  @Bean
  public ExecutionSignalRouter backendSignalRouter(
      MappingRegistry mappingRegistry,
      WksStageAdvancer stageAdvancer,
      CaseStatusUpdater statusUpdater,
      CaseRepository caseRepository,
      EventPublisher eventPublisher,
      Clock clock,
      CaseTypeReader caseTypeReader) {
    return new ExecutionSignalRouter(
        mappingRegistry,
        stageAdvancer,
        statusUpdater,
        caseRepository,
        eventPublisher,
        clock,
        caseTypeReader);
  }
}
