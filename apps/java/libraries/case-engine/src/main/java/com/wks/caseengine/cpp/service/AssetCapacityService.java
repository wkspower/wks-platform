package com.wks.caseengine.cpp.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AssetUtilityDTO;
import com.wks.caseengine.cpp.dto.MonthCapacityDto;
import com.wks.caseengine.cpp.dto.AssetCapacityDTO;
import com.wks.caseengine.cpp.dto.AssetCapacityProjection;
import com.wks.caseengine.cpp.repository.AssetCapacityRepository;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.FinancialYearMonthRepository;

@Service
public class AssetCapacityService {
     
    @Autowired
    private AssetCapacityRepository assetCapacityRepo;

    @Autowired
    private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<AssetCapacityDTO> getAssetCapacityByCppAndFY(String cppId, String financialYear) {
        List<AssetCapacityProjection> projections = assetCapacityRepo.getAssetAvailabilityByCPPAndFY(UUID.fromString(cppId), financialYear);
        
        // Convert projections to DTOs
        List<AssetCapacityDTO> dtos = projections.stream().map(proj -> {
            AssetCapacityDTO dto = new AssetCapacityDTO();
           // System.out.println("Projection.getAssetId(): " + proj.getAssetId());
            dto.setAssetId(proj.getAssetId().toString().toUpperCase());
            dto.setAssetName(proj.getAssetName());
            dto.setPlantCode(proj.getPlantCode());
            dto.setUom(proj.getUOM());
            dto.setRemarks(proj.getRemarks());
            dto.setFixedMin(proj.getFixedMin());
            dto.setFixedMax(proj.getFixedMax());
            // Set utility details
            AssetUtilityDTO utilityDistributed = new AssetUtilityDTO();
            utilityDistributed.setName(proj.getUtilityDistributedName());
            utilityDistributed.setSapCode(proj.getUtilityDistributedSAP());
            dto.setUtilityDistributed(utilityDistributed);

            AssetUtilityDTO utilityGenerated = new AssetUtilityDTO();
            utilityGenerated.setName(proj.getUtilityGeneratedName());
            utilityGenerated.setSapCode(proj.getUtilityGeneratedSAP());
            dto.setUtilityGenerated(utilityGenerated);

            dto.setRemarks(proj.getRemarks());

            System.out.println("april min: " + proj.getAprMinCapacity() + ", april max: " + proj.getAprMaxCapacity());

            dto.setApril(new MonthCapacityDto(proj.getAprMinCapacity(), proj.getAprMaxCapacity()));
            dto.setMay(new MonthCapacityDto(proj.getMayMinCapacity(), proj.getMayMaxCapacity()));
            dto.setJune(new MonthCapacityDto(proj.getJunMinCapacity(), proj.getJunMaxCapacity()));
            dto.setJuly(new MonthCapacityDto(proj.getJulMinCapacity(), proj.getJulMaxCapacity()));
            dto.setAug(new MonthCapacityDto(proj.getAugMinCapacity(), proj.getAugMaxCapacity()));
            dto.setSep(new MonthCapacityDto(proj.getSepMinCapacity(), proj.getSepMaxCapacity()));
            dto.setOct(new MonthCapacityDto(proj.getOctMinCapacity(), proj.getOctMaxCapacity()));
            dto.setNov(new MonthCapacityDto(proj.getNovMinCapacity(), proj.getNovMaxCapacity()));
            dto.setDec(new MonthCapacityDto(proj.getDecMinCapacity(), proj.getDecMaxCapacity()));
            dto.setJan(new MonthCapacityDto(proj.getJanMinCapacity(), proj.getJanMaxCapacity()));
            dto.setFeb(new MonthCapacityDto(proj.getFebMinCapacity(), proj.getFebMaxCapacity()));
            dto.setMarch(new MonthCapacityDto(proj.getMarMinCapacity(), proj.getMarMaxCapacity()));
             System.out.println("DTO Created: " + dto);
            return dto;
        }).collect(Collectors.toList());

        return dtos;
    }

    public void updateAssetCapacities(List<AssetCapacityDTO> assetCapacities, String financialYear) {

          if (assetCapacities == null || assetCapacities.isEmpty() || financialYear == null || financialYear.isBlank()) 
            return;

           int startYear = Integer.parseInt(financialYear.substring(0, 4));
           int endYear = startYear + 1;

           // get FinancialYearMonthIds from april of startYear to march of endYear
            Map<Integer, UUID> financialMonthIds = new LinkedHashMap<>();
            List<Object[]> fyMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
          for (Object[] row : fyMonths) {
            Integer month = (Integer) row[0];
            UUID id = UUID.fromString((String) row[1]);
            financialMonthIds.put(month, id);
        }

        // get all AssetCapacity for given AssetIds and FinancialYearMonthIds. data to determine whether to insert or update
         Set<UUID> assetIds = assetCapacities.stream()
                .map(asset -> UUID.fromString(asset.getAssetId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Object[]> existingCapacities = assetCapacityRepo.getAssetCapacitiesByAssetsAndFYMonths(assetIds, financialMonthIds.values());

           Set<String> existingKeys = existingCapacities.stream()
                .map(row -> row[0].toString() + "-" + row[1].toString()) // AssetId-FinancialYearMonthId
                .collect(Collectors.toSet());




                

          List<Object[]> updates = new ArrayList<>();
          List<Object[]> inserts = new ArrayList<>();
          List<Object[]> updatesRemarks = new ArrayList<>();

            for (AssetCapacityDTO asset : assetCapacities) {
                UUID assetId = UUID.fromString(asset.getAssetId());
                
                // update remarks logic
                List<Object[]> existingCapacitiesForAsset =  assetCapacityRepo.getAssetCapacitiesByAssetsAndFYMonths( List.of(assetId), financialMonthIds.values());

                Set<UUID> existingFinancialYearMonthIds = existingCapacitiesForAsset.stream()
                .map(row -> UUID.fromString((String) row[1]))
                .collect(Collectors.toSet());

              //  updatesRemarks.add(new Object[] { asset.getRemarks(), assetId, existingFinancialYearMonthIds });
              for(UUID fymId : existingFinancialYearMonthIds) {
                updatesRemarks.add(new Object[] { asset.getRemarks(), assetId, fymId });
              }
                


                double fixedMax = asset.getFixedMax() != null ? asset.getFixedMax() : 0.0;
                double fixedMin = asset.getFixedMin() != null ? asset.getFixedMin() : 0.0;

                if (assetId == null) continue;
              
                updatesRemarks.add(new Object[] { asset.getRemarks(), assetId });
              
                if(asset.getApril() != null) {
                  
                    UUID fymId = financialMonthIds.get(4);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                   
                    if (existingKeys.contains(key)) {
                       
                        updates.add(new Object[] { asset.getApril().getMin(), asset.getApril().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                      
                        inserts.add(new Object[] { assetId, fymId, asset.getApril().getMin(), asset.getApril().getMax(), fixedMin, fixedMax });
                    }
                }
                if(asset.getMay() != null) {
                  
                    UUID fymId = financialMonthIds.get(5);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                
                    if (existingKeys.contains(key)) {
                        
                        updates.add(new Object[] { asset.getMay().getMin(), asset.getMay().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                       
                        inserts.add(new Object[] { assetId, fymId, asset.getMay().getMin(), asset.getMay().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getJune() != null) {
                    UUID fymId = financialMonthIds.get(6);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getJune().getMin(), asset.getJune().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getJune().getMin(), asset.getJune().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getJuly() != null) {
                    UUID fymId = financialMonthIds.get(7);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getJuly().getMin(), asset.getJuly().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getJuly().getMin(), asset.getJuly().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getAug() != null) {
                    UUID fymId = financialMonthIds.get(8);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getAug().getMin(), asset.getAug().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getAug().getMin(), asset.getAug().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getSep() != null) {
                    UUID fymId = financialMonthIds.get(9);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getSep().getMin(), asset.getSep().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getSep().getMin(), asset.getSep().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getOct() != null) {
                    UUID fymId = financialMonthIds.get(10);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getOct().getMin(), asset.getOct().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getOct().getMin(), asset.getOct().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getNov() != null) {
                    UUID fymId = financialMonthIds.get(11);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getNov().getMin(), asset.getNov().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getNov().getMin(), asset.getNov().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getDec() != null) {
                    UUID fymId = financialMonthIds.get(12);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getDec().getMin(), asset.getDec().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getDec().getMin(), asset.getDec().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getJan() != null) {
                    UUID fymId = financialMonthIds.get(1);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getJan().getMin(), asset.getJan().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getJan().getMin(), asset.getJan().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getFeb() != null) {
                    UUID fymId = financialMonthIds.get(2);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getFeb().getMin(), asset.getFeb().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getFeb().getMin(), asset.getFeb().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getMarch() != null) {
                    UUID fymId = financialMonthIds.get(3);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getMarch().getMin(), asset.getMarch().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getMarch().getMin(), asset.getMarch().getMax(), fixedMin, fixedMax });
                    }
                }

                
        
    }

        // Perform batch updates
        if (!updates.isEmpty()) {
            String updateSql = "UPDATE AssetAvailability SET MinOperatingCapacity = ?, MaxOperatingCapacity = ?, FixedMin = ?, FixedMax = ? WHERE AssetId = ? AND FinancialYearMonthId = ?";
            jdbcTemplate.batchUpdate(updateSql, updates);
        }

        if(!inserts.isEmpty()) {
            String insertSql = "INSERT INTO AssetAvailability ( Id, AssetId, FinancialYearMonthId, MinOperatingCapacity, MaxOperatingCapacity, FixedMin, FixedMax) VALUES (NEWID(), ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.batchUpdate(insertSql, inserts);
        }

        if(!updatesRemarks.isEmpty()) {
            String updateRemarksSql = "UPDATE AssetAvailability SET Remarks = ? WHERE AssetId = ? AND FinancialYearMonthId = ?";
            jdbcTemplate.batchUpdate(updateRemarksSql, updatesRemarks);
        }

}

    public byte[] exportAssetCapacity(String cppId, String financialYear, boolean isAfterSave, List<AssetCapacityDTO> dtoList) {
        try {
            if (!isAfterSave) {
                dtoList = getAssetCapacityByCppAndFY(cppId, financialYear);
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Asset Capacity");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle remarksStyle = createRemarksStyle(workbook);

            String startYearSuffix = financialYear.substring(2, 4);
            String endYearSuffix = financialYear.substring(5, 7);
            
            int currentRow = 0;
            int col = 0;

            // Create top header row (Row 0) with merged cells for months
            Row topHeaderRow = sheet.createRow(currentRow++);
            col = 0;
            
            // Static columns that span both rows
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Asset Name", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Plant Code", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "UOM", headerStyle);
            col++;
            
            // Utility Distributed columns
            createMergedHeaderCell(sheet, topHeaderRow, 0, 0, col, col + 1, "Utility Distributed", headerStyle);
            col += 2;
            
            // Utility Generated columns
            createMergedHeaderCell(sheet, topHeaderRow, 0, 0, col, col + 1, "Utility Generated", headerStyle);
            col += 2;
            
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Fixed Min", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Fixed Max", headerStyle);
            col++;
            
            // Month headers (each spans 2 columns for Min and Max)
            String[] months = {"Apr-" + startYearSuffix, "May-" + startYearSuffix, "Jun-" + startYearSuffix, "Jul-" + startYearSuffix,
                    "Aug-" + startYearSuffix, "Sep-" + startYearSuffix, "Oct-" + startYearSuffix, "Nov-" + startYearSuffix,
                    "Dec-" + startYearSuffix, "Jan-" + endYearSuffix, "Feb-" + endYearSuffix, "Mar-" + endYearSuffix};
            
            int monthStartCol = col;
            for (String month : months) {
                createMergedHeaderCell(sheet, topHeaderRow, 0, 0, col, col + 1, month, headerStyle);
                col += 2;
            }
            
            int remarksCol = col;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Remarks", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "AssetId", headerStyle);
            int assetIdCol = col;
            col++;
            
            if (isAfterSave) {
                createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Status", headerStyle);
                col++;
                createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Error Description", headerStyle);
                col++;
            }
            int totalColumns = col;
            
            // Create sub-header row (Row 1) for Min/Max capacity under each month
            Row subHeaderRow = sheet.createRow(currentRow++);
            col = 3; // Start after Asset Name, Plant Code, UOM
            
            // Utility Distributed sub-headers
            Cell cell = subHeaderRow.createCell(col++);
            cell.setCellValue("Name");
            cell.setCellStyle(headerStyle);
            
            cell = subHeaderRow.createCell(col++);
            cell.setCellValue("SAP Code");
            cell.setCellStyle(headerStyle);
            
            // Utility Generated sub-headers
            cell = subHeaderRow.createCell(col++);
            cell.setCellValue("Name");
            cell.setCellStyle(headerStyle);
            
            cell = subHeaderRow.createCell(col++);
            cell.setCellValue("SAP Code");
            cell.setCellStyle(headerStyle);
            
            col = monthStartCol; // Skip Fixed Min and Fixed Max (already set)
            
            // Sub-headers for each month (Min Capacity, Max Capacity)
            for (int i = 0; i < 12; i++) {
                cell = subHeaderRow.createCell(col++);
                cell.setCellValue("Min Capacity");
                cell.setCellStyle(headerStyle);
                
                cell = subHeaderRow.createCell(col++);
                cell.setCellValue("Max Capacity");
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (AssetCapacityDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                col = 0;

                cell = row.createCell(col++);
                cell.setCellValue(dto.getAssetName() != null ? dto.getAssetName() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getPlantCode() != null ? dto.getPlantCode() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUom() != null ? dto.getUom() : "");
                cell.setCellStyle(dataStyle);
                
                // Utility Distributed
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUtilityDistributed() != null && dto.getUtilityDistributed().getName() != null 
                    ? dto.getUtilityDistributed().getName() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUtilityDistributed() != null && dto.getUtilityDistributed().getSapCode() != null 
                    ? dto.getUtilityDistributed().getSapCode() : "");
                cell.setCellStyle(dataStyle);
                
                // Utility Generated
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUtilityGenerated() != null && dto.getUtilityGenerated().getName() != null 
                    ? dto.getUtilityGenerated().getName() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUtilityGenerated() != null && dto.getUtilityGenerated().getSapCode() != null 
                    ? dto.getUtilityGenerated().getSapCode() : "");
                cell.setCellStyle(dataStyle);
                
                setDoubleCellValue(row.createCell(col++), dto.getFixedMin(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getFixedMax(), dataStyle);
                
                // April
                setDoubleCellValue(row.createCell(col++), dto.getApril() != null ? dto.getApril().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getApril() != null ? dto.getApril().getMax() : null, dataStyle);
                // May
                setDoubleCellValue(row.createCell(col++), dto.getMay() != null ? dto.getMay().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getMay() != null ? dto.getMay().getMax() : null, dataStyle);
                // June
                setDoubleCellValue(row.createCell(col++), dto.getJune() != null ? dto.getJune().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getJune() != null ? dto.getJune().getMax() : null, dataStyle);
                // July
                setDoubleCellValue(row.createCell(col++), dto.getJuly() != null ? dto.getJuly().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getJuly() != null ? dto.getJuly().getMax() : null, dataStyle);
                // August
                setDoubleCellValue(row.createCell(col++), dto.getAug() != null ? dto.getAug().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getAug() != null ? dto.getAug().getMax() : null, dataStyle);
                // September
                setDoubleCellValue(row.createCell(col++), dto.getSep() != null ? dto.getSep().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getSep() != null ? dto.getSep().getMax() : null, dataStyle);
                // October
                setDoubleCellValue(row.createCell(col++), dto.getOct() != null ? dto.getOct().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getOct() != null ? dto.getOct().getMax() : null, dataStyle);
                // November
                setDoubleCellValue(row.createCell(col++), dto.getNov() != null ? dto.getNov().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getNov() != null ? dto.getNov().getMax() : null, dataStyle);
                // December
                setDoubleCellValue(row.createCell(col++), dto.getDec() != null ? dto.getDec().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getDec() != null ? dto.getDec().getMax() : null, dataStyle);
                // January
                setDoubleCellValue(row.createCell(col++), dto.getJan() != null ? dto.getJan().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getJan() != null ? dto.getJan().getMax() : null, dataStyle);
                // February
                setDoubleCellValue(row.createCell(col++), dto.getFeb() != null ? dto.getFeb().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getFeb() != null ? dto.getFeb().getMax() : null, dataStyle);
                // March
                setDoubleCellValue(row.createCell(col++), dto.getMarch() != null ? dto.getMarch().getMin() : null, dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getMarch() != null ? dto.getMarch().getMax() : null, dataStyle);
                
                cell = row.createCell(col++);
                cell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                cell.setCellStyle(remarksStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getAssetId() != null ? dto.getAssetId() : "");
                cell.setCellStyle(dataStyle);

                if (isAfterSave) {
                    cell = row.createCell(col++);
                    cell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                    cell.setCellStyle(dataStyle);
                    cell = row.createCell(col++);
                    cell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                    cell.setCellStyle(dataStyle);
                }
            }

            // Hide AssetId column (now at column index 33)
            sheet.setColumnHidden(assetIdCol, true);

            for (int i = 0; i < totalColumns; i++) {
                if (i == remarksCol) {
                    sheet.setColumnWidth(i, 8000);
                    continue;
                }
                sheet.autoSizeColumn(i);
                String headerText = getHeaderText(sheet, i);
                applyHeaderMinWidth(sheet, i, headerText);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AOPMessageVM importExcel(String cppId, String financialYear, MultipartFile file) {
        try {
            List<AssetCapacityDTO> data = readAssetCapacity(file.getInputStream(), cppId, financialYear);
            
            // Separate failed records from successful ones
            List<AssetCapacityDTO> validRecords = new ArrayList<>();
            List<AssetCapacityDTO> failedRecords = new ArrayList<>();
            
            for (AssetCapacityDTO dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    updateAssetCapacities(validRecords, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    for (AssetCapacityDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = exportAssetCapacity(cppId, financialYear, true, failedRecords);
                String base64File = Base64.getEncoder().encodeToString(fileByteArray);
                aopMessageVM.setData(base64File);
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved");
            } else {
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("All data has been saved");
            }

            return aopMessageVM;
        } catch (Exception e) {
            e.printStackTrace();
            AOPMessageVM errorVM = new AOPMessageVM();
            errorVM.setCode(500);
            errorVM.setMessage("Error importing file: " + e.getMessage());
            return errorVM;
        }
    }

    private List<AssetCapacityDTO> readAssetCapacity(InputStream inputStream, String cppId, String financialYear) {
        List<AssetCapacityDTO> assetList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip both header rows (top header and sub-header)
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip top header row
            }
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip sub-header row
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AssetCapacityDTO dto = new AssetCapacityDTO();
                
                try {
                    int col = 0;
                    dto.setAssetName(getStringCellValue(row.getCell(col++)));
                    dto.setPlantCode(getStringCellValue(row.getCell(col++)));
                    dto.setUom(getStringCellValue(row.getCell(col++)));
                    
                    // Skip Utility Distributed columns (read-only, not editable)
                    col += 2; // Skip Utility Distributed Name and SAP Code
                    
                    // Skip Utility Generated columns (read-only, not editable)
                    col += 2; // Skip Utility Generated Name and SAP Code
                    
                    dto.setFixedMin(getDoubleCellValue(row.getCell(col++)));
                    dto.setFixedMax(getDoubleCellValue(row.getCell(col++)));
                    
                    // April
                    Double aprilMin = getDoubleCellValue(row.getCell(col++));
                    Double aprilMax = getDoubleCellValue(row.getCell(col++));
                    dto.setApril(new MonthCapacityDto(aprilMin, aprilMax));
                    
                    // May
                    Double mayMin = getDoubleCellValue(row.getCell(col++));
                    Double mayMax = getDoubleCellValue(row.getCell(col++));
                    dto.setMay(new MonthCapacityDto(mayMin, mayMax));
                    
                    // June
                    Double juneMin = getDoubleCellValue(row.getCell(col++));
                    Double juneMax = getDoubleCellValue(row.getCell(col++));
                    dto.setJune(new MonthCapacityDto(juneMin, juneMax));
                    
                    // July
                    Double julyMin = getDoubleCellValue(row.getCell(col++));
                    Double julyMax = getDoubleCellValue(row.getCell(col++));
                    dto.setJuly(new MonthCapacityDto(julyMin, julyMax));
                    
                    // August
                    Double augMin = getDoubleCellValue(row.getCell(col++));
                    Double augMax = getDoubleCellValue(row.getCell(col++));
                    dto.setAug(new MonthCapacityDto(augMin, augMax));
                    
                    // September
                    Double sepMin = getDoubleCellValue(row.getCell(col++));
                    Double sepMax = getDoubleCellValue(row.getCell(col++));
                    dto.setSep(new MonthCapacityDto(sepMin, sepMax));
                    
                    // October
                    Double octMin = getDoubleCellValue(row.getCell(col++));
                    Double octMax = getDoubleCellValue(row.getCell(col++));
                    dto.setOct(new MonthCapacityDto(octMin, octMax));
                    
                    // November
                    Double novMin = getDoubleCellValue(row.getCell(col++));
                    Double novMax = getDoubleCellValue(row.getCell(col++));
                    dto.setNov(new MonthCapacityDto(novMin, novMax));
                    
                    // December
                    Double decMin = getDoubleCellValue(row.getCell(col++));
                    Double decMax = getDoubleCellValue(row.getCell(col++));
                    dto.setDec(new MonthCapacityDto(decMin, decMax));
                    
                    // January
                    Double janMin = getDoubleCellValue(row.getCell(col++));
                    Double janMax = getDoubleCellValue(row.getCell(col++));
                    dto.setJan(new MonthCapacityDto(janMin, janMax));
                    
                    // February
                    Double febMin = getDoubleCellValue(row.getCell(col++));
                    Double febMax = getDoubleCellValue(row.getCell(col++));
                    dto.setFeb(new MonthCapacityDto(febMin, febMax));
                    
                    // March
                    Double marMin = getDoubleCellValue(row.getCell(col++));
                    Double marMax = getDoubleCellValue(row.getCell(col++));
                    dto.setMarch(new MonthCapacityDto(marMin, marMax));
                    
                    dto.setRemarks(getStringCellValue(row.getCell(col++)));
                    
                    String assetIdStr = getStringCellValue(row.getCell(col++));
                    if (assetIdStr != null && !assetIdStr.isEmpty()) {
                        dto.setAssetId(assetIdStr);
                    } else {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Asset ID is missing");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage());
                }
                
                assetList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return assetList;
    }

    private void setDoubleCellValue(Cell cell, Double value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
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

    private String getHeaderText(Sheet sheet, int col) {
        String subHeader = getCellText(sheet, 1, col);
        if (subHeader != null && !subHeader.isBlank()) {
            return subHeader;
        }
        return getCellText(sheet, 0, col);
    }

    private String getCellText(Sheet sheet, int rowIndex, int col) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return cell.getStringCellValue();
        }
        return null;
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

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            String value;
            if (cell.getCellType() == CellType.NUMERIC) {
                value = String.valueOf((long) cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                value = cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.FORMULA) {
                value = cell.getStringCellValue();
            } else {
                return null;
            }
            
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return value.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private Double getDoubleCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // Return null for invalid numbers
        }
        return null;
    }

    private void createMergedHeaderCell(Sheet sheet, Row row, int rowStart, int rowEnd, 
                                       int colStart, int colEnd, String value, CellStyle style) {
        // Create merged region if needed
        if (rowStart != rowEnd || colStart != colEnd) {
            sheet.addMergedRegion(new CellRangeAddress(rowStart, rowEnd, colStart, colEnd));
        }
        
        // Create cell and set value
        Cell cell = row.createCell(colStart);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        
        // Apply style to all cells in the merged region
        for (int r = rowStart; r <= rowEnd; r++) {
            Row currentRow = sheet.getRow(r);
            if (currentRow == null) {
                currentRow = sheet.createRow(r);
            }
            for (int c = colStart; c <= colEnd; c++) {
                Cell currentCell = currentRow.getCell(c);
                if (currentCell == null) {
                    currentCell = currentRow.createCell(c);
                }
                currentCell.setCellStyle(style);
            }
        }
    }
}
