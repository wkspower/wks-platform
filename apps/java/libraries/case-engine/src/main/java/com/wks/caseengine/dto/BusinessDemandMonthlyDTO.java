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
public class BusinessDemandMonthlyDTO {
	
	private String id;
    private String normParameterFKId;
    private String jan;
    private String feb;
    private String mar;
    private String apr;
    private String may;
    private String jun;
    private String jul;
    private String aug;
    private String sep;
    private String oct;
    private String nov;
    private String dec;
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
	private String productName;
    private String saveStatus;
    private String errDescription;
    private String type;
   // private String normParameterDisplayName;

}
