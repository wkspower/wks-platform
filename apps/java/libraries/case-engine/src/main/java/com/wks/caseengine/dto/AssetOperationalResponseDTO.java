package com.wks.caseengine.dto;

import java.util.Map;
import java.util.UUID;

import lombok.Data;

@Data
public class AssetOperationalResponseDTO {

    private String assetName;
    private UUID assetId;
   // private Map<String, MonthlyHoursDTO> months;
    private MonthlyHoursDTO april;
    private MonthlyHoursDTO may; 
    private MonthlyHoursDTO june;
    private MonthlyHoursDTO july;
    private MonthlyHoursDTO aug;
    private MonthlyHoursDTO sep;
    private MonthlyHoursDTO oct;
    private MonthlyHoursDTO nov;
    private MonthlyHoursDTO dec;
    private MonthlyHoursDTO jan;
    private MonthlyHoursDTO feb;
    private MonthlyHoursDTO march;

    // public AssetOperationalResponseDTO(String assetName,
    //                                    Map<String, MonthlyHoursDTO> months) {
    //     this.assetName = assetName;
    //     this.months = months;
    // }

    public String getAssetName() {
        return assetName;
    }

    // public Map<String, MonthlyHoursDTO> getMonths() {
    //     return months;
    // }
}
