package com.wks.caseengine.cpp.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for the Spinning Margin Maintained report.
 *
 * <p>Maps rows from {@code dbo.CPPMonthlySpinningMarginMaintained} joined with
 * the Plants table to resolve the plant name. The table stores monthly
 * spinning margin values per MarginType (POWER / STEAM) and Plant.</p>
 */
@Data
public class SpinningMarginReportDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("aopYear")
    private String aopYear;

    @JsonProperty("marginType")
    private String marginType;

    @JsonProperty("plantFkId")
    private UUID plantFkId;

    @JsonProperty("plantName")
    private String plantName;

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
}
