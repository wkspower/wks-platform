package com.wks.caseengine.cases.definition.action;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CaseActionSerializer<T extends CaseAction> implements JsonSerializer<T> {

	@Override
	public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
		// TODO Auto-generated method stub
		return new Gson().toJsonTree(src);
	}

}
