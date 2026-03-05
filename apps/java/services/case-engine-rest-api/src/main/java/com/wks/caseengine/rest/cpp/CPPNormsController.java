package com.wks.caseengine.rest.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.cpp.service.CPPNormsService;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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

    @GetMapping(value = {"/cpp-norms/export", "/cpp-norms/export/{startDate}/{endDate}"})
    public ResponseEntity<byte[]> exportCPPNorms(
            @RequestParam UUID cppPlantId,
            @RequestParam String financialYear,
            @PathVariable(required = false) String startDate,
            @PathVariable(required = false) String endDate) {
        
        log.info("========== EXPORT CPP NORMS REQUEST ==========");
        log.info("Request Parameters - cppPlantId: {}, financialYear: {}", cppPlantId, financialYear);
        log.info("Optional Parameters - startDate: {}, endDate: {}", startDate, endDate);
        
        try {
            byte[] excelFile = cppNormsService.exportCPPNorms(cppPlantId, financialYear, startDate, endDate);
            log.info("Excel file generated successfully, size: {} bytes", excelFile.length);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "CPPNorms_" + financialYear + ".xlsx");
            
            log.info("========== EXPORT RESPONSE SENT ==========");
            return new ResponseEntity<>(excelFile, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error exporting CPP norms: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/cpp-norms/import")
    public ResponseEntity<AOPMessageVM> importCPPNorms(
            @RequestParam UUID cppPlantId,
            @RequestParam String financialYear,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "SYSTEM") String modifiedBy) {
        
        log.info("========== IMPORT CPP NORMS REQUEST ==========");
        log.info("File name: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        log.info("CPPPlantId: {}, FinancialYear: {}, ModifiedBy: {}", cppPlantId, financialYear, modifiedBy);
        
        try {
            AOPMessageVM result = cppNormsService.importExcel(cppPlantId, financialYear, file, modifiedBy);
            log.info("CPP norms import completed with status: {}", result.getCode());
            log.info("========== IMPORT RESPONSE SENT ==========");
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error importing CPP norms: {}", e.getMessage(), e);
            AOPMessageVM errorVM = new AOPMessageVM();
            errorVM.setCode(500);
            errorVM.setMessage("Error importing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorVM);
        }
    }
}
