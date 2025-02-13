package com.wks.caseengine.rest.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ListToStringConverter implements AttributeConverter<List<String>, String> {
	private static final String SPLIT_CHAR = ",";

	@Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().collect(Collectors.joining(SPLIT_CHAR));
    }
	
	@Override
    public List<String> convertToEntityAttribute(String joined) {
        if (joined == null || joined.isEmpty()) {
            return null;
        }
        return Arrays.asList(joined.split(SPLIT_CHAR));
    }
}
