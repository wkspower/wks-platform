package com.wks.caseengine.cpp.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.AssetPrioriryDTO;
import com.wks.caseengine.cpp.dto.AssetPriorityProjection;
import com.wks.caseengine.cpp.repository.AssetPriorityRepository;
import com.wks.caseengine.cpp.service.AssetPriorityService;
import com.wks.caseengine.entity.FinancialYearMonth;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.ExistingAssetAvailabilityProjection;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.utility.Utility;

@Service
public class AssetPriorityServiceImpl implements AssetPriorityService {
      
    @Autowired
     private  AssetPriorityRepository assetPriorityRepository;

     @Autowired
     private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

     @Override
    public List<AssetPrioriryDTO>
            getAssetPriority(UUID cppId, String financialYear) {

               
	
        if (cppId == null || financialYear == null || financialYear.isBlank()) {
            throw new IllegalArgumentException("CPP Id and Financial Year are required");
        }

        List<AssetPriorityProjection> projections = assetPriorityRepository
                .getAssetAvailabilityPriorityByCPP(cppId, financialYear);

        return projections.stream().
        filter(projection ->
            projection.getAssetType() != null &&
            !projection.getAssetType().isEmpty() &&
            !"Steam_Dis".equals(projection.getAssetType())
    )
                .map(projection -> {
            
                  
                    AssetPrioriryDTO dto = new AssetPrioriryDTO();
                    dto.setAssetId(projection.getAssetId());
                    dto.setAssetName(projection.getAssetName());
                    dto.setApril(projection.getApril());
                    dto.setMay(projection.getMay());
                    dto.setJune(projection.getJune());
                    dto.setJuly(projection.getJuly());
                    dto.setAug(projection.getAugust());
                    dto.setSep(projection.getSeptember());
                    dto.setOct(projection.getOctober());
                    dto.setNov(projection.getNovember());
                    dto.setDec(projection.getDecember());
                    dto.setJan(projection.getJanuary());
                    dto.setFeb(projection.getFebruary());
                    dto.setMar(projection.getMarch());
                    dto.setRemarks(projection.getRemarks());
                    return dto;
                })
                .toList();
    }

    // code for update

    @Override
    public void setAssetPriority(List<AssetPrioriryDTO> dto,  String financialYear) {
        if (dto == null || dto.isEmpty() || financialYear == null || financialYear.isBlank()) {
            return;
        }

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = Integer.parseInt("20" + financialYear.substring(5));

        // build map of (year-month) -> FinancialYearMonth id in two calls (one per year)
        Map<String, UUID> yearMonthToFymId = new HashMap<>();
        List<FinancialYearMonth> startFyms = fyRepo.findByYear(startYear);
        for (FinancialYearMonth f : startFyms) {
            yearMonthToFymId.put(f.getYear() + "-" + f.getMonth(), f.getId());
        }
        List<FinancialYearMonth> endFyms = fyRepo.findByYear(endYear);
        for (FinancialYearMonth f : endFyms) {
            yearMonthToFymId.put(f.getYear() + "-" + f.getMonth(), f.getId());
        }

         

        // Collect assetIds and the exact fymIds that will be touched
        Set<UUID> assetIds = dto.stream()
                .map(AssetPrioriryDTO::getAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Map<Integer, Integer>> assetToMonthPriority = new HashMap<>();
        Set<UUID> fymIdsToCheck = new HashSet<>();

        // List<Object[]> updatesRemarks = new ArrayList<>();

        for (AssetPrioriryDTO item : dto) {
            UUID assetId = item.getAssetId();
            if (assetId == null) continue;
        //    updatesRemarks.add(new Object[] { item.getRemarks(), assetId });

            Map<Integer, Integer> monthPriorityMap = new HashMap<>();
            monthPriorityMap.put(4, item.getApril());
            monthPriorityMap.put(5, item.getMay());
            monthPriorityMap.put(6, item.getJune());
            monthPriorityMap.put(7, item.getJuly());
            monthPriorityMap.put(8, item.getAug());
            monthPriorityMap.put(9, item.getSep());
            monthPriorityMap.put(10, item.getOct());
            monthPriorityMap.put(11, item.getNov());
            monthPriorityMap.put(12, item.getDec());
            monthPriorityMap.put(1, item.getJan());
            monthPriorityMap.put(2, item.getFeb());
            monthPriorityMap.put(3, item.getMar());

            assetToMonthPriority.put(assetId, monthPriorityMap);

            for (Map.Entry<Integer, Integer> e : monthPriorityMap.entrySet()) {
                if (e.getValue() == null) continue;
                int month = e.getKey();
                int year = (month >= 4) ? startYear : endYear;
                UUID fymId = yearMonthToFymId.get(year + "-" + month);
                if (fymId != null) {
                    fymIdsToCheck.add(fymId);
                }
            }
        }

        if (assetIds.isEmpty() || fymIdsToCheck.isEmpty()) {
            return;
        }

        // Fetch existing rows in a single query
        List<ExistingAssetAvailabilityProjection> existing = assetPriorityRepository
                .findExistingByAssetIdsAndFymIds(new ArrayList<>(assetIds), new ArrayList<>(fymIdsToCheck));

        Set<String> existingKeys = existing.stream()
                .map(p -> p.getAssetId().toString() + "_" + p.getFymId().toString())
                .collect(Collectors.toSet());

                System.out.println("existing keys: " + existingKeys);


                  // **************************  Logic for Remarks Update **************************
           Map<Integer, UUID> financialMonthIdsForFinancialYear = new LinkedHashMap<>();
           List<Object[]> fyMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
         for (Object[] row : fyMonths) {
           Integer month = (Integer) row[0];
           UUID id = UUID.fromString((String) row[1]);
           financialMonthIdsForFinancialYear.put(month, id);
       }

       List<Object[]> updatesRemarks = new ArrayList<>();

       for (AssetPrioriryDTO item : dto)  {

           UUID assetId = item.getAssetId();

          List<Object[]> existingPriorityForAsset = assetPriorityRepository.getAssetCapacitiesByAssetsAndFYMonths(List.of(assetId), financialMonthIdsForFinancialYear.values());

          Set<UUID> existingFinancialYearMonthIds = existingPriorityForAsset.stream()
                .map(row -> UUID.fromString((String) row[1]))
                .collect(Collectors.toSet());

            for(UUID fymId : existingFinancialYearMonthIds) {
                updatesRemarks.add(new Object[] { item.getRemarks(), assetId, fymId });
            }
       }

       if(!updatesRemarks.isEmpty()) {
        String updateRemarksSql = "UPDATE AssetAvailability SET Priority_Remarks = ? WHERE AssetId = ? AND FinancialYearMonthId = ?";
        jdbcTemplate.batchUpdate(updateRemarksSql, updatesRemarks);
       }


        // **************************  End of Remarks Update Logic **************************

        List<Object[]> updates = new ArrayList<>();
        List<Object[]> inserts = new ArrayList<>();
      

        for (Map.Entry<UUID, Map<Integer, Integer>> assetEntry : assetToMonthPriority.entrySet()) {

            UUID assetId = assetEntry.getKey();

          
            for (Map.Entry<Integer, Integer> monthEntry : assetEntry.getValue().entrySet()) {
                Integer priority = monthEntry.getValue();
                if (priority == null) continue;
                int month = monthEntry.getKey();
                int year = (month >= 4) ? startYear : endYear;
                UUID fymId = yearMonthToFymId.get(year + "-" + month);
                if (fymId == null) continue;

                String key = assetId.toString() + "_" + fymId.toString();
                System.out.println("key for asset " + assetId + ": " + key);
                if (existingKeys.contains(key)) {
                    updates.add(new Object[] { priority, assetId, fymId });
                } else {
                    inserts.add(new Object[] { assetId, fymId, priority });
                }
            }
        }

        if (!updates.isEmpty()) {
            String updateSql = "UPDATE AssetAvailability SET Priority = ? WHERE AssetId = ? AND FinancialYearMonthId = ?";
            jdbcTemplate.batchUpdate(updateSql, updates);
          System.out.println("updates: " + updates);
        }

        if (!inserts.isEmpty()) {
            String insertSql = "INSERT INTO AssetAvailability (Id, AssetId, FinancialYearMonthId, Priority) VALUES (NEWID(), ?, ?, ?)";
              jdbcTemplate.batchUpdate(insertSql, inserts);
           System.out.println("inserts: " + inserts.size()  + "comma separated: " + inserts.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }

        // if(!updatesRemarks.isEmpty()) {
        //     String updateSql = "UPDATE PowerGenerationAssets SET Remarks = ? WHERE AssetId = ?";
        //     jdbcTemplate.batchUpdate(updateSql, updatesRemarks);
        // }
    }


    private UUID fetchFinancialYearMonthId(int year, int month) {

        return fyRepo.findFinancialMonthId(year, month);
    }

    @Override
    public byte[] exportAssetPriority(UUID cppId, String financialYear, boolean isAfterSave, List<AssetPrioriryDTO> dtoList) {
        try {
            if (!isAfterSave) {
                dtoList = getAssetPriority(cppId, financialYear);
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Asset Priority");
            int currentRow = 0;

            // Header row
            List<String> headers = new ArrayList<>();
            headers.add("Asset Name");
            headers.add("April");
            headers.add("May");
            headers.add("June");
            headers.add("July");
            headers.add("August");
            headers.add("September");
            headers.add("October");
            headers.add("November");
            headers.add("December");
            headers.add("January");
            headers.add("February");
            headers.add("March");
            headers.add("Remarks");
            headers.add("AssetId"); // Hidden column

            if (isAfterSave) {
                headers.add("Status");
                headers.add("Error Description");
            }

            Row headerRow = sheet.createRow(currentRow++);
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
            }

            // Data rows
            for (AssetPrioriryDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                row.createCell(col++).setCellValue(dto.getAssetName() != null ? dto.getAssetName() : "");
                setCellValue(row.createCell(col++), dto.getApril());
                setCellValue(row.createCell(col++), dto.getMay());
                setCellValue(row.createCell(col++), dto.getJune());
                setCellValue(row.createCell(col++), dto.getJuly());
                setCellValue(row.createCell(col++), dto.getAug());
                setCellValue(row.createCell(col++), dto.getSep());
                setCellValue(row.createCell(col++), dto.getOct());
                setCellValue(row.createCell(col++), dto.getNov());
                setCellValue(row.createCell(col++), dto.getDec());
                setCellValue(row.createCell(col++), dto.getJan());
                setCellValue(row.createCell(col++), dto.getFeb());
                setCellValue(row.createCell(col++), dto.getMar());
                row.createCell(col++).setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                row.createCell(col++).setCellValue(dto.getAssetId() != null ? dto.getAssetId().toString() : "");

                if (isAfterSave) {
                    row.createCell(col++).setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                    row.createCell(col++).setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                }
            }

            // Hide AssetId column (column index 14)
            sheet.setColumnHidden(14, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AOPMessageVM importExcel(UUID cppId, String financialYear, MultipartFile file) {
        try {
            List<AssetPrioriryDTO> data = readAssetPriority(file.getInputStream(), cppId, financialYear);
            
            // Separate failed records from successful ones
            List<AssetPrioriryDTO> validRecords = new ArrayList<>();
            List<AssetPrioriryDTO> failedRecords = new ArrayList<>();
            
            for (AssetPrioriryDTO dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    setAssetPriority(validRecords, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    for (AssetPrioriryDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = exportAssetPriority(cppId, financialYear, true, failedRecords);
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

    private List<AssetPrioriryDTO> readAssetPriority(InputStream inputStream, UUID cppId, String financialYear) {
        List<AssetPrioriryDTO> assetList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AssetPrioriryDTO dto = new AssetPrioriryDTO();
                
                try {
                    dto.setAssetName(getStringCellValue(row.getCell(0)));
                    dto.setApril(getIntegerCellValue(row.getCell(1)));
                    dto.setMay(getIntegerCellValue(row.getCell(2)));
                    dto.setJune(getIntegerCellValue(row.getCell(3)));
                    dto.setJuly(getIntegerCellValue(row.getCell(4)));
                    dto.setAug(getIntegerCellValue(row.getCell(5)));
                    dto.setSep(getIntegerCellValue(row.getCell(6)));
                    dto.setOct(getIntegerCellValue(row.getCell(7)));
                    dto.setNov(getIntegerCellValue(row.getCell(8)));
                    dto.setDec(getIntegerCellValue(row.getCell(9)));
                    dto.setJan(getIntegerCellValue(row.getCell(10)));
                    dto.setFeb(getIntegerCellValue(row.getCell(11)));
                    dto.setMar(getIntegerCellValue(row.getCell(12)));
                    dto.setRemarks(getStringCellValue(row.getCell(13)));
                    
                    String assetIdStr = getStringCellValue(row.getCell(14));
                    if (assetIdStr != null && !assetIdStr.isEmpty()) {
                        dto.setAssetId(UUID.fromString(assetIdStr));
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

    private void setCellValue(Cell cell, Integer value) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
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

    private Integer getIntegerCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // Return null for invalid numbers
        }
        return null;
    }

}





