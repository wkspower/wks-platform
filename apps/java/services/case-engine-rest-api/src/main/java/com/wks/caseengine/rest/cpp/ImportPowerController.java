package com.wks.caseengine.rest.cpp;

import java.io.ByteArrayOutputStream;
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
            importPowerHoursService.upsertImportPowerOperationalHours(data, financialYear);
            
            return ResponseEntity.ok(AOPMessageVM.builder()
                    .code(200)
                    .message("Operational hours imported successfully")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(AOPMessageVM.builder()
                    .code(400)
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
            importPowerCapacityService.upsertImportPowerCapacity(data, financialYear);
            
            return ResponseEntity.ok(AOPMessageVM.builder()
                    .code(200)
                    .message("Capacity imported successfully")
                    .data(null)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(AOPMessageVM.builder()
                    .code(400)
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
        // TODO: Implement Excel parsing for operational hours
        return List.of();
    }

    private List<ImportPowerCapacityDto> parseCapacityFromExcel(java.io.InputStream inputStream) throws Exception {
        // TODO: Implement Excel parsing for capacity
        return List.of();
    }
}
