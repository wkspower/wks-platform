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
    private Double jan;
    private Double feb;
    private Double mar;
    private Double apr;
    private Double may;
    private Double jun;
    private Double jul;
    private Double aug;
    private Double sep;
    private Double oct;
    private Double nov;
    private Double dec;
    private String remarks;
    private String auditYear;
    private String UOM;
    private String lossCategory;
    private String normType;
    private String ConfigTypeDisplayName;
    private String TypeDisplayName;
    private String ConfigTypeName;
    private String TypeName;
    private Boolean isEditable;

}
