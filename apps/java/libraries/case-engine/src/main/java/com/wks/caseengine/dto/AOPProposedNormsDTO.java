package com.wks.caseengine.dto;

import java.util.Date;

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
public class AOPProposedNormsDTO {
	
	private String id;
    private String normParameterTypeDisplayName;
    private String normParameterDisplayName;
    private String UOM;
    private Double prevYearBudgetApril;
    private Double prevYearBudgetMay;
    private Double prevYearBudgetJune;
    private Double prevYearBudgetJuly;
    private Double prevYearBudgetAugust;
    private Double prevYearBudgetSeptember;
    private Double prevYearBudgetOctober;
    private Double prevYearBudgetNovember;
    private Double prevYearBudgetDecember;
    private Double prevYearBudgetJanuary;
    private Double prevYearBudgetFebruary;
    private Double prevYearBudgetMarch;
    private Double currYearBudgetApril;
    private Double currYearBudgetMay;
    private Double currYearBudgetJune;	
    private Double currYearBudgetJuly;	
    private Double currYearBudgetAugust;	
    private Double currYearBudgetSeptember;	
    private Double currYearBudgetOctober;
    private Double currYearBudgetNovember;	
    private Double currYearBudgetDecember;
    private Double currYearBudgetJanuary;
    private Double currYearBudgetFebruary;	
    private Double currYearBudgetMarch;	
    private Double currYearProposedApril;	
    private Double currYearProposedMay;
    private Double currYearProposedJune;
    private Double currYearProposedJuly;	
    private Double currYearProposedAugust;
    private Double currYearProposedSeptember;
    private Double currYearProposedOctober;
    private Double currYearProposedNovember;
    private Double currYearProposedDecember;
    private Double currYearProposedJanuary;
    private Double currYearProposedFebruary; 
    private Double	currYearProposedMarch;
    private String remarks;
    private String gradeId;
    private String plantId; 
    private String	aopYear;
    private String modifiedBy;
    private Date modifiedOn;
    private Boolean isEditable;
}
