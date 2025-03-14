package com.wks.caseengine.entity.converter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.google.gson.Gson;
import com.wks.caseengine.cases.instance.CaseOwner;

@Converter(autoApply = false)
public class CaseOwnerConverter implements AttributeConverter<CaseOwner, String> {
    private static final Gson gson = new Gson();

    @Override
    public String convertToDatabaseColumn(CaseOwner attribute) {
        return (attribute != null) ? gson.toJson(attribute) : null;
    }

    @Override
    public CaseOwner convertToEntityAttribute(String dbData) {
        return (dbData != null && !dbData.isEmpty()) ? gson.fromJson(dbData, CaseOwner.class) : null;
    }
}
