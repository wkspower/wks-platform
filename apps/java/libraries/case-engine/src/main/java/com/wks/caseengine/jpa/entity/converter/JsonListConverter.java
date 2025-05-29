package com.wks.caseengine.jpa.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

@Converter(autoApply = true)
public class JsonListConverter<T> implements AttributeConverter<List<T>, String> {
	
	private final Gson gson = new Gson();
	
	private final Type listType = new TypeToken<List<T>>() {}.getType();

	@Override
	public String convertToDatabaseColumn(List<T> attribute) {
		return (attribute != null) ? gson.toJson(attribute) : null;
	}

	@Override
	public List<T> convertToEntityAttribute(String dbData) {
		return (dbData != null && !dbData.isEmpty()) ? gson.fromJson(dbData, listType) : null;
	}
	
}
