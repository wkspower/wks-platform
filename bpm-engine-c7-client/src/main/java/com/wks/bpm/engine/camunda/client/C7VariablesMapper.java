package com.wks.bpm.engine.camunda.client;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.bpm.engine.client.VariablesMapper;

@Component
public class C7VariablesMapper implements VariablesMapper {

	@Override
	public JsonObject map(JsonArray caseAttributes) {

		JsonObject processVariables = new JsonObject();

		caseAttributes.forEach(caseAttribute -> {
			JsonObject valueObject = new JsonObject();
			valueObject.addProperty("value", caseAttribute.getAsJsonObject().get("value").getAsString());
			valueObject.addProperty("type", caseAttribute.getAsJsonObject().get("type").getAsString());
			processVariables.add(caseAttribute.getAsJsonObject().get("name").getAsString(), valueObject);

		});

		return processVariables;
	}

}
