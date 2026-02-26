package com.wks.caseengine.cpp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateLookupDTO;
import com.wks.caseengine.cpp.dto.heatrate.HeatRateDTO;
import com.wks.caseengine.cpp.dto.heatrate.HeatRateProjection;
import com.wks.caseengine.cpp.dto.heatrate.SelectedHeatRateType;
import com.wks.caseengine.cpp.dto.heatrate.STGExtractionLookupDTO;
import com.wks.caseengine.cpp.entity.HRSGHeatRateLookup;
import com.wks.caseengine.cpp.entity.STGExtractionLookup;
import com.wks.caseengine.cpp.repository.HRSGHeatRateLookupRepository;
import com.wks.caseengine.cpp.repository.HeatRateRepository;
import com.wks.caseengine.cpp.repository.STGExtractionLookupRepository;


@Service
public class HeatRateService {
    
    private static final Logger logger = LoggerFactory.getLogger(HeatRateService.class);
    
    @Autowired
    private HeatRateRepository heatRateRepository;

    @Autowired
    private STGExtractionLookupRepository stgExtractionLookupRepository;

    @Autowired
    private HRSGHeatRateLookupRepository hrsgHeatRateLookupRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // original
    public List<Object[]> getAssetNamesByCppIdAndAssetType(String cppId) {
       
        //harcoding asset type for HeatRate drop down list
        String assetType = "GT";
      
        return heatRateRepository.findAssetNamesByCppIdAndAssetType(UUID.fromString(cppId), assetType).stream()
                .map(projection -> new Object[] { projection.getAssetId(), projection.getAssetName() })
                .toList();

    }

    // HRSG dropdown
    public List<Object[]> getHRSGAssetNamesByCppId(String cppId) {
        logger.info("========== SERVICE: getHRSGAssetNamesByCppId ==========");
        logger.info("Input Parameters - cppId: {}", cppId);
        
        String assetType = "HRSG";
        UUID cppUUID = UUID.fromString(cppId);
        logger.info("Converted cppId to UUID: {}", cppUUID);
        logger.info("Querying SteamGenerationAssets for AssetType: {}", assetType);
        
        List<Object[]> result = heatRateRepository.findHRSGAssetNamesByCppIdAndAssetType(cppUUID, assetType).stream()
                .map(projection -> {
                    logger.debug("Mapping HRSG asset - AssetId: {}, AssetName: {}", 
                        projection.getAssetId(), projection.getAssetName());
                    return new Object[] { projection.getAssetId(), projection.getAssetName() };
                })
                .toList();
        
        logger.info("Repository returned {} HRSG assets", result.size());
        if (!result.isEmpty()) {
            logger.info("HRSG Assets found:");
            for (Object[] asset : result) {
                logger.info("  - AssetId: {}, AssetName: {}", asset[0], asset[1]);
            }
        } else {
            logger.warn("No HRSG assets found for cppId: {} with AssetType: {}", cppId, assetType);
        }
        logger.info("========== SERVICE: getHRSGAssetNamesByCppId COMPLETED ==========");
        
        return result;
    }

    // ============================================================
    // HRSG HEAT RATE METHODS
    // ============================================================

    /**
     * Get HRSG heat rate data by asset ID and financial year
     */
    public List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO> getHRSGHeatRateByAssetId(String assetId, String financialYear) {
        logger.info("========== SERVICE: getHRSGHeatRateByAssetId ==========");
        logger.info("Input Parameters:");
        logger.info("  - assetId: {}", assetId);
        logger.info("  - financialYear: {}", financialYear);
        
        // Calculate previous financial year
        String previousFinancialYear = calculatePreviousFinancialYear(financialYear);
        logger.info("  - previousFinancialYear (calculated): {}", previousFinancialYear);
        
        UUID assetUUID = UUID.fromString(assetId);
        logger.info("Calling repository.findHrsgHeatRateByAssetId with UUID: {}", assetUUID);

        List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateProjection> projections = 
            heatRateRepository.findHrsgHeatRateByAssetId(assetUUID, financialYear, previousFinancialYear);
        logger.info("Repository returned {} projection records", projections != null ? projections.size() : 0);
        
        if (projections != null && !projections.isEmpty()) {
            com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateProjection firstProj = projections.get(0);
            logger.info("First projection from DB:");
            logger.info("  - getId(): {}", firstProj.getId());
            logger.info("  - getEquipType(): {}", firstProj.getEquipType());
            logger.info("  - getHRSGLoad(): {}", firstProj.getHRSGLoad());
            logger.info("  - getHeatRate(): {}", firstProj.getHeatRate());
            logger.info("  - getPreviousYearHeatRate(): {}", firstProj.getPreviousYearHeatRate());
            logger.info("  - getFinalHeatRate(): {}", firstProj.getFinalHeatRate());
        }
        
        List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO> result = projections.stream()
                .map(projection -> {
                    com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO dto = new com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO();
                    dto.setId(projection.getId());
                    dto.setEquipType(projection.getEquipType());
                    dto.setCppUtility(projection.getCPPUtility());
                    dto.setHrsgLoad(projection.getHRSGLoad());
                    dto.setHeatRate(projection.getHeatRate());
                    dto.setRemarks(projection.getRemarks());
                    dto.setPreviousYearHeatRate(projection.getPreviousYearHeatRate());
                    dto.setFinalHeatRate(projection.getFinalHeatRate());
                    dto.setOemHeatRate(projection.getOEMHeatRate());
                    dto.setSelectedHeatRate(projection.getSelectedHeatRate());
                    
                    logger.debug("Mapped DTO - hrsgLoad: {}, finalHeatRate: {}, oemHeatRate: {}, selectedHeatRate: {}", 
                        dto.getHrsgLoad(), dto.getFinalHeatRate(), dto.getOemHeatRate(), dto.getSelectedHeatRate());
                    return dto;
                })
                .toList();
        
        logger.info("Returning {} HRSG heat rate records", result.size());
        logger.info("========== SERVICE: getHRSGHeatRateByAssetId COMPLETED ==========");
        
        return result;
    }

    /**
     * Get HRSG heat rate data with proposed heat rate calculated from date range
     */
    public List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO> getHRSGHeatRateByAssetIdWithProposed(
            String assetId, String financialYear, String startDate, String endDate) {
        logger.info("========== SERVICE: getHRSGHeatRateByAssetIdWithProposed ==========");
        logger.info("Input Parameters:");
        logger.info("  - assetId: {}", assetId);
        logger.info("  - financialYear: {}", financialYear);
        logger.info("  - startDate: {}", startDate);
        logger.info("  - endDate: {}", endDate);
        
        // Get base HRSG heat rate data from table
        List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO> hrsgHeatRateDTOs = getHRSGHeatRateByAssetId(assetId, financialYear);
        
        // Calculate proposed heat rates from date range if dates are provided
        if (startDate != null && !startDate.trim().isEmpty() && endDate != null && !endDate.trim().isEmpty()) {
            UUID assetUUID = UUID.fromString(assetId);
            java.util.Map<Double, Double> proposedHeatRateMap = calculateProposedHRSGHeatRates(assetUUID, startDate, endDate);
            
            // Merge proposed heat rates with existing data
            for (com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO dto : hrsgHeatRateDTOs) {
                Double proposedHeatRate = proposedHeatRateMap.get(dto.getHrsgLoad());
                if (proposedHeatRate != null) {
                    dto.setProposedHeatRate(proposedHeatRate);
                    logger.debug("Set proposedHeatRate for HRSGLoad {}: {}", dto.getHrsgLoad(), proposedHeatRate);
                }
            }
            
            logger.info("Merged proposed heat rates for {} load points", proposedHeatRateMap.size());
        } else {
            logger.info("No date range provided, skipping proposed heat rate calculation");
        }
        
        logger.info("Returning {} HRSG heat rate records with proposed data", hrsgHeatRateDTOs.size());
        logger.info("========== SERVICE: getHRSGHeatRateByAssetIdWithProposed COMPLETED ==========");
        
        return hrsgHeatRateDTOs;
    }

    /**
     * Calculate proposed HRSG heat rates using stored procedure
     */
    private java.util.Map<Double, Double> calculateProposedHRSGHeatRates(UUID assetId, String startDate, String endDate) {
        logger.info("========== calculateProposedHRSGHeatRates START ==========");
        logger.info("Calculating proposed HRSG heat rates for assetId: {}, dateRange: {} to {}", assetId, startDate, endDate);
        
        // Get asset name - try multiple sources
        String assetName = null;
        
        // First try: Get from CPP_HRSGHeatRate table (most reliable as it's already populated)
        try {
            assetName = jdbcTemplate.queryForObject(
                "SELECT TOP 1 AssetName FROM CPP_HRSGHeatRate WHERE Asset_FK_Id = ?",
                String.class,
                assetId
            );
            logger.info("HRSG Asset name retrieved from CPP_HRSGHeatRate table: '{}'", assetName);
        } catch (Exception e) {
            logger.warn("Could not retrieve asset name from CPP_HRSGHeatRate, trying SteamGenerationAssets. Error: {}", e.getMessage());
            
            // Second try: SteamGenerationAssets with AssetName column
            try {
                assetName = jdbcTemplate.queryForObject(
                    "SELECT AssetName FROM SteamGenerationAssets WHERE AssetId = ?",
                    String.class,
                    assetId
                );
                logger.info("HRSG Asset name retrieved from SteamGenerationAssets.AssetName: '{}'", assetName);
            } catch (Exception e2) {
                logger.warn("AssetName column not found, trying displayName column. Error: {}", e2.getMessage());
                
                // Third try: SteamGenerationAssets with displayName column
                try {
                    assetName = jdbcTemplate.queryForObject(
                        "SELECT displayName FROM SteamGenerationAssets WHERE AssetId = ?",
                        String.class,
                        assetId
                    );
                    logger.info("HRSG Asset name retrieved from SteamGenerationAssets.displayName: '{}'", assetName);
                } catch (Exception e3) {
                    logger.error("Error retrieving HRSG asset name for assetId {} from all sources: {}", assetId, e3.getMessage());
                }
            }
        }
        
        if (assetName == null || assetName.trim().isEmpty()) {
            logger.warn("HRSG Asset not found or empty for assetId: {}", assetId);
            return new java.util.HashMap<>();
        }
        
        // Convert HRSG1, HRSG2, HRSG3 to HRSG-1, HRSG-2, HRSG-3 for SP
        String spAssetName = assetName.replace("HRSG", "HRSG-");
        logger.info("Converted asset name for SP: '{}' -> '{}'", assetName, spAssetName);
        
        // Call the HRSG stored procedure
        String sql = "EXEC CPP_CalculateHRSGHeatRate_ByDateRange @StartDate = ?, @EndDate = ?, @AssetName = ?";
        logger.info("Calling HRSG SP with parameters: StartDate='{}', EndDate='{}', AssetName='{}'", startDate, endDate, spAssetName);
        
        java.util.Map<Double, Double> proposedHeatRateMap = new java.util.HashMap<>();
        
        try {
            jdbcTemplate.query(sql, 
                (rs) -> {
                    Double hrsgLoad = rs.getDouble("HRSGLoad");
                    Double heatRate = rs.getDouble("HeatRate");
                    proposedHeatRateMap.put(hrsgLoad, heatRate);
                    logger.debug("HRSG SP returned: HRSGLoad={}, HeatRate={}", hrsgLoad, heatRate);
                },
                startDate, endDate, spAssetName
            );
            
            logger.info("Proposed HRSG heat rates calculated for {} load points", proposedHeatRateMap.size());
            if (proposedHeatRateMap.isEmpty()) {
                logger.warn("WARNING: HRSG Stored procedure returned NO data! Check if CPP_NMD_FCNA_FuelBill has data for asset '{}' in date range {} to {}", 
                    spAssetName, startDate, endDate);
            }
        } catch (Exception e) {
            logger.error("Error calling HRSG stored procedure: {}", e.getMessage(), e);
        }
        
        logger.info("========== calculateProposedHRSGHeatRates END ==========");
        return proposedHeatRateMap;
    }

    /**
     * Update HRSG heat rate records
     */
    public void updateHRSGHeatRate(List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO> hrsgHeatRateDTOs) {
        logger.info("========== SERVICE: updateHRSGHeatRate ==========");
        logger.info("Received {} HRSG heat rate records to update", hrsgHeatRateDTOs != null ? hrsgHeatRateDTOs.size() : 0);
        
        List<Object[]> updates = new ArrayList<>();
        for(com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO dto : hrsgHeatRateDTOs) {
            // Validate selectedHeatRate field
            String selectedHeatRate = dto.getSelectedHeatRate();
            if (selectedHeatRate != null && !selectedHeatRate.trim().isEmpty()) {
                if (!SelectedHeatRateType.isValid(selectedHeatRate)) {
                    logger.error("Invalid selectedHeatRate value: '{}' for ID: {}. Must be one of: OEM, PREVIOUS_YEAR, PROPOSED, OTHER", 
                        selectedHeatRate, dto.getId());
                    throw new IllegalArgumentException(
                        String.format("Invalid selectedHeatRate value: '%s'. Must be one of: OEM, PREVIOUS_YEAR, PROPOSED, OTHER", 
                        selectedHeatRate));
                }
            } else {
                // Set default value to PROPOSED if null or empty
                selectedHeatRate = SelectedHeatRateType.PROPOSED.getValue();
                dto.setSelectedHeatRate(selectedHeatRate);
                logger.debug("SelectedHeatRate was null/empty for ID: {}, setting default value: PROPOSED", dto.getId());
            }
            
            logger.debug("Preparing update for HRSG ID: {}, HRSGLoad: {}, HeatRate: {}, FinalHeatRate: {}, OEMHeatRate: {}, SelectedHeatRate: {}", 
                dto.getId(), dto.getHrsgLoad(), dto.getHeatRate(), 
                dto.getFinalHeatRate(), dto.getOemHeatRate(), selectedHeatRate);
            
            updates.add(new Object[] { 
                dto.getHrsgLoad(), 
                dto.getFinalHeatRate(), 
                dto.getOemHeatRate(),
                selectedHeatRate,
                dto.getRemarks(), 
                dto.getId() 
            });
        }
        
        if(updates.size() > 0) {
            String sql = "UPDATE CPP_HRSGHeatRate SET HRSGLoad = ?, FinalHeatRate = ?, OEMHeatRate = ?, SelectedHeatRate = ?, Remarks = ?, UpdatedDate = GETDATE() WHERE Id = ?";
            logger.info("Executing batch update SQL: {}", sql);
            logger.info("Updating {} HRSG heat rate records in database", updates.size());
            
            int[] updateCounts = jdbcTemplate.batchUpdate(sql, updates);
            logger.info("Batch update completed. {} HRSG heat rate records updated", updateCounts.length);
        } else {
            logger.warn("No HRSG heat rate records to update");
        }
        logger.info("========== SERVICE: updateHRSGHeatRate COMPLETED ==========");
    }

   // original
    public List<HeatRateDTO> getHeatRateByAssetId(String assetId, String financialYear) {
        logger.info("========== SERVICE: getHeatRateByAssetId ==========");
        logger.info("Input Parameters:");
        logger.info("  - assetId: {}", assetId);
        logger.info("  - financialYear: {}", financialYear);
        
        // Calculate previous financial year (e.g., "2026-27" -> "2025-26")
        String previousFinancialYear = calculatePreviousFinancialYear(financialYear);
        logger.info("  - previousFinancialYear (calculated): {}", previousFinancialYear);
        
        UUID assetUUID = UUID.fromString(assetId);
        logger.info("Calling repository.findGtHeatRateByAssetId with UUID: {}", assetUUID);

        List<HeatRateProjection> projections = heatRateRepository.findGtHeatRateByAssetId(assetUUID, financialYear, previousFinancialYear);
        logger.info("Repository returned {} projection records", projections != null ? projections.size() : 0);
        
        if (projections != null && !projections.isEmpty()) {
            HeatRateProjection firstProj = projections.get(0);
            logger.info("First projection from DB:");
            logger.info("  - getId(): {}", firstProj.getId());
            logger.info("  - getEquipType(): {}", firstProj.getEquipType());
            logger.info("  - getGTLoad(): {}", firstProj.getGTLoad());
            logger.info("  - getHeatRate(): {}", firstProj.getHeatRate());
            logger.info("  - getPreviousYearHeatRate(): {}", firstProj.getPreviousYearHeatRate());
            logger.info("  - getFinalHeatRate(): {}", firstProj.getFinalHeatRate());
            logger.info("  - getFreeSteamFactor(): {}", firstProj.getFreeSteamFactor());
        }
        
        return projections.stream()
                .map(projection -> {
                    HeatRateDTO dto = new HeatRateDTO();
                    dto.setId(projection.getId());
                    dto.setEquipType(projection.getEquipType());
                    dto.setCppUtility(projection.getCPPUtility());
                    dto.setGtLoad(projection.getGTLoad());
                    dto.setHeatRate(projection.getHeatRate());
                    dto.setFreeSteamFactor(projection.getFreeSteamFactor());
                    dto.setRemarks(projection.getRemarks());
                    dto.setPreviousYearHeatRate(projection.getPreviousYearHeatRate());
                    dto.setFinalHeatRate(projection.getFinalHeatRate());
                    dto.setOemHeatRate(projection.getOemHeatRate());
                    dto.setSelectedHeatRate(projection.getSelectedHeatRate());
                    
                    logger.debug("Mapped DTO - finalHeatRate: {}, oemHeatRate: {}, selectedHeatRate: {}", 
                        dto.getFinalHeatRate(), dto.getOemHeatRate(), dto.getSelectedHeatRate());
                    return dto;
                })
                .toList();
    }

    /**
     * Get heat rate data with proposed heat rate calculated from date range
     * 
     * @param assetId Asset UUID
     * @param financialYear Financial year (e.g., "2026-27")
     * @param startDate Start date for proposed calculation (format: YYYY-MM-DD)
     * @param endDate End date for proposed calculation (format: YYYY-MM-DD)
     * @return List of HeatRateDTO with proposedHeatRate populated
     */
    public List<HeatRateDTO> getHeatRateByAssetIdWithProposed(String assetId, String financialYear, String startDate, String endDate) {
        logger.info("========== SERVICE: getHeatRateByAssetIdWithProposed ==========");
        logger.info("Input Parameters:");
        logger.info("  - assetId: {}", assetId);
        logger.info("  - financialYear: {}", financialYear);
        logger.info("  - startDate: {}", startDate);
        logger.info("  - endDate: {}", endDate);
        
        // Get base heat rate data from table
        List<HeatRateDTO> heatRateDTOs = getHeatRateByAssetId(assetId, financialYear);
        
        // Calculate proposed heat rates from date range if dates are provided
        if (startDate != null && !startDate.trim().isEmpty() && endDate != null && !endDate.trim().isEmpty()) {
            UUID assetUUID = UUID.fromString(assetId);
            java.util.Map<Double, Double> proposedHeatRateMap = calculateProposedHeatRates(assetUUID, startDate, endDate);
            
            // Merge proposed heat rates with existing data
            for (HeatRateDTO dto : heatRateDTOs) {
                Double proposedHeatRate = proposedHeatRateMap.get(dto.getGtLoad());
                if (proposedHeatRate != null) {
                    dto.setProposedHeatRate(proposedHeatRate);
                    logger.debug("Set proposedHeatRate for GTLoad {}: {}", dto.getGtLoad(), proposedHeatRate);
                }
            }
            
            logger.info("Merged proposed heat rates for {} load points", proposedHeatRateMap.size());
        } else {
            logger.info("No date range provided, skipping proposed heat rate calculation");
        }
        
        logger.info("Returning {} heat rate records with proposed data", heatRateDTOs.size());
        logger.info("=================================================================");
        
        return heatRateDTOs;
    }

    /**
     * Calculate previous financial year from current financial year
     * Example: "2026-27" -> "2025-26"
     */
    private String calculatePreviousFinancialYear(String financialYear) {
        if (financialYear == null || !financialYear.contains("-")) {
            throw new IllegalArgumentException("Invalid financial year format. Expected format: YYYY-YY");
        }
        
        String[] parts = financialYear.split("-");
        int startYear = Integer.parseInt(parts[0]);
        int endYear = Integer.parseInt(parts[1]);
        
        int prevStartYear = startYear - 1;
        int prevEndYear = endYear - 1;
        
        return prevStartYear + "-" + String.format("%02d", prevEndYear);
    }

    /**
     * Call stored procedure to calculate proposed heat rates based on date range
     * 
     * @param assetId Asset UUID
     * @param startDate Start date for calculation
     * @param endDate End date for calculation
     * @return Map of GTLoad to ProposedHeatRate
     */
    private java.util.Map<Double, Double> calculateProposedHeatRates(UUID assetId, String startDate, String endDate) {
        logger.info("========== calculateProposedHeatRates START ==========");
        logger.info("Calculating proposed heat rates for assetId: {}, dateRange: {} to {}", assetId, startDate, endDate);
        
        // Get asset name from PowerGenerationAssets using displayName column
        String assetName = null;
        try {
            assetName = jdbcTemplate.queryForObject(
                "SELECT displayName FROM PowerGenerationAssets WHERE AssetId = ?",
                String.class,
                assetId
            );
            logger.info("Asset name (displayName) retrieved: '{}'", assetName);
        } catch (Exception e) {
            logger.error("Error retrieving asset name for assetId {}: {}", assetId, e.getMessage());
        }
        
        if (assetName == null || assetName.trim().isEmpty()) {
            logger.warn("Asset not found or empty for assetId: {}", assetId);
            return new java.util.HashMap<>();
        }
        
        // Call the stored procedure
        String sql = "EXEC CPP_CalculateGTHeatRate_ByDateRange @StartDate = ?, @EndDate = ?, @AssetName = ?";
        logger.info("Calling SP with parameters: StartDate='{}', EndDate='{}', AssetName='{}'", startDate, endDate, assetName);
        
        java.util.Map<Double, Double> proposedHeatRateMap = new java.util.HashMap<>();
        
        try {
            jdbcTemplate.query(sql, 
                (rs) -> {
                    Double gtLoad = rs.getDouble("GTLoad");
                    Double heatRate = rs.getDouble("HeatRate");
                    proposedHeatRateMap.put(gtLoad, heatRate);
                    logger.debug("SP returned: GTLoad={}, HeatRate={}", gtLoad, heatRate);
                },
                startDate, endDate, assetName
            );
            
            logger.info("Proposed heat rates calculated for {} load points", proposedHeatRateMap.size());
            if (proposedHeatRateMap.isEmpty()) {
                logger.warn("WARNING: Stored procedure returned NO data! Check if CPP_NMD_FCNA_FuelBill has data for asset '{}' in date range {} to {}", assetName, startDate, endDate);
            }
        } catch (Exception e) {
            logger.error("Error calling stored procedure: {}", e.getMessage(), e);
        }
        
        logger.info("========== calculateProposedHeatRates END ==========");
        return proposedHeatRateMap;
    }

    public List<STGExtractionLookupDTO> getSTGExtractionLookup() {
        return stgExtractionLookupRepository.findAllByOrderByLoadMWAsc().stream()
                .map(this::mapToSTGExtractionDTO)
                .toList();
    }

    private STGExtractionLookupDTO mapToSTGExtractionDTO(STGExtractionLookup entity) {
        return STGExtractionLookupDTO.builder()
                .id(entity.getId())
                .loadMW(entity.getLoadMW())
                .svhInletTPH(entity.getSvhInletTPH())
                .smBleedFlowTPH(entity.getSmBleedFlowTPH())
                .slExtFlowTPH(entity.getSlExtFlowTPH())
                .condensingLoadM3Hr(entity.getCondensingLoadM3Hr())
                .heatRateKcalKWH(entity.getHeatRateKcalKWH())
                .remarks(entity.getRemarks())
                .build();
    }

    // ============================================================
    // HRSG HEAT RATE LOOKUP METHODS
    // ============================================================

    /**
     * Get all HRSG Heat Rate lookup data ordered by EquipmentName and HRSGLoad
     */
    public List<HRSGHeatRateLookupDTO> getHRSGHeatRateLookup() {
        return hrsgHeatRateLookupRepository.findAllByOrderByHrsgLoadAsc().stream()
                .map(this::mapToHRSGHeatRateDTO)
                .toList();
    }

    /**
     * Get HRSG Heat Rate lookup data for a specific HRSG by equipment name
     */
    public List<HRSGHeatRateLookupDTO> getHRSGHeatRateByEquipmentName(String equipmentName) {
        return hrsgHeatRateLookupRepository.findByEquipmentNameOrderByHrsgLoadAsc(equipmentName).stream()
                .map(this::mapToHRSGHeatRateDTO)
                .toList();
    }

    /**
     * Get HRSG Heat Rate lookup data for a specific HRSG by CPPUtility (AssetId)
     */
    public List<HRSGHeatRateLookupDTO> getHRSGHeatRateByCppUtility(String cppUtility) {
        return hrsgHeatRateLookupRepository.findByCppUtilityOrderByHrsgLoadAsc(cppUtility).stream()
                .map(this::mapToHRSGHeatRateDTO)
                .toList();
    }

    private HRSGHeatRateLookupDTO mapToHRSGHeatRateDTO(HRSGHeatRateLookup entity) {
        return HRSGHeatRateLookupDTO.builder()
                .id(entity.getId())
                .equipmentName(entity.getEquipmentName())
                .cppUtility(entity.getCppUtility())
                .hrsgLoad(entity.getHrsgLoad())
                .heatRate(entity.getHeatRate())
                .remarks(entity.getRemarks())
                .build();
    }


    // ============== Update Methods ====================

    public void updateHeatRate(List<HeatRateDTO> heatRateDTOs) {
        logger.info("========== SERVICE: updateHeatRate ==========");
        logger.info("Received {} records to update", heatRateDTOs != null ? heatRateDTOs.size() : 0);
        
        List<Object[]> updates = new ArrayList<>();
        for(HeatRateDTO heatRateDTO : heatRateDTOs) {
            // Validate selectedHeatRate field
            String selectedHeatRate = heatRateDTO.getSelectedHeatRate();
            if (selectedHeatRate != null && !selectedHeatRate.trim().isEmpty()) {
                if (!SelectedHeatRateType.isValid(selectedHeatRate)) {
                    logger.error("Invalid selectedHeatRate value: '{}' for ID: {}. Must be one of: OEM, PREVIOUS_YEAR, PROPOSED, OTHER", 
                        selectedHeatRate, heatRateDTO.getId());
                    throw new IllegalArgumentException(
                        String.format("Invalid selectedHeatRate value: '%s'. Must be one of: OEM, PREVIOUS_YEAR, PROPOSED, OTHER", 
                        selectedHeatRate));
                }
            } else {
                // Set default value to PROPOSED if null or empty
                selectedHeatRate = SelectedHeatRateType.PROPOSED.getValue();
                heatRateDTO.setSelectedHeatRate(selectedHeatRate);
                logger.debug("SelectedHeatRate was null/empty for ID: {}, setting default value: PROPOSED", heatRateDTO.getId());
            }
            
            logger.debug("Preparing update for ID: {}, GTLoad: {}, HeatRate: {}, FinalHeatRate: {}, OEMHeatRate: {}, SelectedHeatRate: {}", 
                heatRateDTO.getId(), heatRateDTO.getGtLoad(), heatRateDTO.getHeatRate(), 
                heatRateDTO.getFinalHeatRate(), heatRateDTO.getOemHeatRate(), selectedHeatRate);
            updates.add(new Object[] { 
                heatRateDTO.getGtLoad(), 
                heatRateDTO.getFreeSteamFactor(), 
                heatRateDTO.getFinalHeatRate(), 
                heatRateDTO.getOemHeatRate(),
                selectedHeatRate,
                heatRateDTO.getRemarks(), 
                heatRateDTO.getId() 
            });
        }
        
        if(updates.size() > 0) {
            String sql = "update CPP_GTHeatRate set GTLoad = ?, FreeSteamFactor = ?, FinalHeatRate = ?, OEMHeatRate = ?, SelectedHeatRate = ?, Remarks = ?, UpdatedDate = GETDATE() WHERE Id = ?";
            logger.info("Executing batch update SQL: {}", sql);
            logger.info("Updating {} records in database", updates.size());
            
            int[] updateCounts = jdbcTemplate.batchUpdate(sql, updates);
            logger.info("Batch update completed. {} records updated", updateCounts.length);
        } else {
            logger.warn("No records to update");
        }
        logger.info("=============================================");
    }

    public void updateHRSGHeatRateLookup(List<HRSGHeatRateLookupDTO> hrsgHeatRateLookupDTOs) {
    List<Object[]> updates = new ArrayList<>();
    for(HRSGHeatRateLookupDTO hrsgHeatRateLookupDTO : hrsgHeatRateLookupDTOs) {
        updates.add(new Object[] { hrsgHeatRateLookupDTO.getHrsgLoad(), hrsgHeatRateLookupDTO.getHeatRate(), hrsgHeatRateLookupDTO.getRemarks(), hrsgHeatRateLookupDTO.getId() });
    }
    if(updates.size() > 0) {
        String sql = "update HRSGHeatRateLookup set  HRSGLoad = ?, HeatRate = ?, Remarks = ? where Id = ?";
        jdbcTemplate.batchUpdate(sql, updates);
    }
}


public void updateSTGExtraction(List<STGExtractionLookupDTO> stgExtractionLookupDTOs) {
    List<Object[]> updates = new ArrayList<>();
    for(STGExtractionLookupDTO stgExtractionLookupDTO : stgExtractionLookupDTOs) {
        updates.add(new Object[] { stgExtractionLookupDTO.getLoadMW(), stgExtractionLookupDTO.getSvhInletTPH(), stgExtractionLookupDTO.getSmBleedFlowTPH(), stgExtractionLookupDTO.getSlExtFlowTPH(), stgExtractionLookupDTO.getCondensingLoadM3Hr(), stgExtractionLookupDTO.getHeatRateKcalKWH(), stgExtractionLookupDTO.getRemarks(), stgExtractionLookupDTO.getId() });
    }
    if(updates.size() > 0) {
        String sql = "update STGExtractionLookup set LoadMW = ?, SVHInletTPH = ?, SMBleedFlowTPH = ?, SLExtFlowTPH = ?, CondensingLoadM3Hr = ?, HeatRateKcalKWH = ?, Remarks = ? where Id = ?";
        jdbcTemplate.batchUpdate(sql, updates);
    }
}

// ============================================================
// EXPORT METHODS
// ============================================================

/**
 * Export HRSG Heat Rate Lookup data to Excel
 */
public byte[] exportHRSGHeatRateLookup() throws IOException {
    List<HRSGHeatRateLookupDTO> data = getHRSGHeatRateLookup();
    
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("HRSG Heat Rate Lookup");
    CellStyle headerStyle = createHeaderStyle(workbook);
    CellStyle dataStyle = createDataStyle(workbook);
    CellStyle remarksStyle = createRemarksStyle(workbook);
    
    int rowNum = 0;
    
    // Create header row
    Row headerRow = sheet.createRow(rowNum++);
    String[] headers = {"Equipment Type", "CPP Utility", "HRSG Load", "Heat Rate", "Remarks", "Id"};
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }
    
    // Hide ID column (index 5)
    sheet.setColumnHidden(5, true);
    
    // Create data rows
    for (HRSGHeatRateLookupDTO dto : data) {
        Row row = sheet.createRow(rowNum++);
        int colNum = 0;
        
        Cell cell = row.createCell(colNum++);
        cell.setCellValue(dto.getEquipmentName() != null ? dto.getEquipmentName() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getCppUtility() != null ? dto.getCppUtility() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getHrsgLoad() != null ? dto.getHrsgLoad().doubleValue() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getHeatRate() != null ? dto.getHeatRate().doubleValue() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
        cell.setCellStyle(remarksStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
        cell.setCellStyle(dataStyle);
    }
    
    // Auto-size columns (header + content aware)
    for (int i = 0; i < headers.length; i++) {
        if (i == 4) {
            sheet.setColumnWidth(i, 8000);
            continue;
        }
        sheet.autoSizeColumn(i);
        applyHeaderMinWidth(sheet, i, headers[i]);
    }
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    
    return outputStream.toByteArray();
}

/**
 * Export STG Extraction Lookup data to Excel
 */
public byte[] exportSTGExtractionLookup() throws IOException {
    List<STGExtractionLookupDTO> data = getSTGExtractionLookup();
    
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("STG Extraction Lookup");
    CellStyle headerStyle = createHeaderStyle(workbook);
    CellStyle dataStyle = createDataStyle(workbook);
    CellStyle remarksStyle = createRemarksStyle(workbook);
    
    int rowNum = 0;
    
    // Create header row
    Row headerRow = sheet.createRow(rowNum++);
    String[] headers = {"Load (MW)", "SVH Inlet (TPH)", "SM Bleed Flow (TPH)", "SL Ext Flow (TPH)", 
                       "Condensing Load (M3/Hr)", "Heat Rate (Kcal/KWH)", "Remarks", "Id"};
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }
    
    // Hide ID column (index 7)
    sheet.setColumnHidden(7, true);
    
    // Create data rows
    for (STGExtractionLookupDTO dto : data) {
        Row row = sheet.createRow(rowNum++);
        int colNum = 0;
        
        Cell cell = row.createCell(colNum++);
        cell.setCellValue(dto.getLoadMW() != null ? dto.getLoadMW().doubleValue() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getSvhInletTPH() != null ? dto.getSvhInletTPH().doubleValue() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getSmBleedFlowTPH() != null ? dto.getSmBleedFlowTPH().doubleValue() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getSlExtFlowTPH() != null ? dto.getSlExtFlowTPH().doubleValue() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getCondensingLoadM3Hr() != null ? dto.getCondensingLoadM3Hr().doubleValue() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getHeatRateKcalKWH() != null ? dto.getHeatRateKcalKWH().doubleValue() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
        cell.setCellStyle(remarksStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
        cell.setCellStyle(dataStyle);
    }
    
    // Auto-size columns (header + content aware)
    for (int i = 0; i < headers.length; i++) {
        if (i == 6) {
            sheet.setColumnWidth(i, 8000);
            continue;
        }
        sheet.autoSizeColumn(i);
        applyHeaderMinWidth(sheet, i, headers[i]);
    }
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    
    return outputStream.toByteArray();
}

/**
 * Export Heat Rate data to Excel for a specific asset
 * @param assetId Asset ID
 * @param financialYear Financial year
 * @param startDate Optional start date for proposed heat rate calculation
 * @param endDate Optional end date for proposed heat rate calculation
 */
public byte[] exportHeatRate(String assetId, String financialYear, String startDate, String endDate) throws IOException {
    List<HeatRateDTO> data;
    
    // If date range is provided, get data with proposed heat rates
    if (startDate != null && !startDate.trim().isEmpty() && endDate != null && !endDate.trim().isEmpty()) {
        logger.info("Exporting with proposed heat rates for date range: {} to {}", startDate, endDate);
        data = getHeatRateByAssetIdWithProposed(assetId, financialYear, startDate, endDate);
    } else {
        logger.info("Exporting without proposed heat rates");
        data = getHeatRateByAssetId(assetId, financialYear);
    }
    
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Heat Rate");
    CellStyle headerStyle = createHeaderStyle(workbook);
    CellStyle dataStyle = createDataStyle(workbook);
    CellStyle remarksStyle = createRemarksStyle(workbook);
    
    int rowNum = 0;
    
    // Create header row - matching UI column order and naming
    Row headerRow = sheet.createRow(rowNum++);
    String[] headers = {"Equipment Type", "CPP Utility", "GT Load", "OEM HR", "PREVIOUS YEAR BUDGET HR", "PROPOSED HR (Based On Actual Data)", "Final HR", "Free Steam Factor", "Remark", "Selected Heat Rate", "Id"};
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }
    
    // Hide Selected Heat Rate column (index 9) and ID column (index 10)
    sheet.setColumnHidden(9, true);
    sheet.setColumnHidden(10, true);
    
    // Create data rows
    for (HeatRateDTO dto : data) {
        Row row = sheet.createRow(rowNum++);
        int colNum = 0;
        
        // Column order matching UI: Equipment Type, CPP Utility, GT Load, OEM HR, Previous Year Budget HR, Proposed HR, Final HR, Free Steam Factor, Remark, Selected Heat Rate (hidden), Id (hidden)
        Cell cell = row.createCell(colNum++);
        cell.setCellValue(dto.getEquipType() != null ? dto.getEquipType() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getCppUtility() != null ? dto.getCppUtility() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getGtLoad() != null ? dto.getGtLoad() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getOemHeatRate() != null ? dto.getOemHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getPreviousYearHeatRate() != null ? dto.getPreviousYearHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getProposedHeatRate() != null ? dto.getProposedHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getFinalHeatRate() != null ? dto.getFinalHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getFreeSteamFactor() != null ? dto.getFreeSteamFactor() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
        cell.setCellStyle(remarksStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getSelectedHeatRate() != null ? dto.getSelectedHeatRate() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
        cell.setCellStyle(dataStyle);
    }
    
    // Auto-size columns (header + content aware)
    for (int i = 0; i < headers.length; i++) {
        if (i == 8) { // Remark column (now at index 8)
            sheet.setColumnWidth(i, 8000);
            continue;
        }
        sheet.autoSizeColumn(i);
        applyHeaderMinWidth(sheet, i, headers[i]);
    }
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    
    return outputStream.toByteArray();
}

// ============================================================
// IMPORT METHODS
// ============================================================

/**
 * Import HRSG Heat Rate Lookup data from Excel
 */
public void importHRSGHeatRateLookup(MultipartFile file) throws IOException {

   
    
    List<HRSGHeatRateLookupDTO> dtos = new ArrayList<>();
    
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        int totalRows = sheet.getLastRowNum();
        
        // Start from row 1 (skip header row 0)
        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            HRSGHeatRateLookupDTO dto = new HRSGHeatRateLookupDTO();
            
            // Read ID from hidden column (index 5)
            String idStr = getCellValueAsString(row, 5);
            if (idStr != null && !idStr.isEmpty()) {
                dto.setId(UUID.fromString(idStr));
            }
            
            dto.setEquipmentName(getCellValueAsString(row, 0));
            dto.setCppUtility(getCellValueAsString(row, 1));
            dto.setHrsgLoad(getCellValueAsBigDecimal(row, 2));
            dto.setHeatRate(getCellValueAsBigDecimal(row, 3));
            dto.setRemarks(getCellValueAsString(row, 4));
            
            dtos.add(dto);
        }
    }
    
    if (!dtos.isEmpty()) {
        updateHRSGHeatRateLookup(dtos);
    }
}

/**
 * Import STG Extraction Lookup data from Excel
 */
public void importSTGExtractionLookup(MultipartFile file) throws IOException {
    List<STGExtractionLookupDTO> dtos = new ArrayList<>();
    
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        int totalRows = sheet.getLastRowNum();
        
        // Start from row 1 (skip header row 0)
        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            STGExtractionLookupDTO dto = new STGExtractionLookupDTO();
            
            // Read ID from hidden column (index 7)
            String idStr = getCellValueAsString(row, 7);
            if (idStr != null && !idStr.isEmpty()) {
                dto.setId(UUID.fromString(idStr));
            }
            
            dto.setLoadMW(getCellValueAsBigDecimal(row, 0));
            dto.setSvhInletTPH(getCellValueAsBigDecimal(row, 1));
            dto.setSmBleedFlowTPH(getCellValueAsBigDecimal(row, 2));
            dto.setSlExtFlowTPH(getCellValueAsBigDecimal(row, 3));
            dto.setCondensingLoadM3Hr(getCellValueAsBigDecimal(row, 4));
            dto.setHeatRateKcalKWH(getCellValueAsBigDecimal(row, 5));
            dto.setRemarks(getCellValueAsString(row, 6));
            
            dtos.add(dto);
        }
    }
    
    if (!dtos.isEmpty()) {
        updateSTGExtraction(dtos);
    }
}

/**
 * Import Heat Rate data from Excel
 */
public void importHeatRate(MultipartFile file) throws IOException {

    System.out.println("Importing Heat Rate");
    List<HeatRateDTO> dtos = new ArrayList<>();
    
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        int totalRows = sheet.getLastRowNum();
        
        // Start from row 1 (skip header row 0)
        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            HeatRateDTO dto = new HeatRateDTO();
            
            // Column order: Equipment Type, CPP Utility, GT Load, OEM HR, PREVIOUS YEAR BUDGET HR, PROPOSED HR, Final HR, Free Steam Factor, Remark, Selected Heat Rate (hidden), Id (hidden)
            
            // Read ID from hidden column (index 10)
            String idStr = getCellValueAsString(row, 10);
            if (idStr != null && !idStr.isEmpty()) {
                dto.setId(UUID.fromString(idStr));
            }
            
            dto.setEquipType(getCellValueAsString(row, 0));           // Equipment Type
            dto.setCppUtility(getCellValueAsString(row, 1));          // CPP Utility
            dto.setGtLoad(getCellValueAsDouble(row, 2));              // GT Load
            dto.setOemHeatRate(getCellValueAsDouble(row, 3));         // OEM HR
            dto.setPreviousYearHeatRate(getCellValueAsDouble(row, 4)); // PREVIOUS YEAR BUDGET HR
            // Skip index 5 - proposedHeatRate (PROPOSED HR - calculated, not imported)
            dto.setFinalHeatRate(getCellValueAsDouble(row, 6));       // Final HR
            dto.setFreeSteamFactor(getCellValueAsDouble(row, 7));     // Free Steam Factor
            dto.setRemarks(getCellValueAsString(row, 8));             // Remark
            dto.setSelectedHeatRate(getCellValueAsString(row, 9));    // Selected Heat Rate (hidden)
            
            dtos.add(dto);
        }
    }
    
    if (!dtos.isEmpty()) {
        updateHeatRate(dtos);
    }
}

/**
 * Export HRSG Heat Rate data to Excel for a specific asset
 * @param assetId Asset ID
 * @param financialYear Financial year
 * @param startDate Optional start date for proposed heat rate calculation
 * @param endDate Optional end date for proposed heat rate calculation
 */
public byte[] exportHRSGHeatRate(String assetId, String financialYear, String startDate, String endDate) throws IOException {
    logger.info("========== EXPORT HRSG HEAT RATE ==========");
    logger.info("Parameters - assetId: {}, financialYear: {}, startDate: {}, endDate: {}", assetId, financialYear, startDate, endDate);
    
    List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO> data;
    
    // If date range is provided, get data with proposed heat rates
    if (startDate != null && !startDate.trim().isEmpty() && endDate != null && !endDate.trim().isEmpty()) {
        logger.info("Exporting with proposed heat rates for date range: {} to {}", startDate, endDate);
        data = getHRSGHeatRateByAssetIdWithProposed(assetId, financialYear, startDate, endDate);
    } else {
        logger.info("Exporting without proposed heat rates");
        data = getHRSGHeatRateByAssetId(assetId, financialYear);
    }
    
    logger.info("Retrieved {} HRSG heat rate records for export", data.size());
    
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("HRSG Heat Rate");
    CellStyle headerStyle = createHeaderStyle(workbook);
    CellStyle dataStyle = createDataStyle(workbook);
    CellStyle remarksStyle = createRemarksStyle(workbook);
    
    int rowNum = 0;
    
    // Create header row - matching UI column order and naming
    Row headerRow = sheet.createRow(rowNum++);
    String[] headers = {"Equipment Type", "CPP Utility", "HRSG Load", "OEM HR", "PREVIOUS YEAR BUDGET HR", "PROPOSED HR (Based On Actual Data)", "Final HR", "Remark", "Selected Heat Rate", "Id"};
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }
    
    // Hide Selected Heat Rate column (index 8) and ID column (index 9)
    sheet.setColumnHidden(8, true);
    sheet.setColumnHidden(9, true);
    
    logger.info("Creating {} data rows in Excel", data.size());
    
    // Create data rows
    for (com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO dto : data) {
        Row row = sheet.createRow(rowNum++);
        int colNum = 0;
        
        // Column order matching UI: Equipment Type, CPP Utility, HRSG Load, OEM HR, Previous Year Budget HR, Proposed HR, Final HR, Remark, Selected Heat Rate (hidden), Id (hidden)
        Cell cell = row.createCell(colNum++);
        cell.setCellValue(dto.getEquipType() != null ? dto.getEquipType() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getCppUtility() != null ? dto.getCppUtility() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getHrsgLoad() != null ? dto.getHrsgLoad() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getOemHeatRate() != null ? dto.getOemHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getPreviousYearHeatRate() != null ? dto.getPreviousYearHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getProposedHeatRate() != null ? dto.getProposedHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getFinalHeatRate() != null ? dto.getFinalHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
        cell.setCellStyle(remarksStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getSelectedHeatRate() != null ? dto.getSelectedHeatRate() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
        cell.setCellStyle(dataStyle);
    }
    
    // Auto-size columns (header + content aware)
    for (int i = 0; i < headers.length; i++) {
        if (i == 7) { // Remark column (now at index 7)
            sheet.setColumnWidth(i, 8000);
            continue;
        }
        sheet.autoSizeColumn(i);
        applyHeaderMinWidth(sheet, i, headers[i]);
    }
    
    logger.info("Excel file created successfully with {} rows", rowNum);
    
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    
    logger.info("========== EXPORT COMPLETE ==========");
    
    return outputStream.toByteArray();
}

/**
 * Import HRSG Heat Rate data from Excel
 */
public void importHRSGHeatRate(MultipartFile file) throws IOException {
    logger.info("========== IMPORT HRSG HEAT RATE ==========");
    logger.info("File name: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
    
    List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO> dtos = new ArrayList<>();
    
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        int totalRows = sheet.getLastRowNum();
        logger.info("Processing {} rows from Excel", totalRows);
        
        // Start from row 1 (skip header row 0)
        for (int i = 1; i <= totalRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO dto = new com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateDTO();
            
            // Column order: Equipment Type, CPP Utility, HRSG Load, OEM HR, PREVIOUS YEAR BUDGET HR, PROPOSED HR, Final HR, Remark, Selected Heat Rate (hidden), Id (hidden)
            
            // Read ID from hidden column (index 9)
            String idStr = getCellValueAsString(row, 9);
            if (idStr != null && !idStr.isEmpty()) {
                dto.setId(UUID.fromString(idStr));
            }
            
            dto.setEquipType(getCellValueAsString(row, 0));           // Equipment Type
            dto.setCppUtility(getCellValueAsString(row, 1));          // CPP Utility
            dto.setHrsgLoad(getCellValueAsDouble(row, 2));            // HRSG Load
            dto.setOemHeatRate(getCellValueAsDouble(row, 3));         // OEM HR
            dto.setPreviousYearHeatRate(getCellValueAsDouble(row, 4)); // PREVIOUS YEAR BUDGET HR
            // Skip index 5 - proposedHeatRate (PROPOSED HR - calculated, not imported)
            dto.setFinalHeatRate(getCellValueAsDouble(row, 6));       // Final HR
            dto.setRemarks(getCellValueAsString(row, 7));             // Remark
            dto.setSelectedHeatRate(getCellValueAsString(row, 8));    // Selected Heat Rate (hidden)
            
            dtos.add(dto);
        }
    }
    
    logger.info("Parsed {} HRSG heat rate records from Excel", dtos.size());
    
    if (!dtos.isEmpty()) {
        logger.info("Updating HRSG heat rate records in database");
        updateHRSGHeatRate(dtos);
        logger.info("HRSG heat rate import completed successfully");
    } else {
        logger.warn("No records found in Excel file to import");
    }
    
    logger.info("========== IMPORT COMPLETE ==========");
}

// ============================================================
// HELPER METHODS
// ============================================================

private String getCellValueAsString(Row row, int cellIndex) {
    if (row.getCell(cellIndex) == null) {
        return null;
    }
    
    try {
        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(row.getCell(cellIndex));
        return value != null && !value.trim().isEmpty() ? value.trim() : null;
    } catch (Exception e) {
        return null;
    }
}

private BigDecimal getCellValueAsBigDecimal(Row row, int cellIndex) {
    if (row.getCell(cellIndex) == null) {
        return null;
    }
    
    try {
        switch (row.getCell(cellIndex).getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(row.getCell(cellIndex).getNumericCellValue());
            case STRING:
                String strValue = row.getCell(cellIndex).getStringCellValue().trim();
                if (strValue.isEmpty()) {
                    return null;
                }
                return new BigDecimal(strValue);
            default:
                return null;
        }
    } catch (Exception e) {
        return null;
    }
}

private Double getCellValueAsDouble(Row row, int cellIndex) {
    if (row.getCell(cellIndex) == null) {
        return null;
    }
    
    try {
        switch (row.getCell(cellIndex).getCellType()) {
            case NUMERIC:
                return row.getCell(cellIndex).getNumericCellValue();
            case STRING:
                String strValue = row.getCell(cellIndex).getStringCellValue().trim();
                if (strValue.isEmpty()) {
                    return null;
                }
                return Double.parseDouble(strValue);
            default:
                return null;
        }
    } catch (Exception e) {
        return null;
    }
}

private CellStyle createHeaderStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);
    return style;
}

private CellStyle createDataStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
}

private CellStyle createRemarksStyle(Workbook workbook) {
    CellStyle style = createDataStyle(workbook);
    style.setWrapText(true);
    return style;
}

private void applyHeaderMinWidth(Sheet sheet, int col, String headerText) {
    if (headerText == null || headerText.isBlank()) {
        return;
    }
    int headerWidth = Math.min(255 * 256, (headerText.length() + 2) * 256);
    if (sheet.getColumnWidth(col) < headerWidth) {
        sheet.setColumnWidth(col, headerWidth);
    }
}
}


