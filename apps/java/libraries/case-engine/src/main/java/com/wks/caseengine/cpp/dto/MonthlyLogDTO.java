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
 * DTO for monthly log summary (child record)
 * Shows in the list of 12 months under a parent execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyLogDTO {
    
    private UUID id;
    private UUID parentExecutionFkId;
    private Integer financialYear;
    private Integer month;
    private Date executionDateTime;
    private String status;
    private Integer iterations;
    private String executionTime;
    private String convergenceStatus;
    
    // Quick indicators
    private boolean hasAssetStatus;
    private boolean hasPowerBalance;
    private boolean hasSteamBalance;
}
