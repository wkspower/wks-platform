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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wks.api.client.gateway.impl.CaseDefinitionApiGateway;
import com.wks.api.client.gateway.impl.ProcessDefinitionApiGateway;
import com.wks.api.dto.ProcessDefinitionStartDto;
import com.wks.bpm.engine.model.spi.ProcessVariable;

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
	public void handleJob(final JobClient client, final ActivatedJob job) {

		log.info("Starting Worker '{}'", job.getType());

		String caseInstanceJson = String.valueOf(job.getVariablesAsMap().get("caseInstance"));

		JsonObject jsonObject = gsonBuilder.create().fromJson(caseInstanceJson, JsonObject.class);

		JsonArray caseAttributesArray = jsonObject.get("attributes").getAsJsonArray();
		List<ProcessVariable> processVariables = new ArrayList<>();
		for (JsonElement element : caseAttributesArray) {
			processVariables.add(gsonBuilder.create().fromJson(element, ProcessVariable.class));
		}

		processDefinitionApiGateway.start(getProcessDefinitionId(jsonObject), ProcessDefinitionStartDto.builder()
				.processVariables(processVariables).businessKey(jsonObject.get("businessKey").getAsString()).build());
	}

	private String getProcessDefinitionId(JsonObject caseInstanceJson) {
		String caseDefJsonString = caseDefinitionApiGateway.get(caseInstanceJson.get("caseDefinitionId").getAsString());
		JsonObject caseDefJson = gsonBuilder.create().fromJson(caseDefJsonString, JsonObject.class);
		String processDefKey = caseDefJson.get("stagesLifecycleProcessKey").getAsString();
		return processDefKey;
	}

}
