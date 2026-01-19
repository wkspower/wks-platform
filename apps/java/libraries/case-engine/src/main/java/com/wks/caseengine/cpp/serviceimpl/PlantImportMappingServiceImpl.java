package com.wks.caseengine.cpp.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.repository.PlantImportMappingRepository;
import com.wks.caseengine.dto.PlantImportMappingDto;
import com.wks.caseengine.entity.FinancialYearMonth;
import com.wks.caseengine.entity.PlantImportMapping;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.utility.Utility;


@Service
public class PlantImportMappingServiceImpl {



    @Autowired
    private PlantImportMappingRepository plantRepo;

    @Autowired
    private FinancialYearMonthRepository fyMonthRepo;

    

    //@Override
    public List<PlantImportMappingDto> getPivotData(String cppPlantId, String financialYear) {

        if (financialYear == null || financialYear.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        financialYear = financialYear.trim();

        if (!financialYear.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        int startYear = extractStartYear(financialYear);
        int endYear = extractEndYear(financialYear);

        if (startYear < 2000 || startYear > 2100) {
            throw new RestInvalidArgumentException(
                    "Invalid year range. Start year must be between 2000 and 2100", null);
        }

        if (endYear != startYear + 1) {
            throw new RestInvalidArgumentException(
                    "Invalid financial year. End year must be exactly one year after start year", null);
        }

        try {

            List<MonthYear> monthYearList = generateFinancialYearMonths(financialYear);

            List<FinancialYearMonth> fyMonths = new ArrayList<>();
            List<String> missingEntries = new ArrayList<>();

            for (MonthYear my : monthYearList) {
                Optional<FinancialYearMonth> fmOpt = fyMonthRepo.findByMonthAndYear(my.month, my.year);
                if (fmOpt.isPresent()) {
                    fyMonths.add(fmOpt.get());
                } else {
                    missingEntries.add(String.format("%s-%d", Month.of(my.month).name(), my.year));
                }
            }

            if (!missingEntries.isEmpty()) {
                throw new RestInvalidArgumentException(
                        "Missing FinancialYearMonth entries for: " + String.join(", ", missingEntries) +
                                ". Please ensure FY months exist in FinancialYearMonth table.",
                        null);
            }

            if (fyMonths.size() != 12) {
                throw new RestInvalidArgumentException(
                        "Expected 12 financial months but found " + fyMonths.size(), null);
            }

            List<UUID> fmIds = fyMonths.stream().map(FinancialYearMonth::getId).toList();

             System.out.println("Financial Month Ids: " + fmIds);

  
            // get the plantId from PowerConsumptionPlantMapping table
            List<UUID> plantIds = plantRepo.findPlantIdsByConsumptionId(UUID.fromString(cppPlantId));
                 System.out.println("Asset Ids in the table PlantImportMapping: " + plantIds);

            Map<UUID, String> plantIdNameMap = new HashMap<>();
            List<Object[]> plantIdNames = plantRepo.findPlantsByIds(plantIds);

            for (Object[] row : plantIdNames) {
                String id = (String) row[0];
                String name = (String) row[1];
                plantIdNameMap.put(UUID.fromString(id), name);
            }

            System.out.println("Plant ID to Name Map: " + plantIdNameMap.toString());

       //     List<AssetImportMapping> records = assetRepo.findByFinancialMonthIdIn(fmIds);

            List<PlantImportMapping> records = plantRepo.findByFinancialMonthIdInAndAssetIdIn(fmIds, plantIds);

   

            System.out.println("records: " + records);

            // Group records by assetId (plant) to consolidate multiple months into one row
            Map<UUID, PlantImportMappingDto> plantDataMap = new LinkedHashMap<>();

            for(PlantImportMapping pim : records) {
                UUID assetId = pim.getAssetId();
                
                // Create DTO if it doesn't exist for this plant
                plantDataMap.putIfAbsent(assetId, new PlantImportMappingDto());
                PlantImportMappingDto dto = plantDataMap.get(assetId);
                
                // Set plant name (will be same for all records of same plant)
                dto.setPlant(plantIdNameMap.get(assetId));
                dto.setAssetId(assetId);
                dto.setUom(pim.getUom());
                dto.setRemarks(pim.getRemarks());
                // Set month values based on financial month
                UUID fmId = pim.getFinancialMonthId();
                FinancialYearMonth fm = fyMonths.stream()
                        .filter(f -> f.getId().equals(fmId))
                        .findFirst()
                        .orElse(null);
                if (fm != null) {
                    int month = fm.getMonth();
                    switch (month) {
                        case 4 -> dto.setApril(pim.getValue());
                        case 5 -> dto.setMay(pim.getValue());
                        case 6 -> dto.setJune(pim.getValue());
                        case 7 -> dto.setJuly(pim.getValue());
                        case 8 -> dto.setAug(pim.getValue());
                        case 9 -> dto.setSept(pim.getValue());
                        case 10 -> dto.setOct(pim.getValue());
                        case 11 -> dto.setNov(pim.getValue());
                        case 12 -> dto.setDec(pim.getValue());
                        case 1 -> dto.setJan(pim.getValue());
                        case 2 -> dto.setFeb(pim.getValue());
                        case 3 -> dto.setMar(pim.getValue());
                    }
                }
            }
            
            List<PlantImportMappingDto> data = new ArrayList<>(plantDataMap.values());
            return data;
            
        } catch (DateTimeParseException e) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format: " + financialYear + ". Expected format: YYYY-YY (e.g., 2026-27)", e);
        } catch (Exception e) {
            throw new RestInvalidArgumentException(
                    "An unexpected error occurred while retrieving pivot data. Please contact support.", e);
        }

  
          

    }


    private List<MonthYear> generateFinancialYearMonths(String fy) {
        int startYear = extractStartYear(fy);
        int endYear = extractEndYear(fy);

        List<MonthYear> result = new ArrayList<>();

        for (int m = 4; m <= 12; m++) {
            result.add(new MonthYear(m, startYear));
        }

        for (int m = 1; m <= 3; m++) {
            result.add(new MonthYear(m, endYear));
        }

        return result;
    }

    private int extractStartYear(String fy) {
        try {
            return Integer.parseInt(fy.substring(0, 4));
        } catch (Exception e) {
            throw new RestInvalidArgumentException("Invalid financial year format", e);
        }
    }

    private int extractEndYear(String fy) {
        try {
            int start = extractStartYear(fy);
            int yy = Integer.parseInt(fy.substring(5));

            if (yy < 100) {
                return (start / 100) * 100 + yy;
            }
            return yy;
        } catch (Exception e) {
            throw new RestInvalidArgumentException("Invalid financial year format", e);
        }
    }

    private String formatLabel(int month, int year) {
        return LocalDate.of(year, month, 1)
                .format(DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH));
    }

    private static class MonthYear {
        final int month;
        final int year;

        MonthYear(int month, int year) {
            this.month = month;
            this.year = year;
        }
    }

    @Transactional
    public void upsertPlantImportMapping(List<PlantImportMappingDto> pim, String cppPlantId, String financialYear) {

        for (PlantImportMappingDto dto : pim) {
            
        if (dto == null) {
            throw new RestInvalidArgumentException("PlantImportMappingDto is required", null);
        }

        if (dto.getAssetId() == null) {
            throw new RestInvalidArgumentException("assetId is required in PlantImportMappingDto", null);
        }

        if (financialYear == null || financialYear.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        financialYear = financialYear.trim();

        if (!financialYear.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        // verify that the supplied assetId belongs to the cppPlantId
        if (cppPlantId != null && !cppPlantId.isBlank()) {
            List<UUID> plantIds = plantRepo.findPlantIdsByConsumptionId(UUID.fromString(cppPlantId));
            if (!plantIds.contains(dto.getAssetId())) {
                throw new RestInvalidArgumentException("The provided assetId does not belong to the given cppPlantId", null);
            }
        }

        try {
            List<MonthYear> monthYearList = generateFinancialYearMonths(financialYear);

            // for each month in the financial year, get corresponding FinancialYearMonth and upsert value
            List<PlantImportMapping> existing = plantRepo.findByAssetId(dto.getAssetId());

            for (MonthYear my : monthYearList) {
                Optional<FinancialYearMonth> fmOpt = fyMonthRepo.findByMonthAndYear(my.month, my.year);
                if (fmOpt.isEmpty()) {
                    throw new RestInvalidArgumentException(
                            "Missing FinancialYearMonth for month=" + my.month + " year=" + my.year, null);
                }

                UUID fmId = fmOpt.get().getId();

                double valueToSet = getValueForMonth(dto, my.month);

                // find existing mapping for this assetId + financialMonthId
                PlantImportMapping match = null;
                for (PlantImportMapping p : existing) {
                    if (p.getFinancialMonthId().equals(fmId)) {
                        match = p;
                        break;
                    }
                }

                if (match != null) {
                    match.setValue(valueToSet);
                    match.setRemarks(dto.getRemarks() == null ? "" : dto.getRemarks());
                    if (dto.getUom() != null) match.setUom(dto.getUom());
                    plantRepo.save(match);
                } else {
                    PlantImportMapping newRec = new PlantImportMapping();
                    newRec.setAssetId(dto.getAssetId());
                    newRec.setFinancialMonthId(fmId);
                    newRec.setValue(valueToSet);
                    newRec.setUom(dto.getUom() == null ? "" : dto.getUom());
                    newRec.setRemarks(dto.getRemarks() == null ? "" : dto.getRemarks());
                    plantRepo.save(newRec);
                }
            }

        } catch (Exception e) {
            throw new RestInvalidArgumentException("Failed to upsert PlantImportMapping records", e);
        }

    }
    }

    private double getValueForMonth(PlantImportMappingDto dto, int month) {
        return switch (month) {
            case 4 -> dto.getApril();
            case 5 -> dto.getMay();
            case 6 -> dto.getJune();
            case 7 -> dto.getJuly();
            case 8 -> dto.getAug();
            case 9 -> dto.getSept();
            case 10 -> dto.getOct();
            case 11 -> dto.getNov();
            case 12 -> dto.getDec();
            case 1 -> dto.getJan();
            case 2 -> dto.getFeb();
            case 3 -> dto.getMar();
            default -> 0.0;
        };
    }

    public byte[] exportPlantImportMapping(String cppPlantId, String financialYear, boolean isAfterSave, List<PlantImportMappingDto> dtoList) {
        try {
            if (!isAfterSave) {
                dtoList = getPivotData(cppPlantId, financialYear);
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Plant Import Mapping");
            int currentRow = 0;

            // Header row
            List<String> headers = new ArrayList<>();
            headers.add("Plant");
            headers.add("UOM");
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
            for (PlantImportMappingDto dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                row.createCell(col++).setCellValue(dto.getPlant() != null ? dto.getPlant() : "");
                row.createCell(col++).setCellValue(dto.getUom() != null ? dto.getUom() : "");
                setCellValue(row.createCell(col++), dto.getApril());
                setCellValue(row.createCell(col++), dto.getMay());
                setCellValue(row.createCell(col++), dto.getJune());
                setCellValue(row.createCell(col++), dto.getJuly());
                setCellValue(row.createCell(col++), dto.getAug());
                setCellValue(row.createCell(col++), dto.getSept());
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

            // Hide AssetId column (column index 15)
            sheet.setColumnHidden(15, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public AOPMessageVM importExcel(String cppPlantId, String financialYear, MultipartFile file) {
        try {
            List<PlantImportMappingDto> data = readPlantImportMapping(file.getInputStream(), cppPlantId, financialYear);
            
            // Separate failed records from successful ones
            List<PlantImportMappingDto> validRecords = new ArrayList<>();
            List<PlantImportMappingDto> failedRecords = new ArrayList<>();
            
            for (PlantImportMappingDto dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    upsertPlantImportMapping(validRecords, cppPlantId, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    for (PlantImportMappingDto dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = exportPlantImportMapping(cppPlantId, financialYear, true, failedRecords);
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

    private List<PlantImportMappingDto> readPlantImportMapping(InputStream inputStream, String cppPlantId, String financialYear) {
        List<PlantImportMappingDto> plantList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                PlantImportMappingDto dto = new PlantImportMappingDto();
                
                try {
                    dto.setPlant(getStringCellValue(row.getCell(0)));
                    dto.setUom(getStringCellValue(row.getCell(1)));
                    dto.setApril(getDoubleCellValue(row.getCell(2)));
                    dto.setMay(getDoubleCellValue(row.getCell(3)));
                    dto.setJune(getDoubleCellValue(row.getCell(4)));
                    dto.setJuly(getDoubleCellValue(row.getCell(5)));
                    dto.setAug(getDoubleCellValue(row.getCell(6)));
                    dto.setSept(getDoubleCellValue(row.getCell(7)));
                    dto.setOct(getDoubleCellValue(row.getCell(8)));
                    dto.setNov(getDoubleCellValue(row.getCell(9)));
                    dto.setDec(getDoubleCellValue(row.getCell(10)));
                    dto.setJan(getDoubleCellValue(row.getCell(11)));
                    dto.setFeb(getDoubleCellValue(row.getCell(12)));
                    dto.setMar(getDoubleCellValue(row.getCell(13)));
                    dto.setRemarks(getStringCellValue(row.getCell(14)));
                    
                    String assetIdStr = getStringCellValue(row.getCell(15));
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
                
                plantList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return plantList;
    }

    private void setCellValue(Cell cell, double value) {
        cell.setCellValue(value);
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

    private double getDoubleCellValue(Cell cell) {
        if (cell == null) {
            return 0.0;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return 0.0;
                }
                return Double.parseDouble(value);
            } else if (cell.getCellType() == CellType.FORMULA) {
                return cell.getNumericCellValue();
            }
        } catch (NumberFormatException e) {
            // Return 0.0 for invalid numbers
        }
        return 0.0;
    }
}


