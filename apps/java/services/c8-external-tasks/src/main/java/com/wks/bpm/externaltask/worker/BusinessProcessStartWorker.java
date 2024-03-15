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

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.wks.bpm.externaltask.api.gateway.impl.CaseDefinitionApiGateway;
import com.wks.bpm.externaltask.api.gateway.impl.ProcessDefinitionApiGateway;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;

/**
 * @author victor.franca
 *
 */
@Component
@Slf4j
public class BusinessProcessStartWorker {

	@Autowired
	private CaseDefinitionApiGateway caseDefinitionApiGateway;

	@Autowired
	private ProcessDefinitionApiGateway processDefinitionApiGateway;

	@Autowired
	private GsonBuilder gsonBuilder;

	@JobWorker(type = "businessProcessStart", fetchVariables = { "caseInstance" })
	public void handleJobFoo(final JobClient client, final ActivatedJob job) {

		log.info("Starting Worker '{}'", job.getType());

		String caseInstance = gsonBuilder.create()
				.toJson((LinkedHashMap<?, ?>) job.getVariablesAsMap().get("caseInstance"));

		String processDefKey = getProcessDefinitionId(gsonBuilder.create().fromJson(caseInstance, JsonObject.class));

		processDefinitionApiGateway.start(processDefKey, caseInstance);
	}

	private String getProcessDefinitionId(JsonObject caseInstanceJson) {
		String caseDefJsonString = caseDefinitionApiGateway.get(caseInstanceJson.get("caseDefinitionId").getAsString());
		JsonObject caseDefJson = gsonBuilder.create().fromJson(caseDefJsonString, JsonObject.class);
		String processDefKey = caseDefJson.get("stagesLifecycleProcessKey").getAsString();
		return processDefKey;
	}

}
