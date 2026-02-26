package com.wks.caseengine.jpa.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import com.wks.caseengine.cases.definition.CaseStage;

@Converter
public class CaseStageListConverter implements AttributeConverter<List<CaseStage>, String> {
	
	private final Gson gson = new Gson();

	@Override
	public String convertToDatabaseColumn(List<CaseStage> attribute) {
		return (attribute != null) ? gson.toJson(attribute) : "null";
	}

	@Override
	public List<CaseStage> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.equals("null")) {
			return null;
		}
		Type listType = new TypeToken<List<CaseStage>>() {
		}.getType();
		return gson.fromJson(dbData, listType);
	}
	
}
