package com.wks.caseengine.jpa.entity.converter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Converter(autoApply = true)
public class JsonConverter implements AttributeConverter<JsonObject, String> {
    private final Gson gson = new Gson();

    @Override
    public String convertToDatabaseColumn(JsonObject attribute) {
        return (attribute != null) ? gson.toJson(attribute) : null;
    }

    @Override
    public JsonObject convertToEntityAttribute(String dbData) {
        return (dbData != null && !dbData.isEmpty()) ? gson.fromJson(dbData, JsonObject.class) : null;
    }
}
