package com.wks.caseengine.cpp.dto.heatrate;

import java.util.UUID;

import lombok.Data;

@Data
public class HeatRateDTO {
    

    private UUID id;
    private String equipType;
    private String cppUtility;
    private Double gtLoad;
    private Double heatRate;
    private Double freeSteamFactor;
    private String remarks;
    private Double previousYearHeatRate;
    private Double finalHeatRate;
    private Double oemHeatRate;
    private String selectedHeatRate;
}


