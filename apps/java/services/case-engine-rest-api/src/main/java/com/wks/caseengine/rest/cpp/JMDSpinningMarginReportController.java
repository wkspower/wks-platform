package com.wks.caseengine.rest.cpp;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.cpp.service.JMDSpinningMarginReportService;
import com.wks.caseengine.message.vm.AOPMessageVM;

import lombok.extern.slf4j.Slf4j;

/**
 * Read-only Spinning Margin Maintained report endpoints.
 *
 *   GET /task/jmd/spinning-margin-report?aopYear=...[&plantIds=...]
 *   GET /task/jmd/spinning-margin-report/export?aopYear=...[&plantIds=...]
 *
 * Reads from {@code dbo.CPPMonthlySpinningMarginMaintained} filtered by the
 * AOP year. plantIds is optional; when omitted, all rows for the year are
 * returned. Rows where Plant_FK_Id IS NULL are common to all plants.
 */
@RestController
@RequestMapping("task")
@Slf4j
public class JMDSpinningMarginReportController {

    @Autowired
    private JMDSpinningMarginReportService jmdSpinningMarginReportService;

    @GetMapping("/jmd/spinning-margin-report")
    public ResponseEntity<?> getSpinningMarginReport(
            @RequestParam(required = false) List<UUID> plantIds,
            @RequestParam String aopYear) {
        try {
            log.info("=== GET Spinning Margin Report Request ===");
            log.info("PlantIds: {}, AOPYear: {}", plantIds, aopYear);

            AOPMessageVM result = jmdSpinningMarginReportService.getSpinningMarginReport(plantIds, aopYear);

            log.info("=== GET Spinning Margin Report Response ===");
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

    @GetMapping(value = "/jmd/spinning-margin-report/export")
    public ResponseEntity<byte[]> exportSpinningMarginReport(
            @RequestParam(required = false) List<UUID> plantIds,
            @RequestParam String aopYear) {

        log.info("[GET /jmd/spinning-margin-report/export] Request received - plantIds: {}, aopYear: {}",
                plantIds, aopYear);

        try {
            byte[] excelData = jmdSpinningMarginReportService.exportSpinningMarginReport(plantIds, aopYear);

            if (excelData == null) {
                log.error("[GET /jmd/spinning-margin-report/export] Failed to generate Excel file");
                return ResponseEntity.status(500).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Spinning_Margin_Report_" + aopYear + ".xlsx");

            log.info("[GET /jmd/spinning-margin-report/export] Successfully generated Excel file, size: {} bytes",
                    excelData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (Exception e) {
            log.error("[GET /jmd/spinning-margin-report/export] Error exporting Spinning Margin report", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
