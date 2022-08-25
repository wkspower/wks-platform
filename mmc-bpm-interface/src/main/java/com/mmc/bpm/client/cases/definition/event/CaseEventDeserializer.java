package com.mmc.bpm.client.cases.definition.event;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CaseEventDeserializer implements JsonDeserializer<CaseEvent> {

	@Override
	public CaseEvent deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		final JsonObject jsonObject = jsonElement.getAsJsonObject();
		if (jsonObject.get("type") == null) {
			throw new JsonParseException(new InvalidEventException("missing Event Type"));
		}

		//TODO Refactor as dynamic/decoupled validation rules
		if (CaseEventType.PROCESS_START.getCode().equals(jsonObject.get("type").getAsString())) {
			if (jsonObject.get("eventPayload").getAsJsonObject().get("processDefinitionKey") == null) {
				throw new JsonParseException(
						new InvalidEventException("missing Event Payload: Process Definition Key "));
			}
		}

		return new Gson().fromJson(jsonObject, CaseEvent.class);
	}

}
