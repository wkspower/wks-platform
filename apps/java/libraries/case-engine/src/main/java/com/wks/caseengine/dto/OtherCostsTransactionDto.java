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
public class OtherCostsTransactionDto {
	
	private String id;
    private String materialId;
    private Double prevBudget;
    private Double prevActual;
    private Double proposedNorm;
    private String plantId;
    private String aopYear;
    private String updatedBy;
    private String remark;
    private String normTypeName;
    private String displayName;
    private String uom;
    private String saveStatus;
    private String errDescription;
}
