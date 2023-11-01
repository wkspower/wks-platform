/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.bpm.engine.camunda.handler;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.service.CaseInstanceService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ExternalTaskSubscription(topicName = "caseQueueUpdate", includeExtensionProperties = true)
@Slf4j
public class CaseQueueUpdateHandler implements ExternalTaskHandler {

	@Autowired
	private SecurityContextTenantHolder securityContext;

	@Autowired
	private CaseInstanceService caseInstanceService;

	@Autowired
	private ExternalServiceErrorHandler errorHandler;

	@Override
	public void execute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {
		try {
			log.debug("Starting External Task Handler processing '{}'", externalTask.getActivityId());

			if (externalTask.getTenantId() == null) {
				log.warn("Could not start External Task Handler processing '{}' without tenant id",
						externalTask.getActivityId());
				return;
			}

			securityContext.setTenantId(externalTask.getTenantId());

			CaseInstance mergePatch = CaseInstance.builder().queueId(externalTask.getVariable("queue")).build();

			caseInstanceService.patch(externalTask.getBusinessKey(), mergePatch);

			externalTaskService.complete(externalTask);
		} catch (Exception e) {
			log.error("Error updating case queue with business key: {} and new queue: {}",
					externalTask.getBusinessKey(), externalTask.getVariable("stage"));
			errorHandler.handle("Error updating case stage", externalTaskService, externalTask, e);
		} finally {
			log.debug("Finishing External Task Handler activity '{}' for tenant '{}'", externalTask.getActivityId(),
					externalTask.getTenantId());
			securityContext.clear();
		}
	}

}