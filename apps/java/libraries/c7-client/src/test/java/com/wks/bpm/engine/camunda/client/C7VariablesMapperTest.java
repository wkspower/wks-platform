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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class C7VariablesMapperTest {

	private C7VariablesMapper c7VariablesMapper = new C7VariablesMapper();

	@Test
	public void testMap() {

		// given
		String caseAttributes = "[{\"name\": \"when\",\"value\": \"01/01/1990\",\"type\": \"String\"},{\"name\": \"where\",\"value\": \"Toronto\",\"type\": \"String\"}]";

		// when
		JsonObject processVariables = c7VariablesMapper.toJsonObject(new Gson().fromJson(caseAttributes, JsonArray.class));

		assertEquals("{\"value\":\"01/01/1990\",\"type\":\"String\"}", String.valueOf(processVariables.get("when")));
		assertEquals("{\"value\":\"Toronto\",\"type\":\"String\"}", String.valueOf(processVariables.get("where")));
	}

}
