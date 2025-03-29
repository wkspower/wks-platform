package com.wks.caseengine.dto;

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
public class ConfigurationDTO {
	
	private String id;
    private String normParameterFKId;
    private Float jan;
    private Float feb;
    private Float mar;
    private Float apr;
    private Float may;
    private Float jun;
    private Float jul;
    private Float aug;
    private Float sep;
    private Float oct;
    private Float nov;
    private Float dec;
    private String remarks;
    private String auditYear;
    private String UOM;

}
