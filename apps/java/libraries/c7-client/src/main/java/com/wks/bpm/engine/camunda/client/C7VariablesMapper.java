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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.model.spi.ProcessVariable;

@Component
@Qualifier("c7VariablesMapper")
public class C7VariablesMapper implements VariablesMapper<String> {

	@Override
	public JsonObject toJsonObject(JsonArray caseAttributes) {

		JsonObject processVariables = new JsonObject();

		caseAttributes.forEach(caseAttribute -> {
			JsonObject valueObject = new JsonObject();
			valueObject.addProperty("value", caseAttribute.getAsJsonObject().get("value").toString());
			valueObject.addProperty("type", caseAttribute.getAsJsonObject().get("type").getAsString());
			processVariables.add(caseAttribute.getAsJsonObject().get("name").getAsString(), valueObject);

		});

		return processVariables;
	}

	@Override
	public ProcessVariable[] toProcessVariablesArray(String variablesJsonString) {
		JsonObject jsonObject = JsonParser.parseString(variablesJsonString).getAsJsonObject();

		ProcessVariable[] processVariables = new ProcessVariable[jsonObject.size()];

		int i = 0;
		for (String key : jsonObject.keySet()) {

			JsonObject variableObject = jsonObject.getAsJsonObject(key);
			String type = variableObject.get("type").getAsString();
			processVariables[i] = ProcessVariable.builder().name(key).type(type).value(variableObject.get("value"))
					.build();
			i++;
		}

		return processVariables;
	}

}
