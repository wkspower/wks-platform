package com.wks.caseengine.jpa.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import com.wks.caseengine.cases.definition.DocumentRequirement;

@Converter
public class DocumentRequirementListConverter implements AttributeConverter<List<DocumentRequirement>, String> {

	private final Gson gson = new Gson();

	@Override
	public String convertToDatabaseColumn(List<DocumentRequirement> attribute) {
		return (attribute != null) ? gson.toJson(attribute) : "null";
	}

	@Override
	public List<DocumentRequirement> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.equals("null")) {
			return null;
		}
		Type listType = new TypeToken<List<DocumentRequirement>>() {
		}.getType();
		return gson.fromJson(dbData, listType);
	}

}
