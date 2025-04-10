package com.wks.caseengine.utils;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ModelMapperUtils {
	
	public static <T> List<T> mapObjectArrayToDTO(List<Object[]> resultList, Class<T> dtoClass) {
        List<T> dtoList = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        System.out.println("==================>mapobjectArraycall");

        // Get all field names from DTO
        Field[] fields = dtoClass.getDeclaredFields();

        for (Object[] row : resultList) {
            Map<String, Object> fieldMap = new HashMap<>();

            for (int i = 0; i < Math.min(fields.length, row.length); i++) {
                String fieldName = fields[i].getName();
                Object value = row[i];
                fieldMap.put(fieldName, value);
            }

            // Map fieldMap -> DTO using ModelMapper
            T dto = modelMapper.map(fieldMap, dtoClass);
            dtoList.add(dto);
        }

        return dtoList;
    }

}
