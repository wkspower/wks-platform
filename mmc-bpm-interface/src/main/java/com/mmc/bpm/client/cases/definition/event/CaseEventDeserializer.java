package com.mmc.bpm.client.cases.definition.event;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CaseEventDeserializer<T extends CaseEvent> implements JsonDeserializer<T> {

	@Override
	public T deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		final JsonObject jsonObject = jsonElement.getAsJsonObject();

		return context.deserialize(jsonObject, ProcessStartEvent.class);
	}

}
