package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.port.BpmnValidationService;
import com.wkspower.platform.domain.port.CaseDataValidator;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.service.CaseService;
import com.wkspower.platform.domain.service.ConfigService;
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
      EventPublisher eventPublisher) {
    return new ConfigService(
        source, registrar, reader, bpmnValidator, workflowEngine, eventPublisher);
  }

  @Bean
  public CaseService wksCaseService(
      CaseRepository caseRepository,
      CaseTypeReader caseTypeReader,
      CaseDataValidator caseDataValidator,
      WorkflowEngine workflowEngine,
      ProcessDefinitionKeyResolver processKeyResolver,
      EventPublisher eventPublisher,
      Clock clock) {
    return new CaseService(
        caseRepository,
        caseTypeReader,
        caseDataValidator,
        workflowEngine,
        processKeyResolver,
        eventPublisher,
        clock);
  }
}
