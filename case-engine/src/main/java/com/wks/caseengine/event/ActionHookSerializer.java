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
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wks.caseengine.json.GsonBuilderFactory;
import com.wks.caseengine.tasks.event.complete.CaseEventType;
import com.wks.caseengine.tasks.event.complete.TaskCompleteHook;

public class ActionHookSerializer<T extends ActionHook> implements JsonSerializer<T> {

	@Override
	public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {

		Gson gson = new GsonBuilderFactory().getGsonBuilder().create();

		JsonElement jsonElement = gson.toJsonTree(src);

		if (src instanceof TaskCompleteHook) {
			jsonElement.getAsJsonObject().addProperty("eventType", CaseEventType.TASK_COMPLETE_EVENT_TYPE.getCode());

		}

		return jsonElement;
	}

}
