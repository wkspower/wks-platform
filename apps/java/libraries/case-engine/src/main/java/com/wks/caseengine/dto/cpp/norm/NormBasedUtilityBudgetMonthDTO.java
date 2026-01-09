package com.wks.caseengine.dto.cpp.norm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class NormBasedUtilityBudgetMonthDTO {

    @JsonProperty("norms")
    private Double norms;

    @JsonProperty("quantity")
    private Double quantity;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("financialYearMonthFkId")
    private String financialYearMonthFkId;

    @JsonProperty("QTY")
    private Double qty;

    @JsonProperty("generationUom")
    private String generationUom;

    @JsonProperty("remarks")
    private String remarks;
}