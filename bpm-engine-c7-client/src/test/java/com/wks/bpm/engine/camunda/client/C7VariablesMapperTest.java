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
		String caseAttributes = "[" + "{\"name\": \"when\",\"value\": \"01/01/1990\"},"
				+ "{\"name\": \"where\",\"value\": \"Toronto\"}]";

		// when
		JsonObject processVariables = c7VariablesMapper.map(new Gson().fromJson(caseAttributes, JsonArray.class));

		assertEquals("{\"value\":\"01/01/1990\",\"type\":\"String\"}", String.valueOf(processVariables.get("when")));
		assertEquals("{\"value\":\"Toronto\",\"type\":\"String\"}", String.valueOf(processVariables.get("where")));
	}

}
