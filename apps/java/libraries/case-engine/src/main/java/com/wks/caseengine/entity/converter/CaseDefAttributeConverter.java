package com.wks.caseengine.entity.converter;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.cases.instance.CaseAttribute;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CaseDefAttributeConverter implements AttributeConverter<List<CaseAttribute>, String> {
	
    private static final Gson gson = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<CaseAttribute>>() {}.getType();

    @Override
    public String convertToDatabaseColumn(List<CaseAttribute> attributeList) {
        return (attributeList != null) ? gson.toJson(attributeList) : "[]"; 
    }

    @Override
    public List<CaseAttribute> convertToEntityAttribute(String dbData) {
        return (dbData != null && !dbData.isEmpty()) ? gson.fromJson(dbData, LIST_TYPE) : List.of();
    }
    
}
