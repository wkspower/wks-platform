package com.wks.caseengine.jpa.entity.converter;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.cases.instance.CaseMilestoneState;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores a case's achieved milestones as JSON in a single column, matching how the
 * other per-case lists (documents, comments, attributes) are held.
 */
@Converter(autoApply = false)
public class CaseMilestoneStateListConverter
		implements AttributeConverter<List<CaseMilestoneState>, String> {

	private static final Gson gson = new Gson();

	private static final Type LIST_TYPE = new TypeToken<List<CaseMilestoneState>>() {
	}.getType();

	@Override
	public String convertToDatabaseColumn(List<CaseMilestoneState> attribute) {
		return (attribute != null) ? gson.toJson(attribute) : "[]";
	}

	@Override
	public List<CaseMilestoneState> convertToEntityAttribute(String dbData) {
		return (dbData != null && !dbData.isEmpty()) ? gson.fromJson(dbData, LIST_TYPE) : List.of();
	}

}
