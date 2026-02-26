package com.wks.caseengine.cpp.dto.norm;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CPPNormsRequestDTO {

    @JsonProperty("cppNormsId")
    private UUID cppNormsId;

    @JsonProperty("normsHeaderFkId")
    private UUID normsHeaderFkId;

    private String aopYear;

    @JsonProperty("normTypeFkId")
    private Integer normTypeFkId;

    private BigDecimal aprNorms;
    private BigDecimal mayNorms;
    private BigDecimal junNorms;
    private BigDecimal julNorms;
    private BigDecimal augNorms;
    private BigDecimal sepNorms;
    private BigDecimal octNorms;
    private BigDecimal novNorms;
    private BigDecimal decNorms;
    private BigDecimal janNorms;
    private BigDecimal febNorms;
    private BigDecimal marNorms;

    private String remarks;
    private Boolean applyActualNormToAll;
    
    private String generatingPlantName;
    private String utilityName;
    private String utilityId;
    private String uom;
    private String accountName;
    private String materialName;
    private String materialId;
    private String issuingPlantName;
    private String issuingUom;
}
