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
import com.wks.bpm.engine.client.VariablesMapper;

@Component
@Qualifier("c8VariablesMapper")
public class C8VariablesMapper implements VariablesMapper {

	@Override
	public JsonObject map(JsonArray caseAttributes) {

		JsonObject processVariables = new JsonObject();

		caseAttributes.forEach(caseAttribute -> {
			processVariables.add(caseAttribute.getAsJsonObject().get("name").getAsString(),
					caseAttribute.getAsJsonObject().get("value"));
		});

		return processVariables;
	}

}
