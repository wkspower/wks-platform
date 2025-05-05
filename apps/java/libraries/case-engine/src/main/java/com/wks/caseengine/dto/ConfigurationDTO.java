package com.wks.caseengine.dto;

import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConfigurationDTO extends MonthsDTO{
	
	private String id;
    private String normParameterFKId;
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
