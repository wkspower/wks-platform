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
        log.debug("Getting all parent executions");
        List<CPPModelCalculationLogListDTO> executions = service.getAllParentExecutions();
        return ResponseEntity.ok(executions);
    }

    /**
     * Get parent executions with filters
     */
    @GetMapping("/cpp-model-logs/search")
    public ResponseEntity<List<CPPModelCalculationLogListDTO>> getParentExecutionsWithFilters(
            @RequestParam(required = false) Integer financialYear,
            @RequestParam(required = false) String status) {
        
        log.debug("Searching parent executions with financialYear={}, status={}", financialYear, status);
        List<CPPModelCalculationLogListDTO> executions = service.getParentExecutionsWithFilters(
                financialYear, status);
        return ResponseEntity.ok(executions);
    }

    /**
     * Get parent execution by ID
     */
    @GetMapping("/cpp-model-logs/{parentId}")
    public ResponseEntity<CPPModelCalculationLogListDTO> getParentExecutionById(
            @PathVariable UUID parentId) {
        
        log.debug("Getting parent execution with id={}", parentId);
        return service.getParentExecutionById(parentId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Parent execution not found with id: " + parentId));
    }

    /**
     * Get monthly logs for a parent execution (all 12 months)
     */
    @GetMapping("/cpp-model-logs/{parentId}/months")
    public ResponseEntity<List<MonthlyLogDTO>> getMonthlyLogsByParentId(
            @PathVariable UUID parentId) {
        
        log.debug("Getting monthly logs for parent id={}", parentId);
        
        if (!service.parentExecutionExists(parentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    "Parent execution not found with id: " + parentId);
        }
        
        List<MonthlyLogDTO> monthlyLogs = service.getMonthlyLogsByParentId(parentId);
        return ResponseEntity.ok(monthlyLogs);
    }

    /**
     * Get detailed monthly log with parsed JSON
     */
    @GetMapping("/cpp-model-logs/month/{logId}")
    public ResponseEntity<MonthlyLogDetailDTO> getMonthlyLogDetail(
            @PathVariable UUID logId) {
        
        log.debug("Getting monthly log detail for id={}", logId);
        return service.getMonthlyLogDetail(logId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Monthly log not found with id: " + logId));
    }

    /**
     * Get monthly log by parent ID and month number (1-12)
     */
    @GetMapping("/cpp-model-logs/{parentId}/month/{month}")
    public ResponseEntity<MonthlyLogDetailDTO> getMonthlyLogByParentAndMonth(
            @PathVariable UUID parentId,
            @PathVariable Integer month) {
        
        log.debug("Getting monthly log for parent={}, month={}", parentId, month);
        
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Month must be between 1 and 12");
        }
        
        return service.getMonthlyLogByParentAndMonth(parentId, month)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Monthly log not found for parent: " + parentId + ", month: " + month));
    }

    /**
     * Get latest parent execution
     */
    @GetMapping("/cpp-model-logs/latest")
    public ResponseEntity<CPPModelCalculationLogListDTO> getLatestParentExecution() {
        log.debug("Getting latest parent execution");
        return service.getLatestParentExecution()
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "No parent executions found"));
    }
}
