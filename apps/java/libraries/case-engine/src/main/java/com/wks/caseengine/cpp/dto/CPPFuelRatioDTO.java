package com.wks.caseengine.cpp.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CPPFuelRatioDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("cppPlantFkId")
    private UUID cppPlantFkId;

    @JsonProperty("fuelFkId")
    private UUID fuelFkId;

    @JsonProperty("fuelName")
    private String fuelName;

    @JsonProperty("gcvApr")
    private Double gcvApr;
    @JsonProperty("gcvMay")
    private Double gcvMay;
    @JsonProperty("gcvJun")
    private Double gcvJun;
    @JsonProperty("gcvJul")
    private Double gcvJul;
    @JsonProperty("gcvAug")
    private Double gcvAug;
    @JsonProperty("gcvSep")
    private Double gcvSep;
    @JsonProperty("gcvOct")
    private Double gcvOct;
    @JsonProperty("gcvNov")
    private Double gcvNov;
    @JsonProperty("gcvDec")
    private Double gcvDec;
    @JsonProperty("gcvJan")
    private Double gcvJan;
    @JsonProperty("gcvFeb")
    private Double gcvFeb;
    @JsonProperty("gcvMar")
    private Double gcvMar;

    @JsonProperty("percentageByWtApr")
    private Double percentageByWtApr;
    @JsonProperty("percentageByWtMay")
    private Double percentageByWtMay;
    @JsonProperty("percentageByWtJun")
    private Double percentageByWtJun;
    @JsonProperty("percentageByWtJul")
    private Double percentageByWtJul;
    @JsonProperty("percentageByWtAug")
    private Double percentageByWtAug;
    @JsonProperty("percentageByWtSep")
    private Double percentageByWtSep;
    @JsonProperty("percentageByWtOct")
    private Double percentageByWtOct;
    @JsonProperty("percentageByWtNov")
    private Double percentageByWtNov;
    @JsonProperty("percentageByWtDec")
    private Double percentageByWtDec;
    @JsonProperty("percentageByWtJan")
    private Double percentageByWtJan;
    @JsonProperty("percentageByWtFeb")
    private Double percentageByWtFeb;
    @JsonProperty("percentageByWtMar")
    private Double percentageByWtMar;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("aopYear")
    private String aopYear;

    @JsonProperty("rowHash")
    private String rowHash;
}
