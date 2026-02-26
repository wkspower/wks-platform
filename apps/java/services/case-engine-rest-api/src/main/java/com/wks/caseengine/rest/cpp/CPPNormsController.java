package com.wks.caseengine.rest.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.cpp.service.CPPNormsService;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("task")
@Slf4j
public class CPPNormsController {

    @Autowired
    private CPPNormsService cppNormsService;

    @GetMapping("/cpp-norms")
    public ResponseEntity<?> getCPPNorms(
            @RequestParam UUID cppPlantId,
            @RequestParam String financialYear,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        try {
            log.info("=== GET CPP Norms Request ===");
            log.info("CPPPlantId: {}, FinancialYear: {}, StartDate: {}, EndDate: {}", cppPlantId, financialYear, startDate, endDate);

            AOPMessageVM result = cppNormsService.getCPPNorms(cppPlantId, financialYear, startDate, endDate);

            log.info("=== GET CPP Norms Response ===");
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

    @PostMapping("/cpp-norms/{financialYear}")
    public ResponseEntity<?> saveOrUpdateCPPNorms(
            @RequestBody List<CPPNormsRequestDTO> dtoList,
            @PathVariable String financialYear,
            @RequestParam(required = false, defaultValue = "SYSTEM") String modifiedBy
    ) {
        try {
            log.info("=== POST CPPNorms Request ===");
            log.info("FinancialYear: {}, ModifiedBy: {}", financialYear, modifiedBy);

            if (dtoList == null || dtoList.isEmpty()) {
                AOPMessageVM errorResponse = new AOPMessageVM();
                errorResponse.setCode(400);
                errorResponse.setMessage("Request body cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            log.info("Total records received: {}", dtoList.size());

            AOPMessageVM response = cppNormsService.saveOrUpdateCPPNorms(dtoList, financialYear, modifiedBy);

            if (response.getCode() == 200 || response.getCode() == 207) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getCode()).body(response);
            }

        } catch (RestInvalidArgumentException e) {
            log.error("Validation error: {}", e.getMessage());

            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(400);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("=== ERROR in saveOrUpdateCPPNorms ===", e);

            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(500);
            errorResponse.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping(value = "/cpp-norms/export")
    public ResponseEntity<byte[]> exportCPPNorms(
            @RequestParam UUID cppPlantId,
            @RequestParam String financialYear) {

        byte[] excelFile = cppNormsService.exportCPPNorms(cppPlantId, financialYear, false, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "CPPNorms_" + financialYear + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

    @PostMapping(value = "/cpp-norms/import")
    public ResponseEntity<AOPMessageVM> importCPPNorms(
            @RequestParam UUID cppPlantId,
            @RequestParam String financialYear,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "SYSTEM") String modifiedBy) {

        AOPMessageVM result = cppNormsService.importExcel(cppPlantId, financialYear, file, modifiedBy);
        return ResponseEntity.ok(result);
    }
}
