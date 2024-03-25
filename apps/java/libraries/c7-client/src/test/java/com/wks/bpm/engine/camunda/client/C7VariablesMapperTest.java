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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import org.camunda.community.rest.client.dto.VariableValueDto;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wks.bpm.engine.camunda.client.C7VariablesMapper;
import com.wks.bpm.engine.model.spi.ProcessVariable;

public class C7VariablesMapperTest {

	private C7VariablesMapper c7VariablesMapper = new C7VariablesMapper();

	@Test
	public void testMap() {
		// given
		String caseAttributes = "[{\"name\": \"when\",\"value\": \"01/01/1990\",\"type\": \"String\"},{\"name\": \"where\",\"value\": \"Toronto\",\"type\": \"String\"}]";

		// when
		Type listType = new TypeToken<ArrayList<ProcessVariable>>() {
		}.getType();
		Map<String, VariableValueDto> processVariables = c7VariablesMapper
				.toEngineFormat(new Gson().fromJson(caseAttributes, listType));

		// then
		assertEquals("{\"value\":\"01/01/1990\",\"type\":\"String\"}", convertToJson(processVariables.get("when")));
		assertEquals("{\"value\":\"Toronto\",\"type\":\"String\"}", convertToJson(processVariables.get("where")));
	}

	private String convertToJson(VariableValueDto variableValueDto) {
		return "{\"value\":\"" + variableValueDto.getValue() + "\",\"type\":\"" + variableValueDto.getType() + "\"}";
	}

}
