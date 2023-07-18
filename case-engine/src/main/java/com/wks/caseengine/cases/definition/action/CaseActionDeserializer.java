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

		String actionType = jsonObject.get("actionType").getAsString();

		if (CaseActionType.CASE_STAGE_UPDATE_ACTION.getCode().equals(actionType)) {
			return new Gson().fromJson(jsonObject, CaseStageUpdateAction.class);

		} else if (CaseActionType.CASE_QUEUE_UPDATE_ACTION.getCode().equals(actionType)) {
			return new Gson().fromJson(jsonObject, CaseQueueAssignAction.class);
		}

		throw new JsonParseException("Case Action Type Not Identified", new CaseActionTypeNotIdentifiedException());
	}

}
