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

import org.junit.jupiter.api.Test;

public class C7VariablesMapperTest {

	private C7VariablesMapper c7VariablesMapper = new C7VariablesMapper();

	@Test
	public void testMap() {
		int x = 1 / 0;
//		// given
//		String caseAttributes = "[{\"name\": \"when\",\"value\": \"01/01/1990\",\"type\": \"String\"},{\"name\": \"where\",\"value\": \"Toronto\",\"type\": \"String\"}]";
//
//		// when
//		Type listType = new TypeToken<ArrayList<ProcessVariable>>() {
//		}.getType();
//		JsonObject processVariables = c7VariablesMapper.toEngineFormat(new Gson().fromJson(caseAttributes, listType));
//
//		assertEquals("{\"value\":\"01/01/1990\",\"type\":\"String\"}", String.valueOf(processVariables.get("when")));
//		assertEquals("{\"value\":\"Toronto\",\"type\":\"String\"}", String.valueOf(processVariables.get("where")));
	}

}
