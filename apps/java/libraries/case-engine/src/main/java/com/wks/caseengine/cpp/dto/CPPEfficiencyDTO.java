package com.wks.caseengine.cpp.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CPPEfficiencyDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("cppPlantFkId")
    private UUID cppPlantFkId;

    @JsonProperty("assetFkId")
    private UUID assetFkId;

    @JsonProperty("assetName")
    private String assetName;

    @JsonProperty("type")
    private String type;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("uom")
    private String uom;

    @JsonProperty("apr")
    private Double apr;
    @JsonProperty("may")
    private Double may;
    @JsonProperty("jun")
    private Double jun;
    @JsonProperty("jul")
    private Double jul;
    @JsonProperty("aug")
    private Double aug;
    @JsonProperty("sep")
    private Double sep;
    @JsonProperty("oct")
    private Double oct;
    @JsonProperty("nov")
    private Double nov;
    @JsonProperty("dec")
    private Double dec;
    @JsonProperty("jan")
    private Double jan;
    @JsonProperty("feb")
    private Double feb;
    @JsonProperty("mar")
    private Double mar;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("aopYear")
    private String aopYear;

    @JsonProperty("rowHash")
    private String rowHash;
}
