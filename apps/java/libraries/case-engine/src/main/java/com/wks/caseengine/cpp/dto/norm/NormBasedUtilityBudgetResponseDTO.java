package com.wks.caseengine.cpp.dto.norm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class NormBasedUtilityBudgetResponseDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("generatingPlantName")
    private String generatingPlantName;

    @JsonProperty("utilityName")
    private String utilityName;

    @JsonProperty("utilityId")
    private String utilityId;

    @JsonProperty("uom")
    private String uom;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("materialName")
    private String materialName;

    @JsonProperty("materialId")
    private String materialId;

    @JsonProperty("issuingPlantName")
    private String issuingPlantName;

    @JsonProperty("issuingUom")
    private String issuingUom;

    @JsonProperty("generationUom")
    private String generationUom;

    @JsonProperty("normHeaderId")
    private String normHeaderId;

    @JsonProperty("apr")
    private NormBasedUtilityBudgetMonthDTO apr;

    @JsonProperty("may")
    private NormBasedUtilityBudgetMonthDTO may;

    @JsonProperty("jun")
    private NormBasedUtilityBudgetMonthDTO jun;

    @JsonProperty("jul")
    private NormBasedUtilityBudgetMonthDTO jul;

    @JsonProperty("aug")
    private NormBasedUtilityBudgetMonthDTO aug;

    @JsonProperty("sep")
    private NormBasedUtilityBudgetMonthDTO sep;

    @JsonProperty("oct")
    private NormBasedUtilityBudgetMonthDTO oct;

    @JsonProperty("nov")
    private NormBasedUtilityBudgetMonthDTO nov;

    @JsonProperty("dec")
    private NormBasedUtilityBudgetMonthDTO dec;

    @JsonProperty("jan")
    private NormBasedUtilityBudgetMonthDTO jan;

    @JsonProperty("feb")
    private NormBasedUtilityBudgetMonthDTO feb;

    @JsonProperty("mar")
    private NormBasedUtilityBudgetMonthDTO mar;

    @JsonProperty("remarks")
    private String remarks;
}


