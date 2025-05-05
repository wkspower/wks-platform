package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CatalystAttributesDTO extends MonthsDTO{
	
    private String remarks;
    private String catalystAttributeFKId;
    private String attributeName;
    private String year;
    private Float TPH;
    private Float avgTPH;
    private String normParameterFKId;
}