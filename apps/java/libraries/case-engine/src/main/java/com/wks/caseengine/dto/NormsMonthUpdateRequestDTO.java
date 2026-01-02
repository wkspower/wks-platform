package com.wks.caseengine.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class NormsMonthUpdateRequestDTO {


    @JsonProperty("normsHeaderFkId") 
    @JsonAlias({ "normHeader_FK_Id", "normHeaderFkId", "normsHeaderFkId" })
    private UUID normsHeaderFkId;

    private NormsMonthValueDTO apr;
    private NormsMonthValueDTO may;
    private NormsMonthValueDTO jun;
    private NormsMonthValueDTO jul;
    private NormsMonthValueDTO aug;
    private NormsMonthValueDTO sep;
    private NormsMonthValueDTO oct;
    private NormsMonthValueDTO nov;
    private NormsMonthValueDTO dec;
    private NormsMonthValueDTO jan;
    private NormsMonthValueDTO feb;
    private NormsMonthValueDTO mar;
    private String generatingPlantName;
    private String utilityName;
    private String utilityId;
    private String uom;
    private String accountName;
    private String materialName;
    private String issuingPlantName;
    private String issuingUom;
    private Boolean inEdit;
    private String remarks;
}
