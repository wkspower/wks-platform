package com.wks.caseengine.cpp.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CalculatedProcessDemandDTO {

    private UUID id;
    private String financialYear;
    private String processPlant;
    private String processPlantId;
    private String cppUtility;
    private String cppUtilityId;
    private String cppPlant;
    private String cppPlantId;
    private String uom;
    private Double apr;
    private Double may;
    private Double jun;
    private Double jul;
    private Double aug;
    private Double sep;
    private Double oct;
    private Double nov;
    private Double dec;
    private Double jan;
    private Double feb;
    private Double mar;
    private Boolean isCalculated;
    private String remarks;

}
