package com.wks.caseengine.rest.cpp;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.CPPSRMappingDTO;
import com.wks.caseengine.cpp.dto.CPPSRMappingImportDTO;
import com.wks.caseengine.cpp.entity.CPPSRMapping;
import com.wks.caseengine.cpp.service.CPPSRMappingService;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.utility.Utility;

@RestController
@RequestMapping("task")
public class CPPSRMappingController {

    private final CPPSRMappingService service;

    public CPPSRMappingController(CPPSRMappingService service) {
        this.service = service;
    }

    @PostMapping("/sr-mapping")
    public ResponseEntity<List<CPPSRMappingDTO>> saveMappings(@RequestBody List<CPPSRMappingDTO> dtoList) {
        List<CPPSRMappingDTO> response = service.saveMappings(dtoList);
        return ResponseEntity.ok(response);
    }

    // GET KPI with filters
    @GetMapping("/sr-mapping")
    public ResponseEntity<List<CPPSRMapping>> getMappingsByFilters(
            @RequestParam String aopYear,
            @RequestParam UUID plantFkId
    ) {
        return ResponseEntity.ok(
                service.getMappingsByFilters(aopYear, plantFkId)
        );
    }

    // EXPORT
    @GetMapping(value = "/sr-mapping/export")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam String aopYear,
            @RequestParam UUID plantFkId) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        service.exportToExcel(outputStream, aopYear, plantFkId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "CPP_SRMapping_" + aopYear + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());
    }

    // IMPORT
    @PostMapping(value = "/sr-mapping/import")
    public ResponseEntity<AOPMessageVM> importFromExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<CPPSRMappingImportDTO> response = service.importFromExcel(file);

        boolean hasFailed = response.stream().anyMatch(r -> r.getSaveStatus() != null && r.getSaveStatus().equalsIgnoreCase("FAILED"));

        AOPMessageVM vm = new AOPMessageVM();
        if (!hasFailed) {
            vm.setCode(200);
            vm.setMessage("All data has been saved");
            vm.setData(null);
            return ResponseEntity.ok(vm);
        }

        byte[] errorFile = exportImportErrors(response);
        String base64File = Base64.getEncoder().encodeToString(errorFile);
        vm.setCode(400);
        vm.setMessage("Partial data has been saved");
        vm.setData(base64File);
        return ResponseEntity.ok(vm);
    }

    private byte[] exportImportErrors(List<CPPSRMappingImportDTO> rows) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("CPP_SRMapping_Import");

            org.apache.poi.ss.usermodel.CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
            org.apache.poi.ss.usermodel.CellStyle dataStyle = Utility.createBorderedStyle(workbook);

            String[] headers = {
                    "Id",
                    "Receiver Utility", "Receiver Utility ID",
                    "Receiver Cost Center", "Receiver Cost Center ID",
                    "Receiver Plant", "Receiver Plant ID",
                    "Sender Cost Center", "Sender Cost Center ID",
                    "Sender Plant", "Sender Plant ID",
                    "Utility", "Utility ID",
                    "Remarks", "AOPYear",
                    "Status", "Error Description"
            };

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // keep template consistent with main export (hidden id)
            sheet.setColumnHidden(0, true);

            int rowNum = 1;
            for (CPPSRMappingImportDTO dto : rows) {
                if (dto.getSaveStatus() == null || !dto.getSaveStatus().equalsIgnoreCase("FAILED")) {
                    continue;
                }
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                int c = 0;

                org.apache.poi.ss.usermodel.Cell idCell = row.createCell(c++);
                idCell.setCellValue(dto.getId() != null ? dto.getId() : "");
                idCell.setCellStyle(dataStyle);

                row.createCell(c++).setCellValue(dto.getReceiverUtility() != null ? dto.getReceiverUtility() : "");
                row.createCell(c++).setCellValue(dto.getReceiverUtilityId() != null ? dto.getReceiverUtilityId() : "");
                row.createCell(c++).setCellValue(dto.getReceiverCostCenter() != null ? dto.getReceiverCostCenter() : "");
                row.createCell(c++).setCellValue(dto.getReceiverCostCenterId() != null ? dto.getReceiverCostCenterId() : "");
                row.createCell(c++).setCellValue(dto.getReceiverPlant() != null ? dto.getReceiverPlant() : "");
                row.createCell(c++).setCellValue(dto.getReceiverPlantId() != null ? dto.getReceiverPlantId() : "");
                row.createCell(c++).setCellValue(dto.getSenderCostCenter() != null ? dto.getSenderCostCenter() : "");
                row.createCell(c++).setCellValue(dto.getSenderCostCenterId() != null ? dto.getSenderCostCenterId() : "");
                row.createCell(c++).setCellValue(dto.getSenderPlant() != null ? dto.getSenderPlant() : "");
                row.createCell(c++).setCellValue(dto.getSenderPlantId() != null ? dto.getSenderPlantId() : "");
                row.createCell(c++).setCellValue(dto.getUtility() != null ? dto.getUtility() : "");
                row.createCell(c++).setCellValue(dto.getUtilityId() != null ? dto.getUtilityId() : "");
                row.createCell(c++).setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                row.createCell(c++).setCellValue(dto.getAopYear() != null ? dto.getAopYear() : "");
                row.createCell(c++).setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                row.createCell(c++).setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");

                // apply borders to all cells except header
                for (int col = 0; col < headers.length; col++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
                    if (cell != null) {
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
        return outputStream.toByteArray();
    }
}
