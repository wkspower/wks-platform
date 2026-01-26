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
 * DTO for Power Balance JSON data
 * Contains power supply and demand breakdown
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PowerBalanceDTO {
    
    // Supply side
    private SupplyDTO supply;
    
    // Demand side
    private DemandDTO demand;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplyDTO {
        private Double gasEngine;        // GTs total (MWh)
        private Double steamTurbine;     // STG total (MWh)
        private Double importPower;      // MEL import (MWh)
        private Double totalSupply;      // Sum of all supply (MWh)
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemandDTO {
        private Double processDemand;    // Process demand (MWh)
        private Double u4uPower;         // U4U demand (MWh)
        private Double totalDemand;      // Sum of all demand (MWh)
    }
}
