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

/**
 * DTO for Steam Balance JSON data
 * Contains steam supply and demand breakdown for each steam type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SteamBalanceDTO {
    
    private SteamTypeBalanceDTO shp;  // Super High Pressure
    private SteamTypeBalanceDTO hp;   // High Pressure
    private SteamTypeBalanceDTO mp;   // Medium Pressure
    private SteamTypeBalanceDTO lp;   // Low Pressure
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SteamTypeBalanceDTO {
        private String steamType;        // SHP, HP, MP, LP
        private Double totalDemand;      // Total demand (TPH)
        private Double totalSupply;      // Total supply (TPH)
        private Double balance;          // Supply - Demand (TPH)
    }
}
