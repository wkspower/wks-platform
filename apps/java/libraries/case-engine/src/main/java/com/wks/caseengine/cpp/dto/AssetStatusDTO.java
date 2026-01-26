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
        private String assetType;  // GT, HRSG, STG, MEL_IMPORT
        private Double loading;     // MWh or MW
        private String unit;        // MWh, MW
    }
}
