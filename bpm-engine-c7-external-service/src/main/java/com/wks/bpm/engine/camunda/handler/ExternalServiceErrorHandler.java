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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceErrorHandler {

	private static final int DEFAULT_RETRY = 3;

	public void handle(String errorMessage, ExternalTaskService externalTaskService, ExternalTask externalTask,
			Exception e) {
		StringWriter stringWriter = new StringWriter();

		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);

		int retries = externalTask.getRetries() == null ? DEFAULT_RETRY : externalTask.getRetries() - 1;

		externalTaskService.handleFailure(externalTask, errorMessage, stringWriter.toString(), retries, 3000L);
	}

}
