/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.cpp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Asset Status JSON data
 * Contains loading information for all assets
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetStatusDTO {
    
    private List<AssetLoadingDTO> assets;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetLoadingDTO {
        private String assetName;
        private String assetType;  // GT, HRSG, STG, ImportPower
        private Integer priority;  // Dispatch priority (lower = higher priority)
        private Boolean isAvailable;
        private Double operatingHours;  // Working hours
        
        // For GT/STG/ImportPower assets
        private Double minCapacityMW;
        private Double maxCapacityMW;
        private Double dispatchedLoadMW;  // Dispatched load in MW
        private Double avgLoadingMW;  // Average loading during operation
        private Double grossMWh;
        private Double netMWh;
        private Double auxiliaryMWh;
        
        // For HRSG assets
        private Double steamGenerationMT;  // Total supplementary firing steam (MT)
        private Double freeSteamMT;  // Free steam from GT exhaust (MT)
        private Double avgSteamGenRateMTPerHr;  // Average generation rate (MT/hour)
        
        private String status;  // OFF, AtMin, AtMax, PARTIAL, Running, etc.
        
        // Legacy fields for backward compatibility
        private Double loading;  // Deprecated: use dispatchedLoadMW or steamGenerationMT
        private String unit;  // Deprecated: use specific field names
    }
}
