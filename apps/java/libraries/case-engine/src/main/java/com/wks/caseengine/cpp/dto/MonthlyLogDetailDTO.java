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

import java.util.Date;
import java.util.UUID;

/**
 * DTO for detailed monthly log view
 * Includes parsed JSON data for assets, power, and steam
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyLogDetailDTO {
    
    private UUID id;
    private UUID parentExecutionFkId;
    private Integer financialYear;
    private Integer month;
    private Date executionDateTime;
    private String status;
    private Integer iterations;
    private String executionTime;
    private String convergenceStatus;
    
    // Parsed JSON data
    private AssetStatusDTO assetStatus;
    private PowerBalanceDTO powerBalance;
    private SteamBalanceDTO steamBalance;
}
