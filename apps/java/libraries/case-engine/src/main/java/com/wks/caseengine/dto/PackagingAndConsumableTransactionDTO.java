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
public class PackagingAndConsumableTransactionDTO {
	
	private String id;
    private String materialId;
    private String displayName;
    private String uom;
    private Double packagingPrice;
    private Double prevBudget;
    private Double prevActual;
    private Double proposedNorm;
    private String plantId;
    private String aopYear;
    private String remark;
    private String updatedBy;
    private Date modifiedOn;
    private String saveStatus;
    private String errDescription;
    private String normParameterTypeName;
    private String sapMaterialCode;
}
