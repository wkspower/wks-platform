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
package com.wks.bpm.engine.camunda.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.model.spi.ProcessVariable;

import io.camunda.operate.model.Variable;

@Component
@Qualifier("c8VariablesMapper")
public class C8VariablesMapper implements VariablesMapper<List<Variable>> {

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public JsonObject toJsonObject(JsonArray caseAttributes) {

		JsonObject processVariables = new JsonObject();

		caseAttributes.forEach(caseAttribute -> {
			processVariables.add(caseAttribute.getAsJsonObject().get("name").getAsString(),
					caseAttribute.getAsJsonObject().get("value"));
		});

		return processVariables;
	}

	@Override
	public ProcessVariable[] toProcessVariablesArray(List<Variable> variablesFromProcessEngine) {

		return variablesFromProcessEngine.stream()
				.map(variable -> ProcessVariable.builder().name(variable.getName())
						.value(gsonBuilder.create().toJsonTree(variable.getValue())).build())
				.toArray(ProcessVariable[]::new);

	}

}
