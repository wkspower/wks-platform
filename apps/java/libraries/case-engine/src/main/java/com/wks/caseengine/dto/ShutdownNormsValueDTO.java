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
public class ShutdownNormsValueDTO {
    private String id;
    private String siteFkId;
    private String plantFkId;
    private String verticalFkId;
    private String materialFkId;
    private String normParameterTypeFkId;
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
    private String remarks;
    private Date createdOn;
    private Date modifiedOn;
    private String mcuVersion;
    private String updatedBy;
    private String normParameterTypeId;
    private String normParameterTypeName;
    private String normParameterTypeDisplayName;
    private String UOM;
    private String aOPCaseId;
    private String aOPStatus;
    private Boolean isEditable;
    private String productName;
}

