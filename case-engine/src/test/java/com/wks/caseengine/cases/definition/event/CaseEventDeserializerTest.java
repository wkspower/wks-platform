package com.wks.caseengine.cases.definition.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.lang.reflect.Type;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.wks.caseengine.cases.definition.event.CaseEvent;
import com.wks.caseengine.cases.definition.event.CaseEventDeserializer;
import com.wks.caseengine.cases.definition.event.CaseEventType;

public class CaseEventDeserializerTest {

	@Test
	public void shouldDeserialize() {

		// Given
		String json = "{ \"id\": \"1\", \"name\": \"eventName\", \"type\": \"PROCESS_START_EVENT_TYPE\", \"eventPayload\": {\"processDefinitionKey\": \"processDef-1\"} }";
		JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);

		// When
		CaseEventDeserializer deserializer = new CaseEventDeserializer();
		CaseEvent caseEvent = (CaseEvent) deserializer.deserialize(jsonObject, CaseEvent.class, new MockGsonContext());

		// Then
		assertEquals("1", caseEvent.getId());
		assertEquals("eventName", caseEvent.getName());
		assertEquals(CaseEventType.PROCESS_START, caseEvent.getType());
		assertEquals("processDef-1", caseEvent.getEventPayload().get("processDefinitionKey").getAsString());
	}

	@Test(expected = NullPointerException.class)
	public void shouldRaiseMissingPayloadError() {

		// Given
		String json = "{ \"id\": \"1\", \"name\": \"eventName\", \"type\": \"PROCESS_START_EVENT_TYPE\" }";
		JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);

		// When
		CaseEventDeserializer deserializer = new CaseEventDeserializer();
		deserializer.deserialize(jsonObject, CaseEvent.class, new MockGsonContext());
	}

	private class MockGsonContext implements JsonSerializationContext, JsonDeserializationContext {
		@Override
		public JsonElement serialize(Object src) {
			return null;
		}

		@Override
		public JsonElement serialize(Object src, Type typeOfSrc) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
			return (R) new Gson().fromJson(json, CaseEvent.class);
		}
	};

}
