package com.wks.caseengine.rest.cpp;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.service.ImportPowerCapacityService;
import com.wks.caseengine.cpp.service.ImportPowerHoursService;
import com.wks.caseengine.dto.ImportPowerCapacityDto;
import com.wks.caseengine.dto.ImportPowerHoursDto;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.utility.Utility;

@RestController
@RequestMapping("/task/import-power")
public class ImportPowerController {

    @Autowired
    private ImportPowerHoursService importPowerHoursService;

    @Autowired
    private ImportPowerCapacityService importPowerCapacityService;

    // ========================================
    // OPERATIONAL HOURS ENDPOINTS
    // ========================================

    @GetMapping("/operational-hours/{cppPlantId}/{financialYear}")
    public ResponseEntity<List<ImportPowerHoursDto>> getImportPowerOperationalHours(
            @PathVariable UUID cppPlantId,
            @PathVariable String financialYear) {
        
        List<ImportPowerHoursDto> data = importPowerHoursService.getImportPowerOperationalHours(cppPlantId, financialYear);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/operational-hours/{cppPlantId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> saveImportPowerOperationalHours(
            @PathVariable UUID cppPlantId,
            @PathVariable String financialYear,
            @RequestBody List<ImportPowerHoursDto> payload) {
        
        importPowerHoursService.upsertImportPowerOperationalHours(payload, financialYear);
        return ResponseEntity.ok(AOPMessageVM.builder()
                .code(0)
                .message("Operational hours saved successfully")
                .data(null)
                .build());
    }

    @GetMapping("/operational-hours/export/{cppPlantId}/{financialYear}")
    public ResponseEntity<byte[]> exportOperationalHours(
            @PathVariable UUID cppPlantId,
            @PathVariable String financialYear) {
        
        List<ImportPowerHoursDto> data = importPowerHoursService.getImportPowerOperationalHours(cppPlantId, financialYear);
        byte[] excelFile = generateOperationalHoursExcel(data);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "ImportPower_OperationalHours_" + financialYear + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    @PostMapping("/operational-hours/import/{cppPlantId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> importOperationalHours(
            @PathVariable UUID cppPlantId,
            @PathVariable String financialYear,
            @RequestParam("file") MultipartFile file) {
        
        try {
            List<ImportPowerHoursDto> data = parseOperationalHoursFromExcel(file.getInputStream());
            
            // Separate passed and failed records based on validation
            List<ImportPowerHoursDto> validRecords = new ArrayList<>();
            List<ImportPowerHoursDto> failedRecords = new ArrayList<>();
            
            for (ImportPowerHoursDto dto : data) {
                if (dto.getSourceId() != null && dto.getSourceName() != null) {
                    validRecords.add(dto);
                } else {
                    failedRecords.add(dto);
                }
            }
            
            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    importPowerHoursService.upsertImportPowerOperationalHours(validRecords, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    for (ImportPowerHoursDto dto : validRecords) {
                        failedRecords.add(dto);
                    }
                }
            }
            
            // Prepare response
            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = generateOperationalHoursExcel(failedRecords);
                String base64File = Base64.getEncoder().encodeToString(fileByteArray);
                aopMessageVM.setData(base64File);
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved. Please check the error file for details.");
            } else {
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("All operational hours imported successfully");
            }
            
            return ResponseEntity.ok(aopMessageVM);
        } catch (Exception e) {
            return ResponseEntity.ok(AOPMessageVM.builder()
                    .code(500)
                    .message("Error importing operational hours: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    // ========================================
    // CAPACITY ENDPOINTS
    // ========================================

    @GetMapping("/capacity/{cppPlantId}/{financialYear}")
    public ResponseEntity<List<ImportPowerCapacityDto>> getImportPowerCapacity(
            @PathVariable UUID cppPlantId,
            @PathVariable String financialYear) {
        
        List<ImportPowerCapacityDto> data = importPowerCapacityService.getImportPowerCapacity(cppPlantId, financialYear);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/capacity/{cppPlantId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> saveImportPowerCapacity(
            @PathVariable UUID cppPlantId,
            @PathVariable String financialYear,
            @RequestBody List<ImportPowerCapacityDto> payload) {
        
        importPowerCapacityService.upsertImportPowerCapacity(payload, financialYear);
        return ResponseEntity.ok(AOPMessageVM.builder()
                .code(0)
                .message("Capacity saved successfully")
                .data(null)
                .build());
    }

    @GetMapping("/capacity/export/{cppPlantId}/{financialYear}")
    public ResponseEntity<byte[]> exportCapacity(
            @PathVariable UUID cppPlantId,
            @PathVariable String financialYear) {
        
        List<ImportPowerCapacityDto> data = importPowerCapacityService.getImportPowerCapacity(cppPlantId, financialYear);
        byte[] excelFile = generateCapacityExcel(data);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "ImportPower_Capacity_" + financialYear + ".xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    @PostMapping("/capacity/import/{cppPlantId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> importCapacity(
            @PathVariable UUID cppPlantId,
            @PathVariable String financialYear,
            @RequestParam("file") MultipartFile file) {
        
        try {
            List<ImportPowerCapacityDto> data = parseCapacityFromExcel(file.getInputStream());
            
            // Separate passed and failed records based on validation
            List<ImportPowerCapacityDto> validRecords = new ArrayList<>();
            List<ImportPowerCapacityDto> failedRecords = new ArrayList<>();
            
            for (ImportPowerCapacityDto dto : data) {
                if (dto.getSourceId() != null && dto.getSourceName() != null) {
                    validRecords.add(dto);
                } else {
                    failedRecords.add(dto);
                }
            }
            
            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    importPowerCapacityService.upsertImportPowerCapacity(validRecords, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    for (ImportPowerCapacityDto dto : validRecords) {
                        failedRecords.add(dto);
                    }
                }
            }
            
            // Prepare response
            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = generateCapacityExcel(failedRecords);
                String base64File = Base64.getEncoder().encodeToString(fileByteArray);
                aopMessageVM.setData(base64File);
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved. Please check the error file for details.");
            } else {
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("All capacity imported successfully");
            }
            
            return ResponseEntity.ok(aopMessageVM);
        } catch (Exception e) {
            return ResponseEntity.ok(AOPMessageVM.builder()
                    .code(500)
                    .message("Error importing capacity: " + e.getMessage())
                    .data(null)
                    .build());
        }
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private byte[] generateOperationalHoursExcel(List<ImportPowerHoursDto> data) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Operational Hours");
            int currentRow = 0;

            // Header row
            List<String> headers = List.of(
                    "Source Name", "Material Code", "SAP Code", "Plant Name",
                    "April", "May", "June", "July", "August", "September",
                    "October", "November", "December", "January", "February", "March",
                    "Remarks", "Source ID"
            );

            Row headerRow = sheet.createRow(currentRow++);
            CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (ImportPowerHoursDto dto : data) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                row.createCell(col++).setCellValue(dto.getSourceName() != null ? dto.getSourceName() : "");
                row.createCell(col++).setCellValue(dto.getMaterialCode() != null ? dto.getMaterialCode() : "");
                row.createCell(col++).setCellValue(dto.getSapCode() != null ? dto.getSapCode() : "");
                row.createCell(col++).setCellValue(dto.getPlantName() != null ? dto.getPlantName() : "");
                row.createCell(col++).setCellValue(dto.getApril() != null ? dto.getApril() : 0.0);
                row.createCell(col++).setCellValue(dto.getMay() != null ? dto.getMay() : 0.0);
                row.createCell(col++).setCellValue(dto.getJune() != null ? dto.getJune() : 0.0);
                row.createCell(col++).setCellValue(dto.getJuly() != null ? dto.getJuly() : 0.0);
                row.createCell(col++).setCellValue(dto.getAugust() != null ? dto.getAugust() : 0.0);
                row.createCell(col++).setCellValue(dto.getSeptember() != null ? dto.getSeptember() : 0.0);
                row.createCell(col++).setCellValue(dto.getOctober() != null ? dto.getOctober() : 0.0);
                row.createCell(col++).setCellValue(dto.getNovember() != null ? dto.getNovember() : 0.0);
                row.createCell(col++).setCellValue(dto.getDecember() != null ? dto.getDecember() : 0.0);
                row.createCell(col++).setCellValue(dto.getJanuary() != null ? dto.getJanuary() : 0.0);
                row.createCell(col++).setCellValue(dto.getFebruary() != null ? dto.getFebruary() : 0.0);
                row.createCell(col++).setCellValue(dto.getMarch() != null ? dto.getMarch() : 0.0);
                row.createCell(col++).setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                row.createCell(col++).setCellValue(dto.getSourceId() != null ? dto.getSourceId().toString() : "");
            }

            // Hide Source ID column
            sheet.setColumnHidden(17, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] generateCapacityExcel(List<ImportPowerCapacityDto> data) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Capacity");
            int currentRow = 0;

            // Header row
            List<String> headers = List.of(
                    "Source Name", "Material Code", "SAP Code", "Plant Name", "UOM",
                    "April", "May", "June", "July", "August", "September",
                    "October", "November", "December", "January", "February", "March",
                    "Remarks", "Source ID"
            );

            Row headerRow = sheet.createRow(currentRow++);
            CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (ImportPowerCapacityDto dto : data) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                row.createCell(col++).setCellValue(dto.getSourceName() != null ? dto.getSourceName() : "");
                row.createCell(col++).setCellValue(dto.getMaterialCode() != null ? dto.getMaterialCode() : "");
                row.createCell(col++).setCellValue(dto.getSapCode() != null ? dto.getSapCode() : "");
                row.createCell(col++).setCellValue(dto.getPlantName() != null ? dto.getPlantName() : "");
                row.createCell(col++).setCellValue(dto.getUom() != null ? dto.getUom() : "MW");
                row.createCell(col++).setCellValue(dto.getApril() != null ? dto.getApril() : 0.0);
                row.createCell(col++).setCellValue(dto.getMay() != null ? dto.getMay() : 0.0);
                row.createCell(col++).setCellValue(dto.getJune() != null ? dto.getJune() : 0.0);
                row.createCell(col++).setCellValue(dto.getJuly() != null ? dto.getJuly() : 0.0);
                row.createCell(col++).setCellValue(dto.getAugust() != null ? dto.getAugust() : 0.0);
                row.createCell(col++).setCellValue(dto.getSeptember() != null ? dto.getSeptember() : 0.0);
                row.createCell(col++).setCellValue(dto.getOctober() != null ? dto.getOctober() : 0.0);
                row.createCell(col++).setCellValue(dto.getNovember() != null ? dto.getNovember() : 0.0);
                row.createCell(col++).setCellValue(dto.getDecember() != null ? dto.getDecember() : 0.0);
                row.createCell(col++).setCellValue(dto.getJanuary() != null ? dto.getJanuary() : 0.0);
                row.createCell(col++).setCellValue(dto.getFebruary() != null ? dto.getFebruary() : 0.0);
                row.createCell(col++).setCellValue(dto.getMarch() != null ? dto.getMarch() : 0.0);
                row.createCell(col++).setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                row.createCell(col++).setCellValue(dto.getSourceId() != null ? dto.getSourceId().toString() : "");
            }

            // Hide Source ID column
            sheet.setColumnHidden(18, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<ImportPowerHoursDto> parseOperationalHoursFromExcel(java.io.InputStream inputStream) throws Exception {
        List<ImportPowerHoursDto> dtoList = new ArrayList<>();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Skip completely blank rows
                boolean isBlank = true;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    if (row.getCell(j) != null && !row.getCell(j).toString().trim().isEmpty()) {
                        isBlank = false;
                        break;
                    }
                }
                if (isBlank) continue;
                
                ImportPowerHoursDto dto = new ImportPowerHoursDto();
                int col = 0;
                
                // Read columns: Source Name, Material Code, SAP Code, Plant Name, Apr-Mar (12 months), Remarks, Source ID
                dto.setSourceName(getCellStringValue(row, col++));
                dto.setMaterialCode(getCellStringValue(row, col++));
                dto.setSapCode(getCellStringValue(row, col++));
                dto.setPlantName(getCellStringValue(row, col++));
                
                // Month values
                dto.setApril(getCellDoubleValue(row, col++));
                dto.setMay(getCellDoubleValue(row, col++));
                dto.setJune(getCellDoubleValue(row, col++));
                dto.setJuly(getCellDoubleValue(row, col++));
                dto.setAugust(getCellDoubleValue(row, col++));
                dto.setSeptember(getCellDoubleValue(row, col++));
                dto.setOctober(getCellDoubleValue(row, col++));
                dto.setNovember(getCellDoubleValue(row, col++));
                dto.setDecember(getCellDoubleValue(row, col++));
                dto.setJanuary(getCellDoubleValue(row, col++));
                dto.setFebruary(getCellDoubleValue(row, col++));
                dto.setMarch(getCellDoubleValue(row, col++));
                
                // Remarks and Source ID
                dto.setRemarks(getCellStringValue(row, col++));
                String sourceIdStr = getCellStringValue(row, col++);
                if (sourceIdStr != null && !sourceIdStr.trim().isEmpty()) {
                    try {
                        dto.setSourceId(UUID.fromString(sourceIdStr));
                    } catch (IllegalArgumentException e) {
                        // Skip invalid UUID
                        continue;
                    }
                }
                
                if (dto.getSourceId() != null) {
                    dtoList.add(dto);
                }
            }
        }
        
        return dtoList;
    }

    private List<ImportPowerCapacityDto> parseCapacityFromExcel(java.io.InputStream inputStream) throws Exception {
        List<ImportPowerCapacityDto> dtoList = new ArrayList<>();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // Skip completely blank rows
                boolean isBlank = true;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    if (row.getCell(j) != null && !row.getCell(j).toString().trim().isEmpty()) {
                        isBlank = false;
                        break;
                    }
                }
                if (isBlank) continue;
                
                ImportPowerCapacityDto dto = new ImportPowerCapacityDto();
                int col = 0;
                
                // Read columns: Source Name, Material Code, SAP Code, Plant Name, UOM, Apr-Mar (12 months), Remarks, Source ID
                dto.setSourceName(getCellStringValue(row, col++));
                dto.setMaterialCode(getCellStringValue(row, col++));
                dto.setSapCode(getCellStringValue(row, col++));
                dto.setPlantName(getCellStringValue(row, col++));
                dto.setUom(getCellStringValue(row, col++) != null ? getCellStringValue(row, col - 1) : "MW");
                
                // Month values
                dto.setApril(getCellDoubleValue(row, col++));
                dto.setMay(getCellDoubleValue(row, col++));
                dto.setJune(getCellDoubleValue(row, col++));
                dto.setJuly(getCellDoubleValue(row, col++));
                dto.setAugust(getCellDoubleValue(row, col++));
                dto.setSeptember(getCellDoubleValue(row, col++));
                dto.setOctober(getCellDoubleValue(row, col++));
                dto.setNovember(getCellDoubleValue(row, col++));
                dto.setDecember(getCellDoubleValue(row, col++));
                dto.setJanuary(getCellDoubleValue(row, col++));
                dto.setFebruary(getCellDoubleValue(row, col++));
                dto.setMarch(getCellDoubleValue(row, col++));
                
                // Remarks and Source ID
                dto.setRemarks(getCellStringValue(row, col++));
                String sourceIdStr = getCellStringValue(row, col++);
                if (sourceIdStr != null && !sourceIdStr.trim().isEmpty()) {
                    try {
                        dto.setSourceId(UUID.fromString(sourceIdStr));
                    } catch (IllegalArgumentException e) {
                        // Skip invalid UUID
                        continue;
                    }
                }
                
                if (dto.getSourceId() != null) {
                    dtoList.add(dto);
                }
            }
        }
        
        return dtoList;
    }
    
    private String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    
    private Double getCellDoubleValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String val = cell.getStringCellValue().trim();
                if (val.isEmpty()) return null;
                try {
                    return Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}
