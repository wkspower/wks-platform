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
package com.wks.bpm.externaltask.worker.impl;

import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import com.wks.api.client.gateway.impl.CaseInstanceApiGateway;
import com.wks.bpm.externaltask.worker.BpmnError;
import com.wks.bpm.externaltask.worker.WksExternalTaskHandler;

/**
 * @author victor.franca
 *
 */
@Configuration
@ExternalTaskSubscription(topicName = "validateCaseBusinessKey", includeExtensionProperties = true)
public class ValidateCaseBusinessKeyWorker extends WksExternalTaskHandler {

	@Autowired
	private CaseInstanceApiGateway caseInstanceApiGateway;

	@Override
	protected Optional<Map<String, Object>> doExecute(ExternalTask externalTask, ExternalTaskService externalTaskService) throws BpmnError {

		try {
			caseInstanceApiGateway.get(externalTask.getVariable("caseInstanceBusinessKey"));
			return Optional.empty();
		} catch (NotFound ex) {
			externalTaskService.handleBpmnError(externalTask, BpmnErrorCode.CASE_NOT_FOUND.getCode());
			throw new BpmnError(ex);
		}
	}
}
