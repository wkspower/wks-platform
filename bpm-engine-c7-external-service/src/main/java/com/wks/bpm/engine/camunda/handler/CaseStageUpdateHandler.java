package com.wks.bpm.engine.camunda.handler;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.cases.instance.CaseInstanceService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ExternalTaskSubscription(topicName = "caseStageUpdate", includeExtensionProperties = true)
@Slf4j
public class CaseStageUpdateHandler implements ExternalTaskHandler {

	@Autowired
	private SecurityContextTenantHolder holder;

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private ExternalServiceErrorHandler errorHandler;

	@Override
	public void execute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {
		try {
			log.info("Starting External Task Handler processing '{}' for tenant '{}'", externalTask.getActivityId());
			
			if (externalTask.getTenantId() == null) {
				log.warn("Could not start External Task Handler processing '{}' without tenant id", externalTask.getActivityId());
				return;
			}
			
			holder.setTenantId(externalTask.getTenantId());
			
			caseInstanceService.updateStage(externalTask.getBusinessKey(), externalTask.getVariable("stage"));
			
			externalTaskService.complete(externalTask);
		} catch (Exception e) {
			log.error("Error updating case stage with business key: {} and new stage: {}", externalTask.getBusinessKey(),  externalTask.getVariable("stage"));
			errorHandler.handle("Error updating case stage", externalTaskService, externalTask, e);
		} finally {
			log.info("Finishing External Task Handler activity '{}' for tenant '{}'", externalTask.getActivityId(), externalTask.getTenantId());
			holder.clear();
		}
	}

}