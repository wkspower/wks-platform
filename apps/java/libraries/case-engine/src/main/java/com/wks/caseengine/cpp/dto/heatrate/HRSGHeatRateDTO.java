package com.wks.caseengine.cpp.dto.heatrate;

import java.util.UUID;

import lombok.Data;

@Data
public class HRSGHeatRateDTO {
    
    private UUID id;
    private String equipType;  // AssetName (HRSG1, HRSG2, HRSG3)
    private String cppUtility;  // UtilityId
    private Double hrsgLoad;  // HRSGLoad in MT/hr
    private Double heatRate;  // DisplayedAvgHeatRate
    private String remarks;
    private Double previousYearHeatRate;
    private Double finalHeatRate;
    private Double oemHeatRate;
    private String selectedHeatRate;  // OEM, PREVIOUS_YEAR, PROPOSED, OTHER
    private Double proposedHeatRate;  // Calculated from date range using SP
}
