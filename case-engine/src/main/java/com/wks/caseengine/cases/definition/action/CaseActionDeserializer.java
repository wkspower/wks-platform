package com.wks.caseengine.cases.definition.action;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CaseActionDeserializer implements JsonDeserializer<CaseAction> {

	@Override
	public CaseAction deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		final JsonObject jsonObject = jsonElement.getAsJsonObject();

		return new Gson().fromJson(jsonObject, CaseStageUpdateAction.class);
	}

}
