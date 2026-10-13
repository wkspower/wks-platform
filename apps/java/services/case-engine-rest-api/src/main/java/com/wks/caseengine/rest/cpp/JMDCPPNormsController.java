package com.wks.caseengine.rest.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.cpp.service.JMDCPPNormsService;
import com.wks.caseengine.message.vm.AOPMessageVM;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("task")
@Slf4j
public class JMDCPPNormsController {

    @Autowired
    private JMDCPPNormsService jmdCppNormsService;

    @GetMapping("/jmd/cpp-norms")
    public ResponseEntity<?> getCPPNormsForPlants(
            @RequestParam List<UUID> plantIds,
            @RequestParam String financialYear,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        try {
            log.info("=== GET JMD CPP Norms Request ===");
            log.info("PlantIds: {}, FinancialYear: {}, StartDate: {}, EndDate: {}", plantIds, financialYear, startDate, endDate);

            AOPMessageVM result = jmdCppNormsService.getCPPNormsForPlants(plantIds, financialYear, startDate, endDate);

            log.info("=== GET JMD CPP Norms Response ===");
            log.info("Response Code: {}", result.getCode());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("=== CONTROLLER EXCEPTION ===", e);

            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(500);
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/jmd/cpp-norms/{financialYear}")
    public ResponseEntity<?> saveOrUpdateCPPNorms(
            @RequestBody List<CPPNormsRequestDTO> dtoList,
            @PathVariable String financialYear,
            @RequestParam(required = false, defaultValue = "SYSTEM") String modifiedBy
    ) {
        try {
            log.info("=== POST JMD CPPNorms Request ===");
            log.info("FinancialYear: {}, ModifiedBy: {}", financialYear, modifiedBy);

            if (dtoList == null || dtoList.isEmpty()) {
                AOPMessageVM errorResponse = new AOPMessageVM();
                errorResponse.setCode(400);
                errorResponse.setMessage("Request body cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Total records received: {}", dtoList.size());

            AOPMessageVM response = jmdCppNormsService.saveOrUpdateCPPNorms(dtoList, financialYear, modifiedBy);

            if (response.getCode() == 200 || response.getCode() == 207) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }

        } catch (Exception e) {
            log.error("=== ERROR in saveOrUpdateCPPNorms (JMD) ===", e);

            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(500);
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/jmd/cpp-norms/export")
    public ResponseEntity<byte[]> exportCPPNorms(
            @RequestParam List<UUID> plantIds,
            @RequestParam String financialYear,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("[GET /jmd/cpp-norms/export] Request received - plantIds: {}, financialYear: {}, startDate: {}, endDate: {}",
                plantIds, financialYear, startDate, endDate);

        try {
            byte[] excelData = jmdCppNormsService.exportCPPNorms(plantIds, financialYear, startDate, endDate);

            if (excelData == null) {
                log.error("[GET /jmd/cpp-norms/export] Failed to generate Excel file");
                return ResponseEntity.status(500).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "CPP_Norms_" + financialYear + ".xlsx");

            log.info("[GET /jmd/cpp-norms/export] Successfully generated Excel file, size: {} bytes", excelData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (Exception e) {
            log.error("[GET /jmd/cpp-norms/export] Error exporting CPP norms", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/jmd/cpp-norms/import")
    public ResponseEntity<AOPMessageVM> importCPPNorms(
            @RequestParam List<UUID> plantIds,
            @RequestParam String financialYear,
            @RequestParam("file") MultipartFile file) {
        log.info("[POST /jmd/cpp-norms/import] plantIds={}, financialYear={}, file={}",
                plantIds, financialYear, file.getOriginalFilename());

        if (file == null || file.isEmpty()) {
            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(400);
            errorResponse.setMessage("File cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            String modifiedBy = "system";
            AOPMessageVM result = jmdCppNormsService.importCPPNorms(plantIds, financialYear, file, modifiedBy);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[POST /jmd/cpp-norms/import] Error importing CPP norms", e);
            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(500);
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/jmd/cpp-norms/calculate")
    public ResponseEntity<?> calculateCPPNorms(
            @RequestParam List<UUID> plantIds,
            @RequestParam String financialYear,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        try {
            log.info("=== POST JMD CPP Norms Calculate Request ===");
            log.info("PlantIds: {}, FinancialYear: {}, StartDate: {}, EndDate: {}", plantIds, financialYear, startDate, endDate);

            AOPMessageVM result = jmdCppNormsService.calculateCPPNorms(plantIds, financialYear, startDate, endDate);

            log.info("=== POST JMD CPP Norms Calculate Response ===");
            log.info("Response Code: {}", result.getCode());

            if (result.getCode() == 200) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(result.getCode()).body(result);
            }
        } catch (Exception e) {
            log.error("=== ERROR in calculateCPPNorms (JMD) ===", e);

            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(500);
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
