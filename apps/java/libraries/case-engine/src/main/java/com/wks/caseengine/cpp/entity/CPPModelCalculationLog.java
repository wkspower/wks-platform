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
package com.wks.caseengine.cpp.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

/**
 * Entity representing CPP Model Calculation Execution Logs
 * Stores both parent execution records (full year runs) and child records (individual months)
 */
@Entity
@Table(name = "CPPModelCalculationLogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CPPModelCalculationLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "Id", nullable = false, updatable = false)
    private UUID id;

    /**
     * Parent execution FK - NULL for parent records, non-null for child month records
     */
    @Column(name = "ParentExecution_FK_Id")
    private UUID parentExecutionFkId;

    /**
     * Financial Year Month FK - NULL for parent records
     */
    @Column(name = "FinancialYearMonth_FK_Id")
    private UUID financialYearMonthFkId;

    /**
     * Financial Year (e.g., 2025 for FY 2025-26)
     */
    @Column(name = "FinancialYear", nullable = false)
    private Integer financialYear;

    /**
     * Month (1-12) - NULL for parent records
     */
    @Column(name = "Month")
    private Integer month;

    /**
     * Execution timestamp
     */
    @Column(name = "ExecutionDateTime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date executionDateTime;

    /**
     * Status: Success, Failed, Warning, InProgress
     */
    @Size(max = 20)
    @Column(name = "Status", length = 20, nullable = false)
    private String status;

    /**
     * Error message if status is Failed or Warning
     */
    @Column(name = "ErrorMessage", columnDefinition = "NVARCHAR(MAX)")
    private String errorMessage;

    /**
     * Error type classification
     */
    @Size(max = 100)
    @Column(name = "ErrorType", length = 100)
    private String errorType;

    /**
     * Total iteration count (for parent: sum of all months, for child: month iterations)
     */
    @Column(name = "IterationCount")
    private Integer iterationCount;

    /**
     * Convergence flag (1 = converged, 0 = not converged)
     */
    @Column(name = "ConvergenceAchieved")
    private Boolean convergenceAchieved;

    /**
     * Execution time in seconds
     */
    @Column(name = "ExecutionTimeSeconds", precision = 10, scale = 2)
    private BigDecimal executionTimeSeconds;

    /**
     * Asset status JSON (stored as String, contains array of asset dispatch data)
     * NULL for parent records
     */
    @Column(name = "AssetStatusJSON", columnDefinition = "NVARCHAR(MAX)")
    private String assetStatusJson;

    /**
     * Power balance JSON (stored as String, contains demand/supply/balance data)
     * NULL for parent records
     */
    @Column(name = "PowerBalanceJSON", columnDefinition = "NVARCHAR(MAX)")
    private String powerBalanceJson;

    /**
     * Steam balance JSON (stored as String, contains SHP/HP/MP/LP balance data)
     * NULL for parent records
     */
    @Column(name = "SteamBalanceJSON", columnDefinition = "NVARCHAR(MAX)")
    private String steamBalanceJson;

    /**
     * Created by user/system
     */
    @Size(max = 100)
    @Column(name = "CreatedBy", length = 100)
    private String createdBy;

    /**
     * Created timestamp
     */
    @Column(name = "CreatedDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    /**
     * Check if this is a parent execution record
     */
    public boolean isParentRecord() {
        return this.parentExecutionFkId == null;
    }

    /**
     * Check if this is a child monthly record
     */
    public boolean isMonthlyRecord() {
        return this.parentExecutionFkId != null;
    }
}
