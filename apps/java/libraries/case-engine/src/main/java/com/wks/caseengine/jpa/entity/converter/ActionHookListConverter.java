package com.wks.caseengine.jpa.entity.converter;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.event.ActionHook;
import com.wks.caseengine.json.GsonBuilderFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActionHookListConverter implements AttributeConverter<List<ActionHook>, String> {

	private GsonBuilder gsonBuilder;
	
    private static final Type listType = new TypeToken<List<ActionHook>>() {}.getType();
    
    public ActionHookListConverter() {
    	this.gsonBuilder = new GsonBuilderFactory().getGsonBuilder();
	}

    @Override
    public String convertToDatabaseColumn(List<ActionHook> actionHooks) {
    	Gson gson = gsonBuilder.create();
        return gson.toJson(actionHooks, listType);
    }

    @Override
    public List<ActionHook> convertToEntityAttribute(String dbData) {
    	Gson gson = gsonBuilder.create();
        return gson.fromJson(dbData, listType);
    }
}
