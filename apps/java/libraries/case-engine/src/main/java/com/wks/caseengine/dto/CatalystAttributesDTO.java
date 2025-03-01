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
@Builder
@Data
public class CatalystAttributesDTO {
	
    private String remarks;
    private String catalystAttributeFKId;
    private String attributeName;
    private Float jan;
    private Float feb;
    private Float march;
    private Float april;
    private Float may;
    private Float june;
    private Float july;
    private Float aug;
    private Float sep;
    private Float oct;
    private Float nov;
    private Float dec;
    private String year;
    private Float TPH;
    private Float avgTPH;
    private String normParameterFKId;
}