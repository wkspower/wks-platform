package com.wks.caseengine.rest.model;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AttributesConverter implements AttributeConverter<List<Attribute>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Attribute> attributes) {
        try {
            return objectMapper.writeValueAsString(attributes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert attributes to JSON", e);
        }
    }

    @Override
    public List<Attribute> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, objectMapper.getTypeFactory().constructCollectionType(List.class, Attribute.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to attributes", e);
        }
    }
}
