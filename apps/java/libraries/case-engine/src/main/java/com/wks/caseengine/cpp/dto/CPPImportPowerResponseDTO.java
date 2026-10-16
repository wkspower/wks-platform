package com.wks.caseengine.cpp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CPPImportPowerResponseDTO {

    private UUID id;
    private String procurementPlant;
    private String plantName;
    private String utilityCode;
    private String utilityName;
    private String materialCode;
    private String materialName;
    private String utility;
    private String material;
    private String materialDisplayName;
    private String uom;

    private BigDecimal apr;
    private BigDecimal may;
    private BigDecimal jun;
    private BigDecimal jul;
    private BigDecimal aug;
    private BigDecimal sep;
    private BigDecimal oct;
    private BigDecimal nov;
    private BigDecimal dec;
    private BigDecimal jan;
    private BigDecimal feb;
    private BigDecimal mar;

    private String aopYear;
    private UUID siteFkId;
    private UUID verticalFkId;
    private String remarks;
    private String createdDate;
    private String updatedDate;
    private UUID importPlantFkId;
    private UUID cppPlantFkId;
    private UUID normParameterFkId;
    private UUID utilityNormParameterFkId;
}
