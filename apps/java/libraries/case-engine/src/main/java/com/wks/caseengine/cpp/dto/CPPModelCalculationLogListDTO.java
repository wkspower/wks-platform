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
 * DTO for parent execution list view
 * Shows summary information for each full year execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CPPModelCalculationLogListDTO {
    
    private UUID id;
    private Integer financialYear;
    private Date executionDateTime;
    private String status;
    private Integer totalIterations;
    private Integer totalMonthsProcessed;
    private String totalExecutionTime;
    
    // Additional summary fields
    private Long monthsSucceeded;
    private Long monthsFailed;
    private Long monthsWithWarnings;
}
