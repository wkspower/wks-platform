package com.wks.caseengine.entity.converter;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.cases.instance.CaseComment;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false) 
public class CaseCommentListConverter implements AttributeConverter<List<CaseComment>, String> {
	
    private static final Gson gson = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<CaseComment>>() {}.getType();

    @Override
    public String convertToDatabaseColumn(List<CaseComment> attribute) {
        return (attribute != null) ? gson.toJson(attribute) : "[]";
    }

    @Override
    public List<CaseComment> convertToEntityAttribute(String dbData) {
        return (dbData != null && !dbData.isEmpty()) ? gson.fromJson(dbData, LIST_TYPE) : List.of();
    }
    
}
