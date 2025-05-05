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
public class ConsumptionNormDTO extends MonthsDTO{
	
	private String id;
    private String siteFkId;
    private String verticalFkId;
    private String aopCaseId;
    private String aopStatus;
    private String aopRemarks;
    private String materialFkId;
    private String aopYear;
    private String plantFkId;
    private String normParameterTypeDisplayName;
    private String UOM;

}
