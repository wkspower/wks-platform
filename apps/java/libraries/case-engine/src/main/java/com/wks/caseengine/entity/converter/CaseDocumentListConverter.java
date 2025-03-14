package com.wks.caseengine.entity.converter;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.cases.instance.CaseDocument;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false) 
public class CaseDocumentListConverter implements AttributeConverter<List<CaseDocument>, String> {
	
    private static final Gson gson = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<CaseDocument>>() {}.getType();

    @Override
    public String convertToDatabaseColumn(List<CaseDocument> attribute) {
        return (attribute != null) ? gson.toJson(attribute) : "[]";
    }

    @Override
    public List<CaseDocument> convertToEntityAttribute(String dbData) {
        return (dbData != null && !dbData.isEmpty()) ? gson.fromJson(dbData, LIST_TYPE) : List.of();
    }
    
}
