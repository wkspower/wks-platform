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
package com.wks.caseengine.event;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wks.caseengine.json.GsonBuilderFactory;
import com.wks.caseengine.tasks.event.complete.CaseEventType;
import com.wks.caseengine.tasks.event.complete.TaskCompleteHook;

public class ActionHookDeserializer implements JsonDeserializer<ActionHook> {

	@Override
	public ActionHook deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		final JsonObject jsonObject = jsonElement.getAsJsonObject();

		String eventType = jsonObject.get("eventType").getAsString();
		
		Gson gson = new GsonBuilderFactory().getGsonBuilder().create();

		if (CaseEventType.TASK_COMPLETE_EVENT_TYPE.getCode().equals(eventType)) {
			return gson.fromJson(jsonObject, TaskCompleteHook.class);
		}

		throw new JsonParseException("Action Hook Type Not Identified", new ActionHookTypeNotIdentified());
	}

}
