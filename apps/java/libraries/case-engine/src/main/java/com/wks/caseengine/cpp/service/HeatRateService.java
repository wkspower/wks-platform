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
        logger.info("Calculating proposed heat rates for assetId: {}, dateRange: {} to {}", assetId, startDate, endDate);
        
        // Get asset name from PowerGenerationAssets using displayName column
        String assetName = jdbcTemplate.queryForObject(
            "SELECT displayName FROM PowerGenerationAssets WHERE AssetId = ?",
            String.class,
            assetId
        );
        
        if (assetName == null) {
            logger.warn("Asset not found for assetId: {}", assetId);
            return new java.util.HashMap<>();
        }
        
        logger.info("Asset name (displayName): {}", assetName);
        
        // Call the stored procedure
        String sql = "EXEC CPP_CalculateGTHeatRate_ByDateRange @StartDate = ?, @EndDate = ?, @AssetName = ?";
        
        java.util.Map<Double, Double> proposedHeatRateMap = new java.util.HashMap<>();
        
        try {
            jdbcTemplate.query(sql, 
                (rs) -> {
                    Double gtLoad = rs.getDouble("GTLoad");
                    Double heatRate = rs.getDouble("HeatRate");
                    proposedHeatRateMap.put(gtLoad, heatRate);
                },
                startDate, endDate, assetName
            );
            
            logger.info("Proposed heat rates calculated for {} load points", proposedHeatRateMap.size());
        } catch (Exception e) {
            logger.error("Error calculating proposed heat rates: {}", e.getMessage(), e);
        }
        
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

    public void updateHRSGHeatRate(List<HRSGHeatRateLookupDTO> hrsgHeatRateLookupDTOs) {
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
 */
public byte[] exportHeatRate(String assetId, String financialYear) throws IOException {
    List<HeatRateDTO> data = getHeatRateByAssetId(assetId, financialYear);
    
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Heat Rate");
    CellStyle headerStyle = createHeaderStyle(workbook);
    CellStyle dataStyle = createDataStyle(workbook);
    CellStyle remarksStyle = createRemarksStyle(workbook);
    
    int rowNum = 0;
    
    // Create header row
    Row headerRow = sheet.createRow(rowNum++);
    String[] headers = {"Equipment Type", "CPP Utility", "GT Load", "Heat Rate", "Previous Year Heat Rate", "Final Heat Rate", "OEM Heat Rate", "Selected Heat Rate", "Free Steam Factor", "Remarks", "Id"};
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }
    
    // Hide ID column (index 10)
    sheet.setColumnHidden(10, true);
    
    // Create data rows
    for (HeatRateDTO dto : data) {
        Row row = sheet.createRow(rowNum++);
        int colNum = 0;
        
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
        cell.setCellValue(dto.getHeatRate() != null ? dto.getHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getPreviousYearHeatRate() != null ? dto.getPreviousYearHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getFinalHeatRate() != null ? dto.getFinalHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getOemHeatRate() != null ? dto.getOemHeatRate() : 0.0);
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getSelectedHeatRate() != null ? dto.getSelectedHeatRate() : "");
        cell.setCellStyle(dataStyle);
        cell = row.createCell(colNum++);
        cell.setCellValue(dto.getFreeSteamFactor() != null ? dto.getFreeSteamFactor() : 0.0);
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
        if (i == 9) { // Remarks column
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
        updateHRSGHeatRate(dtos);
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
            
            // Read ID from hidden column (index 10)
            String idStr = getCellValueAsString(row, 10);
            if (idStr != null && !idStr.isEmpty()) {
                dto.setId(UUID.fromString(idStr));
            }
            
            dto.setEquipType(getCellValueAsString(row, 0));
            dto.setCppUtility(getCellValueAsString(row, 1));
            dto.setGtLoad(getCellValueAsDouble(row, 2));
            dto.setHeatRate(getCellValueAsDouble(row, 3));
            dto.setPreviousYearHeatRate(getCellValueAsDouble(row, 4));
            dto.setFinalHeatRate(getCellValueAsDouble(row, 5));
            dto.setOemHeatRate(getCellValueAsDouble(row, 6));
            dto.setSelectedHeatRate(getCellValueAsString(row, 7));
            dto.setFreeSteamFactor(getCellValueAsDouble(row, 8));
            dto.setRemarks(getCellValueAsString(row, 9));
            
            dtos.add(dto);
        }
    }
    
    if (!dtos.isEmpty()) {
        updateHeatRate(dtos);
    }
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


