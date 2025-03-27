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
    private Float april;
    private Float may;
    private Float june;
    private Float july;
    private Float august;
    private Float september;
    private Float october;
    private Float november;
    private Float december;
    private Float january;
    private Float february;
    private Float march;
    private String financialYear;
    private String remarks;
    private Date createdOn;
    private Date modifiedOn;
    private String mcuVersion;
    private String updatedBy;
    private String normParameterTypeId;
    private String normParameterTypeName;
    private String normParameterTypeDisplayName;
}

