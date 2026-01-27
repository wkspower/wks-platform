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
package com.wks.caseengine.cpp.repository;

import com.wks.caseengine.cpp.entity.CPPModelCalculationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CPP Model Calculation Logs
 */
@Repository
public interface CPPModelCalculationLogRepository extends JpaRepository<CPPModelCalculationLog, UUID> {

    /**
     * Find all parent execution records (full year runs)
     * Parent records have parentExecutionFkId = NULL
     */
    @Query("SELECT l FROM CPPModelCalculationLog l WHERE l.parentExecutionFkId IS NULL ORDER BY l.executionDateTime DESC")
    List<CPPModelCalculationLog> findAllParentExecutions();

    /**
     * Find parent executions by financial year
     */
    @Query("SELECT l FROM CPPModelCalculationLog l WHERE l.parentExecutionFkId IS NULL AND l.financialYear = :financialYear ORDER BY l.executionDateTime DESC")
    List<CPPModelCalculationLog> findParentExecutionsByFinancialYear(@Param("financialYear") Integer financialYear);

    /**
     * Find parent executions by status
     */
    @Query("SELECT l FROM CPPModelCalculationLog l WHERE l.parentExecutionFkId IS NULL AND l.status = :status ORDER BY l.executionDateTime DESC")
    List<CPPModelCalculationLog> findParentExecutionsByStatus(@Param("status") String status);

    /**
     * Find all monthly child records for a parent execution
     */
    @Query("SELECT l FROM CPPModelCalculationLog l WHERE l.parentExecutionFkId = :parentId ORDER BY l.month ASC")
    List<CPPModelCalculationLog> findMonthlyLogsByParentId(@Param("parentId") UUID parentId);

    /**
     * Find specific monthly log by parent ID and month
     */
    @Query("SELECT l FROM CPPModelCalculationLog l WHERE l.parentExecutionFkId = :parentId AND l.month = :month")
    Optional<CPPModelCalculationLog> findMonthlyLogByParentIdAndMonth(
        @Param("parentId") UUID parentId, 
        @Param("month") Integer month
    );

    /**
     * Find parent execution by ID (with null check for parentExecutionFkId)
     */
    @Query("SELECT l FROM CPPModelCalculationLog l WHERE l.id = :id AND l.parentExecutionFkId IS NULL")
    Optional<CPPModelCalculationLog> findParentExecutionById(@Param("id") UUID id);

    /**
     * Check if parent execution exists
     */
    @Query("SELECT COUNT(l) > 0 FROM CPPModelCalculationLog l WHERE l.id = :id AND l.parentExecutionFkId IS NULL")
    boolean existsParentExecutionById(@Param("id") UUID id);

    /**
     * Count monthly logs for a parent execution
     */
    @Query("SELECT COUNT(l) FROM CPPModelCalculationLog l WHERE l.parentExecutionFkId = :parentId")
    long countMonthlyLogsByParentId(@Param("parentId") UUID parentId);

    /**
     * Get latest parent execution
     */
    @Query("SELECT l FROM CPPModelCalculationLog l WHERE l.parentExecutionFkId IS NULL ORDER BY l.executionDateTime DESC LIMIT 1")
    Optional<CPPModelCalculationLog> findLatestParentExecution();

    /**
     * Find parent executions with filters (financial year, status)
     */
    @Query("SELECT l FROM CPPModelCalculationLog l WHERE l.parentExecutionFkId IS NULL " +
           "AND (:financialYear IS NULL OR l.financialYear = :financialYear) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "ORDER BY l.executionDateTime DESC")
    List<CPPModelCalculationLog> findParentExecutionsWithFilters(
        @Param("financialYear") Integer financialYear,
        @Param("status") String status
    );
}
