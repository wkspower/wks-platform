package com.wks.caseengine.cpp.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.FinancialYearMonthProjection;
import com.wks.caseengine.cpp.dto.FixedConsumptionDto;
import com.wks.caseengine.cpp.dto.FixedConsumptionProjection;
import com.wks.caseengine.cpp.repository.FixedConsumptionRepository;
import com.wks.caseengine.message.vm.AOPMessageVM;

@Service
public class FixedConsumptionService {

    @Autowired
    private FixedConsumptionRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<FixedConsumptionDto> getData(UUID plantId, String financialYear) {

        return repository.getFixedConsumption(plantId, financialYear)
                         .stream()
                         .map(this::toDto)
                         .toList();
    }

    public void updateData(List<FixedConsumptionDto> fixedConsumptionDtoList, String financialYear) { 

// get start year and end year from financialYear
        String input = financialYear;    // payload must contain 2026-27 format

        String startYear = input.substring(0, 4);               // "2026"
        String endYearSuffix = input.substring(5);              // "27"
        String endYear = startYear.substring(0, 2) + endYearSuffix; // "2027"

        List<FinancialYearMonthProjection> financialYearMonthList = repository.getFinancialYearMonth();

    //     List<String> costCenterIds = repository.getCostCenterIds(fixedConsumptionDtoList.stream()       // fixedConsumptionDto.getCostCenterId returns costCenterCode
    //                                                         .map(FixedConsumptionDto::getCostCenterId)
    //                                                         .collect(Collectors.toList()));

        
        
    //    List<String> normParameterIds = repository.getNormParameterIds(costCenterIds);

    //    List<String> utilityFixedConsumptionIds = repository.getUtilityFixedConsumptionIds(costCenterIds, normParameterIds);
         List<Object[]> remarkUpdates = new ArrayList<>();
         List<Object[]> remarkInserts = new ArrayList<>();
         List<UUID> existingRemarkIds =  repository.getExistingRemarkIds(fixedConsumptionDtoList.stream()
         .map(FixedConsumptionDto::getRemarkId)
         .collect(Collectors.toList()));

         List<Object[]> utilityUpdates = new ArrayList<>();

        for(FixedConsumptionDto fixedConsumptionDto : fixedConsumptionDtoList) {

            if(existingRemarkIds.contains(fixedConsumptionDto.getRemarkId())) {  

                remarkUpdates.add(new Object[]{ fixedConsumptionDto.getRemarks(), financialYear, fixedConsumptionDto.getRemarkId()});
            } else {
                remarkInserts.add(new Object[]{ fixedConsumptionDto.getRemarks(), fixedConsumptionDto.getCostCenter_FK_Id(), fixedConsumptionDto.getNormParameter_FK_Id(), financialYear});
            }

             
           List<String> costCenterIds = repository.getCostCenterIds(Arrays.asList(fixedConsumptionDto.getCostCenterId()));  // fixedConsumptionDto.getCostCenterId returns costCenterCode

           String normParameterId =  fixedConsumptionDto.getNormParameterId();

           // will only fetch utilityFixedConsumptionIds for given year 
           List<String> utilityFixedConsumptionIds = repository.getUtilityFixedConsumptionIds(costCenterIds, normParameterId , financialYearMonthList.stream()
                                                    .filter(f -> (Integer.parseInt(f.getMonth()) >= 4 && f.getYear()
                                                    .equals(startYear)) || (Integer.parseInt(f.getMonth()) <= 3 && f.getYear().equals(endYear)))
                                                    .map(f -> f.getId()).toList());

           System.out.println("*** fixedComsuption costCenterIds : " + costCenterIds);
           System.out.println("*** fixedComsuption normParameterIds : " + normParameterId);
           System.out.println("*** fixedComsuption fixedConsumptionIds : " + utilityFixedConsumptionIds);

           // Data entry for missing month. this is to allow edit feature for blank cells. 
           // find the missing month
           if(utilityFixedConsumptionIds.size() != 12) {  
            // get FinancialYearMonthIds from apr 2025 to march 2026
            List<String> AllfinancialYearMonthIdsBetweenApril25AndMarch26 = financialYearMonthList.stream().filter(f -> (Integer.parseInt(f.getMonth()) >= 4 && f.getYear().equals(startYear)) || (Integer.parseInt(f.getMonth()) <= 3 && f.getYear().equals(endYear))).map(f -> f.getId()).toList();
          
          
         List<String> financialYearMonthIdsForUtilityFixedConsumptionIds = repository.getFinancialYearMonthIdsForUtilityFixedConsumptionIds(utilityFixedConsumptionIds);

         List<String> missingMonthIds = AllfinancialYearMonthIdsBetweenApril25AndMarch26.stream().filter(f -> !financialYearMonthIdsForUtilityFixedConsumptionIds.contains(f)).toList();
            System.out.println("*** missingMonthIds : " + missingMonthIds);
            // make insert query for missing month
             for(String missingMonthId : missingMonthIds) {
                System.out.println("making entry for missing month : " + missingMonthId);
                String utilityFixedConsumptionId = repository.insertUtilityFixedConsumption(missingMonthId, normParameterId, costCenterIds.get(0), 0.0);
                System.out.println("*** utilityFixedConsumptionId : " + utilityFixedConsumptionId);
                utilityFixedConsumptionIds.add(utilityFixedConsumptionId);
             }
           }
 
           String idsCsv = String.join(",", utilityFixedConsumptionIds);

            if(fixedConsumptionDto.getApril() != null) {  

            String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("4") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

            //    repository.updateUtilityFixedConsumption(fixedConsumptionDto.getApril(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getApril(), idsCsv, financialYearMonthId });
                
            }

            if(fixedConsumptionDto.getMay() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("5") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getMay(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getMay(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getJune() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("6") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJune(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getJune(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getJuly() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("7") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJuly(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getJuly(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getAug() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("8") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getAug(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getAug(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getSep() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("9") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

              //  repository.updateUtilityFixedConsumption(fixedConsumptionDto.getSep(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getSep(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getOct() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("10") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getOct(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getOct(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getNov() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("11") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getNov(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getNov(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getDec() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("12") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getDec(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getDec(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getJan() != null) {  
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("1") && f.getYear().equals(endYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJan(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getJan(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getFeb() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("2") && f.getYear().equals(endYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getFeb(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getFeb(), idsCsv, financialYearMonthId });
            }

            if(fixedConsumptionDto.getMar() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("3") && f.getYear().equals(endYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

             //   repository.updateUtilityFixedConsumption(fixedConsumptionDto.getMar(), utilityFixedConsumptionIds, financialYearMonthId);
                utilityUpdates.add(new Object[]{ fixedConsumptionDto.getMar(), idsCsv, financialYearMonthId });
            }
            }

            if(!utilityUpdates.isEmpty()) { 
                String sql = "Update UtilityFixedConsumption set ConsumptionValue = ? WHERE Id IN (SELECT value FROM STRING_SPLIT(?, ','))  and FinancialYearMonth_FK_Id = ?";
                jdbcTemplate.batchUpdate(sql, utilityUpdates);
            }
 //  @Query(value = "UPDATE UtilityFixedConsumption SET ConsumptionValue = :consumptionValue WHERE Id IN :utilityFixedConsumptionIds AND FinancialYearMonth_FK_Id = :financialYearMonthId", nativeQuery = true)
   

            if(!remarkUpdates.isEmpty()) { 

                String sql = "Update UtilityFixedConsumption_Remarks set Remarks = ?, FinancialYear = ? where Id = ?";
                jdbcTemplate.batchUpdate(sql, remarkUpdates);
            }

            if(!remarkInserts.isEmpty()) { 
                String sql = "Insert into UtilityFixedConsumption_Remarks (Id, Remarks, CostCenter_FK_Id, NormParameter_FK_Id, FinancialYear) values (NEWID(), ?, ?, ?, ?)";
                jdbcTemplate.batchUpdate(sql, remarkInserts);
            }

    }

    private FixedConsumptionDto toDto(FixedConsumptionProjection p) {
        FixedConsumptionDto dto = new FixedConsumptionDto();

        dto.setPlant(p.getPlantName());
        dto.setPlantId(p.getPlantCode());
        dto.setCostCenter(p.getCostCenterName());
        dto.setCostCenterId(p.getCostCenterCode());
        dto.setCppUtility(p.getUtilityName());
        dto.setCppUtilityId(p.getUtilitySAP());
        dto.setCppPlant(p.getUtilityPlantName());
        dto.setCppPlantId(p.getUtilityPlantCode());
        dto.setUom(p.getUom());
        dto.setNormParameterId(p.getNormParameterId());
        dto.setCostCenter_FK_Id(p.getCostCenter_FK_Id());
        dto.setNormParameter_FK_Id(p.getNormParameter_FK_Id());
        dto.setRemarkId(p.getRemarkId());
        dto.setRemarks(p.getRemarks());
        System.out.println("*** remarks  for Id: " + p.getRemarkId() + " is " + p.getRemarks());

        dto.setApril(p.getApr());
        dto.setMay(p.getMay());
        dto.setJune(p.getJun());
        dto.setJuly(p.getJul());
        dto.setAug(p.getAug());
        dto.setSep(p.getSep());
        dto.setOct(p.getOct());
        dto.setNov(p.getNov());
        dto.setDec(p.getDec());
        dto.setJan(p.getJan());
        dto.setFeb(p.getFeb());
        dto.setMar(p.getMar());

        dto.setGrandTotal(  Optional.ofNullable(p.getApr()).orElse(0.0) +
        Optional.ofNullable(p.getMay()).orElse(0.0) +
        Optional.ofNullable(p.getJun()).orElse(0.0) +
        Optional.ofNullable(p.getJul()).orElse(0.0) +
        Optional.ofNullable(p.getAug()).orElse(0.0) +
        Optional.ofNullable(p.getSep()).orElse(0.0) +
        Optional.ofNullable(p.getOct()).orElse(0.0) +
        Optional.ofNullable(p.getNov()).orElse(0.0) +
        Optional.ofNullable(p.getDec()).orElse(0.0) +
        Optional.ofNullable(p.getJan()).orElse(0.0) +
        Optional.ofNullable(p.getFeb()).orElse(0.0) +
        Optional.ofNullable(p.getMar()).orElse(0.0));

        return dto;
    }

    public byte[] exportFixedConsumption(UUID plantId, String financialYear, boolean isAfterSave, List<FixedConsumptionDto> dtoList) {

      
        try {
            if (!isAfterSave) {
                dtoList = getData(plantId, financialYear);
            }

            System.out.println("export fixed consumption dto: " + dtoList);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Fixed Consumption");
            int currentRow = 0;

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle remarksStyle = createRemarksStyle(workbook);
            String startYearSuffix = financialYear.substring(2, 4);
            String endYearSuffix = financialYear.substring(5, 7);

            // Header row
            List<String> headers = new ArrayList<>();
            headers.add("Plant");
            headers.add("Plant Id");
            headers.add("CPP Utilities");
            headers.add("CPP Utility Ids");
            headers.add("CPP Plant");
            headers.add("CPP Plant Id");
            headers.add("UOM");
            headers.add("Apr-" + startYearSuffix);
            headers.add("May-" + startYearSuffix);
            headers.add("Jun-" + startYearSuffix);
            headers.add("Jul-" + startYearSuffix);
            headers.add("Aug-" + startYearSuffix);
            headers.add("Sep-" + startYearSuffix);
            headers.add("Oct-" + startYearSuffix);
            headers.add("Nov-" + startYearSuffix);
            headers.add("Dec-" + startYearSuffix);
            headers.add("Jan-" + endYearSuffix);
            headers.add("Feb-" + endYearSuffix);
            headers.add("Mar-" + endYearSuffix);
            headers.add("Grand Total");
            headers.add("Remarks");
            // Hidden columns
            headers.add("remarkId");
            headers.add("costCenter_FK_Id");
            headers.add("costCenterId");
            headers.add("normParameter_FK_Id");
            headers.add("normParameterId");

            if (isAfterSave) {
                headers.add("Status");
                headers.add("Error Description");
            }

            Row headerRow = sheet.createRow(currentRow++);
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (FixedConsumptionDto dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                Cell cell = row.createCell(col++);
                cell.setCellValue(dto.getPlant() != null ? dto.getPlant() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getPlantId() != null ? dto.getPlantId() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getCppUtility() != null ? dto.getCppUtility() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getCppUtilityId() != null ? dto.getCppUtilityId() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getCppPlant() != null ? dto.getCppPlant() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getCppPlantId() != null ? dto.getCppPlantId() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUom() != null ? dto.getUom() : "");
                cell.setCellStyle(dataStyle);
                
                setDoubleCellValue(row.createCell(col++), dto.getApril(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getMay(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getJune(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getJuly(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getAug(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getSep(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getOct(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getNov(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getDec(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getJan(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getFeb(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getMar(), dataStyle);
                setDoubleCellValue(row.createCell(col++), dto.getGrandTotal(), dataStyle);
                
                cell = row.createCell(col++);
                cell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                cell.setCellStyle(remarksStyle);
                
                // Hidden columns
                cell = row.createCell(col++);
                cell.setCellValue(dto.getRemarkId() != null ? dto.getRemarkId().toString() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getCostCenter_FK_Id() != null ? dto.getCostCenter_FK_Id().toString() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getCostCenterId() != null ? dto.getCostCenterId() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getNormParameter_FK_Id() != null ? dto.getNormParameter_FK_Id().toString() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getNormParameterId() != null ? dto.getNormParameterId() : "");
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

            // Hide specified columns (21-25: remarkId, costCenter_FK_Id, costCenterId, normParameter_FK_Id, normParameterId)
            sheet.setColumnHidden(21, true); // remarkId
            sheet.setColumnHidden(22, true); // costCenter_FK_Id
            sheet.setColumnHidden(23, true); // costCenterId
            sheet.setColumnHidden(24, true); // normParameter_FK_Id
            sheet.setColumnHidden(25, true); // normParameterId

            int totalColumns = headers.size();
            for (int col = 0; col < totalColumns; col++) {
                if (col == 20) {
                    sheet.setColumnWidth(col, 8000);
                    continue;
                }
                sheet.autoSizeColumn(col);
                String headerText = headers.get(col);
                int headerWidth = Math.min(255 * 256, (headerText.length() + 2) * 256);
                if (sheet.getColumnWidth(col) < headerWidth) {
                    sheet.setColumnWidth(col, headerWidth);
                }
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

    public AOPMessageVM importExcel(UUID plantId, String financialYear, MultipartFile file) {
        try {
            List<FixedConsumptionDto> data = readFixedConsumption(file.getInputStream(), plantId, financialYear);

            System.out.println("import fixed consumption data: " + data);
            
            // Separate failed records from successful ones
            List<FixedConsumptionDto> validRecords = new ArrayList<>();
            List<FixedConsumptionDto> failedRecords = new ArrayList<>();
            
            for (FixedConsumptionDto dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    updateData(validRecords, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    for (FixedConsumptionDto dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = exportFixedConsumption(plantId, financialYear, true, failedRecords);
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

    private List<FixedConsumptionDto> readFixedConsumption(InputStream inputStream, UUID plantId, String financialYear) {
        List<FixedConsumptionDto> dataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                FixedConsumptionDto dto = new FixedConsumptionDto();
                
                try {
                    int col = 0;
                    dto.setPlant(getStringCellValue(row.getCell(col++)));
                    dto.setPlantId(getStringCellValue(row.getCell(col++)));
                    dto.setCppUtility(getStringCellValue(row.getCell(col++)));
                    dto.setCppUtilityId(getStringCellValue(row.getCell(col++)));
                    dto.setCppPlant(getStringCellValue(row.getCell(col++)));
                    dto.setCppPlantId(getStringCellValue(row.getCell(col++)));
                    dto.setUom(getStringCellValue(row.getCell(col++)));
                    
                    dto.setApril(getDoubleCellValue(row.getCell(col++)));
                    dto.setMay(getDoubleCellValue(row.getCell(col++)));
                    dto.setJune(getDoubleCellValue(row.getCell(col++)));
                    dto.setJuly(getDoubleCellValue(row.getCell(col++)));
                    dto.setAug(getDoubleCellValue(row.getCell(col++)));
                    dto.setSep(getDoubleCellValue(row.getCell(col++)));
                    dto.setOct(getDoubleCellValue(row.getCell(col++)));
                    dto.setNov(getDoubleCellValue(row.getCell(col++)));
                    dto.setDec(getDoubleCellValue(row.getCell(col++)));
                    dto.setJan(getDoubleCellValue(row.getCell(col++)));
                    dto.setFeb(getDoubleCellValue(row.getCell(col++)));
                    dto.setMar(getDoubleCellValue(row.getCell(col++)));
                    
                    // Grand Total is read-only, skip it
                    col++; // Skip Grand Total column
                    
                    dto.setRemarks(getStringCellValue(row.getCell(col++)));
                    
                    // Hidden columns
                    String remarkIdStr = getStringCellValue(row.getCell(col++));
                    if (remarkIdStr != null && !remarkIdStr.isEmpty()) {
                        dto.setRemarkId(UUID.fromString(remarkIdStr));
                    }
                    
                    String costCenterFKIdStr = getStringCellValue(row.getCell(col++));
                    if (costCenterFKIdStr != null && !costCenterFKIdStr.isEmpty()) {
                        dto.setCostCenter_FK_Id(UUID.fromString(costCenterFKIdStr));
                    }
                    
                    dto.setCostCenterId(getStringCellValue(row.getCell(col++)));
                    
                    String normParameterFKIdStr = getStringCellValue(row.getCell(col++));
                    if (normParameterFKIdStr != null && !normParameterFKIdStr.isEmpty()) {
                        dto.setNormParameter_FK_Id(UUID.fromString(normParameterFKIdStr));
                    } else {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("NormParameter FK Id is missing");
                    }
                    
                    String normParameterIdStr = getStringCellValue(row.getCell(col++));
                    if (normParameterIdStr != null && !normParameterIdStr.isEmpty()) {
                        dto.setNormParameterId(normParameterIdStr);
                    } else {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("NormParameter Id is missing");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage());
                }
                
                dataList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataList;
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
}



