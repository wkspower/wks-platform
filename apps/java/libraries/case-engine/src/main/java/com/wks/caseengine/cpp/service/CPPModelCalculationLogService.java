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
        List<CPPModelCalculationLog> parents = repository.findAllParentExecutions();
        return parents.stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get parent executions with optional filters
     */
    public List<CPPModelCalculationLogListDTO> getParentExecutionsWithFilters(
            Integer financialYear, String status) {
        List<CPPModelCalculationLog> parents = repository.findParentExecutionsWithFilters(
                financialYear, status);
        return parents.stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get parent execution by ID
     */
    public Optional<CPPModelCalculationLogListDTO> getParentExecutionById(UUID id) {
        return repository.findParentExecutionById(id)
                .map(this::convertToListDTO);
    }

    /**
     * Get monthly logs for a parent execution
     */
    public List<MonthlyLogDTO> getMonthlyLogsByParentId(UUID parentId) {
        List<CPPModelCalculationLog> monthlyLogs = repository.findMonthlyLogsByParentId(parentId);
        return monthlyLogs.stream()
                .map(this::convertToMonthlyDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get detailed monthly log with parsed JSON
     */
    public Optional<MonthlyLogDetailDTO> getMonthlyLogDetail(UUID logId) {
        return repository.findById(logId)
                .filter(log -> !log.isParentRecord())
                .map(this::convertToMonthlyDetailDTO);
    }

    /**
     * Get monthly log by parent ID and month
     */
    public Optional<MonthlyLogDetailDTO> getMonthlyLogByParentAndMonth(UUID parentId, Integer month) {
        return repository.findMonthlyLogByParentIdAndMonth(parentId, month)
                .map(this::convertToMonthlyDetailDTO);
    }

    /**
     * Check if parent execution exists
     */
    public boolean parentExecutionExists(UUID id) {
        return repository.existsParentExecutionById(id);
    }

    /**
     * Get latest parent execution
     */
    public Optional<CPPModelCalculationLogListDTO> getLatestParentExecution() {
        return repository.findLatestParentExecution()
                .map(this::convertToListDTO);
    }

    // ==================== Conversion Methods ====================

    /**
     * Convert entity to list DTO with monthly summary statistics
     */
    private CPPModelCalculationLogListDTO convertToListDTO(CPPModelCalculationLog parent) {
        // Get monthly logs to calculate statistics
        List<CPPModelCalculationLog> monthlyLogs = repository.findMonthlyLogsByParentId(parent.getId());
        
        long monthsSucceeded = monthlyLogs.stream()
                .filter(log -> "Success".equalsIgnoreCase(log.getStatus()))
                .count();
        
        long monthsFailed = monthlyLogs.stream()
                .filter(log -> "Failed".equalsIgnoreCase(log.getStatus()))
                .count();
        
        long monthsWithWarnings = monthlyLogs.stream()
                .filter(log -> "Warning".equalsIgnoreCase(log.getStatus()))
                .count();

        return CPPModelCalculationLogListDTO.builder()
                .id(parent.getId())
                .financialYear(parent.getFinancialYear())
                .executionDateTime(parent.getExecutionDateTime())
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
    private MonthlyLogDTO convertToMonthlyDTO(CPPModelCalculationLog log) {
        return MonthlyLogDTO.builder()
                .id(log.getId())
                .parentExecutionFkId(log.getParentExecutionFkId())
                .financialYear(log.getFinancialYear())
                .month(log.getMonth())
                .executionDateTime(log.getExecutionDateTime())
                .status(log.getStatus())
                .iterations(log.getIterationCount())
                .executionTime(log.getExecutionTimeSeconds() != null ? 
                    log.getExecutionTimeSeconds().toString() + "s" : null)
                .convergenceStatus(log.getConvergenceAchieved() != null && log.getConvergenceAchieved() ? 
                    "Converged" : "Not Converged")
                .hasAssetStatus(log.getAssetStatusJson() != null && !log.getAssetStatusJson().isEmpty())
                .hasPowerBalance(log.getPowerBalanceJson() != null && !log.getPowerBalanceJson().isEmpty())
                .hasSteamBalance(log.getSteamBalanceJson() != null && !log.getSteamBalanceJson().isEmpty())
                .build();
    }

    /**
     * Convert entity to monthly log detail DTO with parsed JSON
     */
    private MonthlyLogDetailDTO convertToMonthlyDetailDTO(CPPModelCalculationLog log) {
        return MonthlyLogDetailDTO.builder()
                .id(log.getId())
                .parentExecutionFkId(log.getParentExecutionFkId())
                .financialYear(log.getFinancialYear())
                .month(log.getMonth())
                .executionDateTime(log.getExecutionDateTime())
                .status(log.getStatus())
                .iterations(log.getIterationCount())
                .executionTime(log.getExecutionTimeSeconds() != null ? 
                    log.getExecutionTimeSeconds().toString() + "s" : null)
                .convergenceStatus(log.getConvergenceAchieved() != null && log.getConvergenceAchieved() ? 
                    "Converged" : "Not Converged")
                .assetStatus(parseAssetStatusJson(log.getAssetStatusJson()))
                .powerBalance(parsePowerBalanceJson(log.getPowerBalanceJson()))
                .steamBalance(parseSteamBalanceJson(log.getSteamBalanceJson()))
                .build();
    }

    // ==================== JSON Parsing Methods ====================

    /**
     * Parse Asset Status JSON
     * Python structure: Array directly (not wrapped)
     * [
     *   {"asset": "GT1", "type": "GT", "grossMWh": 18500.5, "netMWh": 18000.0, ...},
     *   {"asset": "HRSG1", "type": "HRSG", "isAvailable": true, "operatingHours": 720, 
     *    "steamGenerationMT": 1234.5, "status": "Running"},
     *   ...
     * ]
     */
    private AssetStatusDTO parseAssetStatusJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            
            // Python saves array directly, not wrapped in "assets" key
            if (!root.isArray()) {
                return null;
            }

            List<AssetStatusDTO.AssetLoadingDTO> assets = new ArrayList<>();
            for (JsonNode assetNode : root) {
                String assetType = getStringOrNull(assetNode, "type");
                Double loading = null;
                String unit = null;
                
                // For GT/STG assets, use grossMWh or netMWh
                if ("GT".equals(assetType) || "STG".equals(assetType) || "ImportPower".equals(assetType)) {
                    loading = getDoubleOrNull(assetNode, "grossMWh");
                    if (loading == null) {
                        loading = getDoubleOrNull(assetNode, "netMWh");
                    }
                    unit = "MWh";
                }
                // For HRSG assets, use steamGenerationMT (SHP steam generation)
                else if ("HRSG".equals(assetType)) {
                    loading = getDoubleOrNull(assetNode, "steamGenerationMT");
                    unit = "MT";  // Metric Tons of SHP steam
                }
                
                AssetStatusDTO.AssetLoadingDTO asset = AssetStatusDTO.AssetLoadingDTO.builder()
                        .assetName(getStringOrNull(assetNode, "asset"))
                        .assetType(assetType)
                        .loading(loading)
                        .unit(unit)
                        .build();
                assets.add(asset);
            }

            return AssetStatusDTO.builder()
                    .assets(assets)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse asset status JSON: {}", e.getMessage());
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
            return null;
        }

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

            return PowerBalanceDTO.builder()
                    .supply(supply)
                    .demand(demand)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse power balance JSON: {}", e.getMessage());
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
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(json);

            return SteamBalanceDTO.builder()
                    .shp(parseSteamTypeBalance(root.get("SHP"), "SHP"))
                    .hp(parseSteamTypeBalance(root.get("HP"), "HP"))
                    .mp(parseSteamTypeBalance(root.get("MP"), "MP"))
                    .lp(parseSteamTypeBalance(root.get("LP"), "LP"))
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Failed to parse steam balance JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parse individual steam type balance
     * Python structure: {"demand": {"total": 150.0, ...}, "supply": {"total": 155.0, ...}, "balance": {"difference": 5.0, ...}}
     */
    private SteamBalanceDTO.SteamTypeBalanceDTO parseSteamTypeBalance(JsonNode node, String steamType) {
        if (node == null) {
            return null;
        }

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
}
