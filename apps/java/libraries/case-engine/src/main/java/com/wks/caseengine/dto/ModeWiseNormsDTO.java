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
public class ModeWiseNormsDTO {
	
	private String id;
	private String plantFKId;
	private String normParameterTypeId;
	private String materialFKId;
	private String normType;
	private String materialName;
    private Double april;
    private Double may;
    private Double june;
    private Double july;
    private Double august;
    private Double september;
    private Double october;
    private Double november;
    private Double december;
    private Double january;
    private Double february;
    private Double march;
    private String financialYear;
    private String remark;
    private Integer displayOrder;
    private Boolean isEditable;
    private Boolean isChecked;
    private String materialDisplayName;
    private String uom;
    private String sapMaterialCode;
   
    
}
