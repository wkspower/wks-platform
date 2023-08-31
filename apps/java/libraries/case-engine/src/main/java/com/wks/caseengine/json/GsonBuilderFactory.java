package com.wks.caseengine.json;

import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.wks.caseengine.cases.definition.action.CaseAction;
import com.wks.caseengine.cases.definition.action.CaseActionDeserializer;
import com.wks.caseengine.cases.definition.action.CaseActionSerializer;
import com.wks.caseengine.event.ActionHook;
import com.wks.caseengine.event.ActionHookDeserializer;
import com.wks.caseengine.event.ActionHookSerializer;

@Component
public class GsonBuilderFactory {

	public GsonBuilder getGsonBuilder() {
		GsonBuilder builder = new GsonBuilder();

		builder.registerTypeAdapter(ActionHook.class, new ActionHookDeserializer());
		builder.registerTypeAdapter(ActionHook.class, new ActionHookSerializer<>());

		builder.registerTypeAdapter(CaseAction.class, new CaseActionDeserializer());
		builder.registerTypeAdapter(CaseAction.class, new CaseActionSerializer<>());

		return builder;
	}

}
