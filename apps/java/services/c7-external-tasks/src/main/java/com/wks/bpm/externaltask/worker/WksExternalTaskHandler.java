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
package com.wks.bpm.externaltask.worker;

import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;

import com.wks.api.security.context.SecurityContextTenantHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * @author victor.franca
 *
 */
@Slf4j
public abstract class WksExternalTaskHandler implements ExternalTaskHandler {

	@Autowired
	private SecurityContextTenantHolder securityContext;

	@Autowired
	private ExternalServiceErrorHandler errorHandler;

	@Override
	public void execute(final ExternalTask externalTask, final ExternalTaskService externalTaskService) {
		try {
			log.debug("Starting External Task Handler processing '{}'", externalTask.getActivityId());

			if (externalTask.getTenantId() == null) {
				throw new RuntimeException(
						String.format("Could not start External Task Handler processing %s without tenant id",
								externalTask.getActivityId()));
			}

			securityContext.setTenantId(externalTask.getTenantId());

			Optional<Map<String, Object>> newVariables = doExecute(externalTask, externalTaskService);

			if (newVariables.isEmpty()) {
				externalTaskService.complete(externalTask);
			}else {
				externalTaskService.complete(externalTask, newVariables.get());
			}
			
		} catch (BpmnError bpmnError) {
			log.debug("BPMN Error on external task {} with businessKey {}", externalTask.getActivityId(),
					externalTask.getBusinessKey(), bpmnError);
		} catch (Exception e) {
			log.error("Error on external task {} with businessKey {}", externalTask.getActivityId(),
					externalTask.getBusinessKey(), e);
			errorHandler.handle("Error on external task " + externalTask.getActivityId(), externalTaskService,
					externalTask, e);
		} finally {
			log.debug("Finishing External Task Handler activity '{}' for tenant '{}'", externalTask.getActivityId(),
					externalTask.getTenantId());
			securityContext.clear();
		}
	}

	abstract protected Optional<Map<String, Object>> doExecute(final ExternalTask externalTask,
			final ExternalTaskService externalTaskService) throws BpmnError;
}
