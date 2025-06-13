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
public class AOPConsumptionNormDTO {
	
	private String id;
    private String siteFkId;
    private String verticalFkId;
    private String aopCaseId;
    private String aopStatus;
    private String aopRemarks;
    private String materialFkId;
    private Double jan;
    private Double feb;
    private Double march;
    private Double april;
    private Double may;
    private Double june;
    private Double july;
    private Double aug;
    private Double sep;
    private Double oct;
    private Double nov;
    private Double dec;
    private String aopYear;
    private String plantFkId;
    private String normParameterTypeDisplayName;
    private String UOM;
    private Boolean isEditable;
    private String productName;

}
