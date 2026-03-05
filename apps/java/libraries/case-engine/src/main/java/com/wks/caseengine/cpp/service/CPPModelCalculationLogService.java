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
package com.wks.caseengine.cpp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.cpp.dto.*;
import com.wks.caseengine.cpp.entity.CPPModelCalculationLog;
import com.wks.caseengine.cpp.repository.CPPModelCalculationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for CPP Model Calculation Logs
 * Handles business logic and JSON parsing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CPPModelCalculationLogService {

    private final CPPModelCalculationLogRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Get all parent executions (full year runs)
     */
    public List<CPPModelCalculationLogListDTO> getAllParentExecutions() {
        log.info("[CPPModelCalculationLogService] Fetching all parent executions from repository");
        try {
            List<CPPModelCalculationLog> parents = repository.findAllParentExecutions();
            log.info("[CPPModelCalculationLogService] Found {} parent execution records", parents.size());
            
            List<CPPModelCalculationLogListDTO> result = parents.stream()
                    .map(this::convertToListDTO)
                    .collect(Collectors.toList());
            
            log.info("[CPPModelCalculationLogService] Successfully converted {} parent executions to DTOs", result.size());
            return result;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogService] Error fetching all parent executions: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get parent executions with optional filters
     */
    public List<CPPModelCalculationLogListDTO> getParentExecutionsWithFilters(
            Integer financialYear, String status) {
        log.info("[CPPModelCalculationLogService] Fetching parent executions with filters - financialYear: {}, status: {}", financialYear, status);
        try {
            List<CPPModelCalculationLog> parents = repository.findParentExecutionsWithFilters(
                    financialYear, status);
            log.info("[CPPModelCalculationLogService] Found {} parent execution records matching filters", parents.size());
            
            List<CPPModelCalculationLogListDTO> result = parents.stream()
                    .map(this::convertToListDTO)
                    .collect(Collectors.toList());
            
            log.info("[CPPModelCalculationLogService] Successfully converted {} filtered parent executions to DTOs", result.size());
            return result;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogService] Error fetching parent executions with filters (financialYear: {}, status: {}): {}", 
                    financialYear, status, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get parent execution by ID
     */
    public Optional<CPPModelCalculationLogListDTO> getParentExecutionById(UUID id) {
        log.info("[CPPModelCalculationLogService] Fetching parent execution by ID: {}", id);
        try {
            Optional<CPPModelCalculationLog> parent = repository.findParentExecutionById(id);
            
            if (parent.isPresent()) {
                log.info("[CPPModelCalculationLogService] Found parent execution with ID: {}", id);
                CPPModelCalculationLogListDTO dto = convertToListDTO(parent.get());
                log.info("[CPPModelCalculationLogService] Successfully converted parent execution to DTO");
                return Optional.of(dto);
            } else {
                log.warn("[CPPModelCalculationLogService] Parent execution not found with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogService] Error fetching parent execution by ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get monthly logs for a parent execution
     */
    public List<MonthlyLogDTO> getMonthlyLogsByParentId(UUID parentId) {
        log.info("[CPPModelCalculationLogService] Fetching monthly logs for parent ID: {}", parentId);
        try {
            List<CPPModelCalculationLog> monthlyLogs = repository.findMonthlyLogsByParentId(parentId);
            log.info("[CPPModelCalculationLogService] Found {} monthly log records for parent ID: {}", monthlyLogs.size(), parentId);
            
            List<MonthlyLogDTO> result = monthlyLogs.stream()
                    .map(this::convertToMonthlyDTO)
                    .collect(Collectors.toList());
            
            log.info("[CPPModelCalculationLogService] Successfully converted {} monthly logs to DTOs", result.size());
            return result;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogService] Error fetching monthly logs for parent ID {}: {}", parentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get detailed monthly log with parsed JSON
     */
    public Optional<MonthlyLogDetailDTO> getMonthlyLogDetail(UUID logId) {
        log.info("[CPPModelCalculationLogService] Fetching monthly log detail for ID: {}", logId);
        try {
            Optional<CPPModelCalculationLog> logOptional = repository.findById(logId);
            
            if (logOptional.isEmpty()) {
                log.warn("[CPPModelCalculationLogService] Monthly log not found with ID: {}", logId);
                return Optional.empty();
            }
            
            CPPModelCalculationLog monthlyLog = logOptional.get();
            if (monthlyLog.isParentRecord()) {
                log.warn("[CPPModelCalculationLogService] Log ID {} is a parent record, not a monthly log", logId);
                return Optional.empty();
            }
            
            log.info("[CPPModelCalculationLogService] Found monthly log with ID: {} (Month: {})", logId, monthlyLog.getMonth());
            MonthlyLogDetailDTO dto = convertToMonthlyDetailDTO(monthlyLog);
            log.info("[CPPModelCalculationLogService] Successfully converted monthly log detail to DTO with parsed JSON data");
            return Optional.of(dto);
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogService] Error fetching monthly log detail for ID {}: {}", logId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get monthly log by parent ID and month
     */
    public Optional<MonthlyLogDetailDTO> getMonthlyLogByParentAndMonth(UUID parentId, Integer month) {
        log.info("[CPPModelCalculationLogService] Fetching monthly log for parent ID: {}, month: {}", parentId, month);
        try {
            Optional<CPPModelCalculationLog> logOptional = repository.findMonthlyLogByParentIdAndMonth(parentId, month);
            
            if (logOptional.isPresent()) {
                log.info("[CPPModelCalculationLogService] Found monthly log for parent ID: {}, month: {}", parentId, month);
                MonthlyLogDetailDTO dto = convertToMonthlyDetailDTO(logOptional.get());
                log.info("[CPPModelCalculationLogService] Successfully converted monthly log to DTO with parsed JSON data");
                return Optional.of(dto);
            } else {
                log.warn("[CPPModelCalculationLogService] Monthly log not found for parent ID: {}, month: {}", parentId, month);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogService] Error fetching monthly log for parent ID: {}, month: {} - Error: {}", 
                    parentId, month, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Check if parent execution exists
     */
    public boolean parentExecutionExists(UUID id) {
        log.info("[CPPModelCalculationLogService] Checking if parent execution exists with ID: {}", id);
        try {
            Integer result = repository.existsParentExecutionById(id);
            boolean exists = result != null && result > 0;
            log.info("[CPPModelCalculationLogService] Parent execution with ID {} exists: {}", id, exists);
            return exists;
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogService] Error checking parent execution existence for ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get latest parent execution
     */
    public Optional<CPPModelCalculationLogListDTO> getLatestParentExecution() {
        log.info("[CPPModelCalculationLogService] Fetching latest parent execution");
        try {
            Optional<CPPModelCalculationLog> latestOptional = repository.findLatestParentExecution();
            
            if (latestOptional.isPresent()) {
                CPPModelCalculationLog latest = latestOptional.get();
                log.info("[CPPModelCalculationLogService] Found latest parent execution - ID: {}, Financial Year: {}, Execution Date: {}", 
                        latest.getId(), latest.getFinancialYear(), latest.getExecutionDateTime());
                CPPModelCalculationLogListDTO dto = convertToListDTO(latest);
                log.info("[CPPModelCalculationLogService] Successfully converted latest parent execution to DTO");
                return Optional.of(dto);
            } else {
                log.warn("[CPPModelCalculationLogService] No parent executions found in the system");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("[CPPModelCalculationLogService] Error fetching latest parent execution: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ==================== Conversion Methods ====================

    /**
     * Convert entity to list DTO with monthly summary statistics
     */
    private CPPModelCalculationLogListDTO convertToListDTO(CPPModelCalculationLog parent) {
        log.debug("[CPPModelCalculationLogService] Converting parent execution {} to DTO", parent.getId());
        // Get monthly logs to calculate statistics
        List<CPPModelCalculationLog> monthlyLogs = repository.findMonthlyLogsByParentId(parent.getId());
        log.debug("[CPPModelCalculationLogService] Found {} monthly logs for parent {}", monthlyLogs.size(), parent.getId());
        
        long monthsSucceeded = monthlyLogs.stream()
                .filter(log -> "Success".equalsIgnoreCase(log.getStatus()))
                .count();
        
        long monthsFailed = monthlyLogs.stream()
                .filter(log -> "Failed".equalsIgnoreCase(log.getStatus()))
                .count();
        
        long monthsWithWarnings = monthlyLogs.stream()
                .filter(log -> "Warning".equalsIgnoreCase(log.getStatus()))
                .count();

        // Format financial year as "2025-26"
        String financialYearDisplay = null;
        if (parent.getFinancialYear() != null) {
            int nextYear = (parent.getFinancialYear() + 1) % 100; // Get last 2 digits of next year
            financialYearDisplay = parent.getFinancialYear() + "-" + String.format("%02d", nextYear);
        }

        // Format execution datetime as "DD-MM-YYYY HH:MM AM/PM"
        String executionDateTimeFormatted = null;
        if (parent.getExecutionDateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            executionDateTimeFormatted = sdf.format(parent.getExecutionDateTime());
        }

        return CPPModelCalculationLogListDTO.builder()
                .id(parent.getId())
                .financialYear(parent.getFinancialYear())
                .financialYearDisplay(financialYearDisplay)
                .executionDateTime(parent.getExecutionDateTime())
                .executionDateTimeFormatted(executionDateTimeFormatted)
                .status(parent.getStatus())
                .totalIterations(parent.getIterationCount())
                .totalMonthsProcessed((int) monthlyLogs.size())
                .totalExecutionTime(parent.getExecutionTimeSeconds() != null ? 
                    parent.getExecutionTimeSeconds().toString() + "s" : null)
                .monthsSucceeded(monthsSucceeded)
                .monthsFailed(monthsFailed)
                .monthsWithWarnings(monthsWithWarnings)
                .build();
    }

    /**
     * Convert entity to monthly log DTO (summary)
     */
    private MonthlyLogDTO convertToMonthlyDTO(CPPModelCalculationLog monthlyLog) {
        log.debug("[CPPModelCalculationLogService] Converting monthly log {} (Month: {}) to summary DTO", monthlyLog.getId(), monthlyLog.getMonth());
        
        // Format financial year as "2025-26"
        String financialYearDisplay = null;
        if (monthlyLog.getFinancialYear() != null) {
            int nextYear = (monthlyLog.getFinancialYear() + 1) % 100;
            financialYearDisplay = monthlyLog.getFinancialYear() + "-" + String.format("%02d", nextYear);
        }
        
        return MonthlyLogDTO.builder()
                .id(monthlyLog.getId())
                .parentExecutionFkId(monthlyLog.getParentExecutionFkId())
                .financialYear(monthlyLog.getFinancialYear())
                .financialYearDisplay(financialYearDisplay)
                .month(monthlyLog.getMonth())
                .executionDateTime(monthlyLog.getExecutionDateTime())
                .status(monthlyLog.getStatus())
                .iterations(monthlyLog.getIterationCount())
                .executionTime(monthlyLog.getExecutionTimeSeconds() != null ? 
                    monthlyLog.getExecutionTimeSeconds().toString() + "s" : null)
                .convergenceStatus(monthlyLog.getConvergenceAchieved() != null && monthlyLog.getConvergenceAchieved() ? 
                    "Converged" : "Not Converged")
                .hasAssetStatus(monthlyLog.getAssetStatusJson() != null && !monthlyLog.getAssetStatusJson().isEmpty())
                .hasPowerBalance(monthlyLog.getPowerBalanceJson() != null && !monthlyLog.getPowerBalanceJson().isEmpty())
                .hasSteamBalance(monthlyLog.getSteamBalanceJson() != null && !monthlyLog.getSteamBalanceJson().isEmpty())
                .build();
    }

    /**
     * Convert entity to monthly log detail DTO with parsed JSON
     */
    private MonthlyLogDetailDTO convertToMonthlyDetailDTO(CPPModelCalculationLog monthlyLog) {
        log.debug("[CPPModelCalculationLogService] Converting monthly log {} (Month: {}) to detailed DTO with JSON parsing", monthlyLog.getId(), monthlyLog.getMonth());
        return MonthlyLogDetailDTO.builder()
                .id(monthlyLog.getId())
                .parentExecutionFkId(monthlyLog.getParentExecutionFkId())
                .financialYear(monthlyLog.getFinancialYear())
                .month(monthlyLog.getMonth())
                .executionDateTime(monthlyLog.getExecutionDateTime())
                .status(monthlyLog.getStatus())
                .iterations(monthlyLog.getIterationCount())
                .executionTime(monthlyLog.getExecutionTimeSeconds() != null ? 
                    monthlyLog.getExecutionTimeSeconds().toString() + "s" : null)
                .convergenceStatus(monthlyLog.getConvergenceAchieved() != null && monthlyLog.getConvergenceAchieved() ? 
                    "Converged" : "Not Converged")
                .assetStatus(parseAssetStatusJson(monthlyLog.getAssetStatusJson()))
                .powerBalance(parsePowerBalanceJson(monthlyLog.getPowerBalanceJson()))
                .steamBalance(parseSteamBalanceJson(monthlyLog.getSteamBalanceJson()))
                .build();
    }

    // ==================== JSON Parsing Methods ====================

    /**
     * Parse Asset Status JSON
     * Python structure: Array directly (not wrapped)
     * GT/STG: {"asset": "GT1", "type": "GT", "priority": 1, "grossMWh": 18500.5, "avgLoadingMW": 68.5, ...}
     * HRSG: {"asset": "HRSG1", "type": "HRSG", "steamGenerationMT": 1234.5, "freeSteamMT": 5678.9, ...}
     */
    private AssetStatusDTO parseAssetStatusJson(String json) {
        if (json == null || json.isEmpty()) {
            log.debug("[CPPModelCalculationLogService] Asset status JSON is null or empty, skipping parsing");
            return null;
        }

        log.debug("[CPPModelCalculationLogService] Parsing asset status JSON (length: {} chars)", json.length());
        try {
            JsonNode root = objectMapper.readTree(json);
            
            // Python saves array directly, not wrapped in "assets" key
            if (!root.isArray()) {
                log.warn("[CPPModelCalculationLogService] Asset status JSON root is not an array, expected array format");
                return null;
            }

            List<AssetStatusDTO.AssetLoadingDTO> assets = new ArrayList<>();
            log.debug("[CPPModelCalculationLogService] Parsing {} assets from JSON array", root.size());
            for (JsonNode assetNode : root) {
                String assetType = getStringOrNull(assetNode, "type");
                
                // Common fields
                AssetStatusDTO.AssetLoadingDTO.AssetLoadingDTOBuilder builder = AssetStatusDTO.AssetLoadingDTO.builder()
                        .assetName(getStringOrNull(assetNode, "asset"))
                        .assetType(assetType)
                        .priority(getIntegerOrNull(assetNode, "priority"))
                        .isAvailable(getBooleanOrNull(assetNode, "isAvailable"))
                        .operatingHours(getDoubleOrNull(assetNode, "operatingHours"))
                        .status(getStringOrNull(assetNode, "status"));
                
                // Type-specific fields
                if ("GT".equals(assetType) || "STG".equals(assetType) || "ImportPower".equals(assetType)) {
                    // GT/STG/ImportPower fields
                    Double grossMWh = getDoubleOrNull(assetNode, "grossMWh");
                    builder.minCapacityMW(getDoubleOrNull(assetNode, "minCapacityMW"))
                           .maxCapacityMW(getDoubleOrNull(assetNode, "maxCapacityMW"))
                           .dispatchedLoadMW(getDoubleOrNull(assetNode, "dispatchedLoadMW"))
                           .avgLoadingMW(getDoubleOrNull(assetNode, "avgLoadingMW"))
                           .grossMWh(grossMWh)
                           .netMWh(getDoubleOrNull(assetNode, "netMWh"))
                           .auxiliaryMWh(getDoubleOrNull(assetNode, "auxiliaryMWh"))
                           .loading(grossMWh)  // Legacy field
                           .unit("MWh");  // Legacy field
                } 
                else if ("HRSG".equals(assetType)) {
                    // HRSG fields
                    Double steamGenMT = getDoubleOrNull(assetNode, "steamGenerationMT");
                    builder.steamGenerationMT(steamGenMT)
                           .freeSteamMT(getDoubleOrNull(assetNode, "freeSteamMT"))
                           .avgSteamGenRateMTPerHr(getDoubleOrNull(assetNode, "avgSteamGenRateMTPerHr"))
                           .loading(steamGenMT)  // Legacy field
                           .unit("MT");  // Legacy field
                }
                
                assets.add(builder.build());
            }

            log.debug("[CPPModelCalculationLogService] Successfully parsed {} assets from JSON", assets.size());
            return AssetStatusDTO.builder()
                    .assets(assets)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("[CPPModelCalculationLogService] Failed to parse asset status JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse Power Balance JSON
     * Python structure:
     * {
     *   "demand": {"processDemand": 27636.925, "fixedDemand": 0, "u4uDemand": 0, "utilityAuxPower": 0, "total": 27636.925},
     *   "supply": {"importPower": 18000.0, "gt1Net": 0, "gt2Net": 0, "gt3Net": 0, "stgNet": 0, "totalNetGeneration": 0, "total": 18000.0},
     *   "balance": {"difference": -9636.925, "isBalanced": false, "excessPower": 0, "shortfall": 9636.925}
     * }
     */
    private PowerBalanceDTO parsePowerBalanceJson(String json) {
        if (json == null || json.isEmpty()) {
            log.debug("[CPPModelCalculationLogService] Power balance JSON is null or empty, skipping parsing");
            return null;
        }

        log.debug("[CPPModelCalculationLogService] Parsing power balance JSON (length: {} chars)", json.length());
        try {
            JsonNode root = objectMapper.readTree(json);
            
            JsonNode supplyNode = root.get("supply");
            JsonNode demandNode = root.get("demand");

            PowerBalanceDTO.SupplyDTO supply = null;
            if (supplyNode != null) {
                supply = PowerBalanceDTO.SupplyDTO.builder()
                        .gasEngine(getDoubleOrNull(supplyNode, "gt1Net") != null ?
                                   getDoubleOrNull(supplyNode, "gt1Net") + 
                                   getDoubleOrNull(supplyNode, "gt2Net") + 
                                   getDoubleOrNull(supplyNode, "gt3Net") : null)
                        .steamTurbine(getDoubleOrNull(supplyNode, "stgNet"))
                        .importPower(getDoubleOrNull(supplyNode, "importPower"))
                        .totalSupply(getDoubleOrNull(supplyNode, "total"))
                        .build();
            }

            PowerBalanceDTO.DemandDTO demand = null;
            if (demandNode != null) {
                demand = PowerBalanceDTO.DemandDTO.builder()
                        .processDemand(getDoubleOrNull(demandNode, "processDemand"))
                        .u4uPower(getDoubleOrNull(demandNode, "u4uDemand"))
                        .totalDemand(getDoubleOrNull(demandNode, "total"))
                        .build();
            }

            log.debug("[CPPModelCalculationLogService] Successfully parsed power balance JSON - Supply: {}, Demand: {}", 
                    supply != null ? supply.getTotalSupply() : null, demand != null ? demand.getTotalDemand() : null);
            return PowerBalanceDTO.builder()
                    .supply(supply)
                    .demand(demand)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("[CPPModelCalculationLogService] Failed to parse power balance JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse Steam Balance JSON
     * Python structure:
     * {
     *   "SHP": {"demand": {...}, "supply": {...}, "balance": {...}},
     *   "HP": {"demand": {...}, "supply": {...}, "balance": {...}},
     *   "MP": {"demand": {...}, "supply": {...}, "balance": {...}},
     *   "LP": {"demand": {...}, "supply": {...}, "balance": {...}}
     * }
     */
    private SteamBalanceDTO parseSteamBalanceJson(String json) {
        if (json == null || json.isEmpty()) {
            log.debug("[CPPModelCalculationLogService] Steam balance JSON is null or empty, skipping parsing");
            return null;
        }

        log.debug("[CPPModelCalculationLogService] Parsing steam balance JSON (length: {} chars)", json.length());
        try {
            JsonNode root = objectMapper.readTree(json);

            SteamBalanceDTO result = SteamBalanceDTO.builder()
                    .shp(parseSteamTypeBalance(root.get("SHP"), "SHP"))
                    .hp(parseSteamTypeBalance(root.get("HP"), "HP"))
                    .mp(parseSteamTypeBalance(root.get("MP"), "MP"))
                    .lp(parseSteamTypeBalance(root.get("LP"), "LP"))
                    .build();
            log.debug("[CPPModelCalculationLogService] Successfully parsed steam balance JSON for all steam types");
            return result;

        } catch (JsonProcessingException e) {
            log.error("[CPPModelCalculationLogService] Failed to parse steam balance JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse individual steam type balance
     * Python structure: {"demand": {"total": 150.0, ...}, "supply": {"total": 155.0, ...}, "balance": {"difference": 5.0, ...}}
     */
    private SteamBalanceDTO.SteamTypeBalanceDTO parseSteamTypeBalance(JsonNode node, String steamType) {
        if (node == null) {
            log.debug("[CPPModelCalculationLogService] No data found for steam type: {}", steamType);
            return null;
        }
        log.debug("[CPPModelCalculationLogService] Parsing steam balance for type: {}", steamType);

        JsonNode demandNode = node.get("demand");
        JsonNode supplyNode = node.get("supply");
        JsonNode balanceNode = node.get("balance");

        Double totalDemand = demandNode != null ? getDoubleOrNull(demandNode, "total") : null;
        Double totalSupply = supplyNode != null ? getDoubleOrNull(supplyNode, "total") : null;
        Double balance = balanceNode != null ? getDoubleOrNull(balanceNode, "difference") : null;

        return SteamBalanceDTO.SteamTypeBalanceDTO.builder()
                .steamType(steamType)
                .totalDemand(totalDemand)
                .totalSupply(totalSupply)
                .balance(balance)
                .build();
    }

    // ==================== JSON Helper Methods ====================

    private Double getDoubleOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asDouble() : null;
    }

    private String getStringOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asText() : null;
    }
    
    private Integer getIntegerOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asInt() : null;
    }
    
    private Boolean getBooleanOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return (field != null && !field.isNull()) ? field.asBoolean() : null;
    }
}
