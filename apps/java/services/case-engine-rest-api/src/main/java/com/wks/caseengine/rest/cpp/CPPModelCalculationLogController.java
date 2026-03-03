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
package com.wks.caseengine.rest.cpp;

import com.wks.caseengine.cpp.dto.CPPModelCalculationLogListDTO;
import com.wks.caseengine.cpp.dto.MonthlyLogDTO;
import com.wks.caseengine.cpp.dto.MonthlyLogDetailDTO;
import com.wks.caseengine.cpp.service.CPPModelCalculationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for CPP Model Calculation Logs
 * Provides endpoints for viewing execution logs, monthly details, and parsed JSON data
 */
@RestController
@RequestMapping("task")
@RequiredArgsConstructor
@Slf4j
public class CPPModelCalculationLogController {

    private final CPPModelCalculationLogService service;

    /**
     * Get all parent executions (full year runs)
     */
    @GetMapping("/cpp-model-logs")
    public ResponseEntity<List<CPPModelCalculationLogListDTO>> getAllParentExecutions() {
        log.info("[CPPModelCalculationLogController] Received request to get all parent executions");
        try {
            List<CPPModelCalculationLogListDTO> executions = service.getAllParentExecutions();
            log.info("[CPPModelCalculationLogController] Successfully retrieved {} parent executions", executions.size());
            return ResponseEntity.ok(executions);
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogController] Error retrieving all parent executions: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get parent executions with filters
     */
    @GetMapping("/cpp-model-logs/search")
    public ResponseEntity<List<CPPModelCalculationLogListDTO>> getParentExecutionsWithFilters(
            @RequestParam(required = false) Integer financialYear,
            @RequestParam(required = false) String status) {
        
        log.info("[CPPModelCalculationLogController] Received request to search parent executions with filters - financialYear: {}, status: {}", financialYear, status);
        try {
            List<CPPModelCalculationLogListDTO> executions = service.getParentExecutionsWithFilters(
                    financialYear, status);
            log.info("[CPPModelCalculationLogController] Successfully retrieved {} parent executions matching filters", executions.size());
            return ResponseEntity.ok(executions);
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogController] Error searching parent executions with financialYear: {}, status: {} - Error: {}", 
                    financialYear, status, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get parent execution by ID
     */
    @GetMapping("/cpp-model-logs/{parentId}")
    public ResponseEntity<CPPModelCalculationLogListDTO> getParentExecutionById(
            @PathVariable UUID parentId) {
        
        log.info("[CPPModelCalculationLogController] Received request to get parent execution by ID: {}", parentId);
        try {
            return service.getParentExecutionById(parentId)
                    .map(execution -> {
                        log.info("[CPPModelCalculationLogController] Successfully retrieved parent execution with ID: {}", parentId);
                        return ResponseEntity.ok(execution);
                    })
                    .orElseThrow(() -> {
                        log.warn("[CPPModelCalculationLogController] Parent execution not found with ID: {}", parentId);
                        return new ResponseStatusException(
                                HttpStatus.NOT_FOUND, 
                                "Parent execution not found with id: " + parentId);
                    });
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogController] Error retrieving parent execution by ID: {} - Error: {}", parentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get monthly logs for a parent execution (all 12 months)
     */
    @GetMapping("/cpp-model-logs/{parentId}/months")
    public ResponseEntity<List<MonthlyLogDTO>> getMonthlyLogsByParentId(
            @PathVariable UUID parentId) {
        
        log.info("[CPPModelCalculationLogController] Received request to get monthly logs for parent ID: {}", parentId);
        
        try {
            if (!service.parentExecutionExists(parentId)) {
                log.warn("[CPPModelCalculationLogController] Parent execution not found with ID: {}", parentId);
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Parent execution not found with id: " + parentId);
            }
            
            List<MonthlyLogDTO> monthlyLogs = service.getMonthlyLogsByParentId(parentId);
            log.info("[CPPModelCalculationLogController] Successfully retrieved {} monthly logs for parent ID: {}", monthlyLogs.size(), parentId);
            return ResponseEntity.ok(monthlyLogs);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogController] Error retrieving monthly logs for parent ID: {} - Error: {}", parentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get detailed monthly log with parsed JSON
     */
    @GetMapping("/cpp-model-logs/month/{logId}")
    public ResponseEntity<MonthlyLogDetailDTO> getMonthlyLogDetail(
            @PathVariable UUID logId) {
        
        log.info("[CPPModelCalculationLogController] Received request to get monthly log detail for ID: {}", logId);
        try {
            return service.getMonthlyLogDetail(logId)
                    .map(detail -> {
                        log.info("[CPPModelCalculationLogController] Successfully retrieved monthly log detail for ID: {}", logId);
                        return ResponseEntity.ok(detail);
                    })
                    .orElseThrow(() -> {
                        log.warn("[CPPModelCalculationLogController] Monthly log not found with ID: {}", logId);
                        return new ResponseStatusException(
                                HttpStatus.NOT_FOUND, 
                                "Monthly log not found with id: " + logId);
                    });
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogController] Error retrieving monthly log detail for ID: {} - Error: {}", logId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get monthly log by parent ID and month number (1-12)
     */
    @GetMapping("/cpp-model-logs/{parentId}/month/{month}")
    public ResponseEntity<MonthlyLogDetailDTO> getMonthlyLogByParentAndMonth(
            @PathVariable UUID parentId,
            @PathVariable Integer month) {
        
        log.info("[CPPModelCalculationLogController] Received request to get monthly log for parent ID: {}, month: {}", parentId, month);
        
        if (month < 1 || month > 12) {
            log.warn("[CPPModelCalculationLogController] Invalid month value: {}. Month must be between 1 and 12", month);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Month must be between 1 and 12");
        }
        
        try {
            return service.getMonthlyLogByParentAndMonth(parentId, month)
                    .map(detail -> {
                        log.info("[CPPModelCalculationLogController] Successfully retrieved monthly log for parent ID: {}, month: {}", parentId, month);
                        return ResponseEntity.ok(detail);
                    })
                    .orElseThrow(() -> {
                        log.warn("[CPPModelCalculationLogController] Monthly log not found for parent ID: {}, month: {}", parentId, month);
                        return new ResponseStatusException(
                                HttpStatus.NOT_FOUND, 
                                "Monthly log not found for parent: " + parentId + ", month: " + month);
                    });
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogController] Error retrieving monthly log for parent ID: {}, month: {} - Error: {}", 
                    parentId, month, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get latest parent execution
     */
    @GetMapping("/cpp-model-logs/latest")
    public ResponseEntity<CPPModelCalculationLogListDTO> getLatestParentExecution() {
        log.info("[CPPModelCalculationLogController] Received request to get latest parent execution");
        try {
            return service.getLatestParentExecution()
                    .map(execution -> {
                        log.info("[CPPModelCalculationLogController] Successfully retrieved latest parent execution with ID: {}", execution.getId());
                        return ResponseEntity.ok(execution);
                    })
                    .orElseThrow(() -> {
                        log.warn("[CPPModelCalculationLogController] No parent executions found in the system");
                        return new ResponseStatusException(
                                HttpStatus.NOT_FOUND, 
                                "No parent executions found");
                    });
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogController] Error retrieving latest parent execution: {}", e.getMessage(), e);
            throw e;
        }
    }
}
