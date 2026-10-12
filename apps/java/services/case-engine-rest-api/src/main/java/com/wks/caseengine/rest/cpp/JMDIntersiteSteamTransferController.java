package com.wks.caseengine.rest.cpp;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import com.wks.caseengine.cpp.dto.IntersiteSteamTransferDto;
import com.wks.caseengine.cpp.service.IntersiteSteamTransferService;
import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("/task")
public class JMDIntersiteSteamTransferController {

    private static final Logger logger = LoggerFactory.getLogger(JMDIntersiteSteamTransferController.class);

    @Autowired
    private IntersiteSteamTransferService intersiteSteamTransferService;

    // ========================================
    // GET INTERSITE STEAM TRANSFER
    // ========================================

    @GetMapping("/jmd/intersite-steam-transfer")
    public AOPMessageVM getIntersiteSteamTransfer(
            @RequestParam String financialYear) {

        logger.info("[GET /jmd/intersite-steam-transfer] financialYear: {}", financialYear);

        AOPMessageVM response = intersiteSteamTransferService
                .getIntersiteSteamTransfer(financialYear);

        logger.info("[GET /jmd/intersite-steam-transfer] code: {}, message: {}",
                response.getCode(), response.getMessage());

        return response;
    }

    // ========================================
    // POST (SAVE OR UPDATE) INTERSITE STEAM TRANSFER
    // ========================================

    @PostMapping("/jmd-saveOrUpdateIntersiteSteamTransfer/{financialYear}")
    public AOPMessageVM saveIntersiteSteamTransfer(
            @RequestParam List<UUID> plantIds,
            @PathVariable String financialYear,
            @RequestBody List<IntersiteSteamTransferDto> payload) {

        logger.info("[POST /jmd-saveOrUpdateIntersiteSteamTransfer/{}] plantIds: {}, records: {}",
                financialYear, plantIds, payload != null ? payload.size() : 0);

        AOPMessageVM response = intersiteSteamTransferService
                .saveIntersiteSteamTransfer(plantIds, financialYear, payload);

        logger.info("[POST /jmd-saveOrUpdateIntersiteSteamTransfer/{}] code: {}, message: {}",
                financialYear, response.getCode(), response.getMessage());

        return response;
    }

    // ========================================
    // EXPORT INTERSITE STEAM TRANSFER
    // ========================================

    @GetMapping("/jmd/intersite-steam-transfer/export")
    public ResponseEntity<byte[]> exportIntersiteSteamTransfer(
            @RequestParam List<UUID> plantIds,
            @RequestParam String financialYear) {

        logger.info("[GET /jmd/intersite-steam-transfer/export] plantIds: {}, financialYear: {}",
                plantIds, financialYear);

        try {
            byte[] excelData = intersiteSteamTransferService
                    .exportIntersiteSteamTransfer(plantIds, financialYear);

            if (excelData == null || excelData.length == 0) {
                logger.error("[GET /jmd/intersite-steam-transfer/export] Failed to generate Excel");
                return ResponseEntity.status(500).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    "Intersite_Steam_Transfer_" + financialYear + ".xlsx");

            logger.info("[GET /jmd/intersite-steam-transfer/export] Excel generated, {} bytes",
                    excelData.length);

            return ResponseEntity.ok().headers(headers).body(excelData);
        } catch (Exception e) {
            logger.error("[GET /jmd/intersite-steam-transfer/export] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // ========================================
    // IMPORT INTERSITE STEAM TRANSFER
    // ========================================

    @PostMapping(value = "/jmd/intersite-steam-transfer/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AOPMessageVM> importIntersiteSteamTransfer(
            @RequestParam List<UUID> plantIds,
            @RequestParam String financialYear,
            @RequestParam("file") MultipartFile file) {

        logger.info("[POST /jmd/intersite-steam-transfer/import] plantIds: {}, financialYear: {}, file: {}",
                plantIds, financialYear, file != null ? file.getOriginalFilename() : "null");

        if (file == null || file.isEmpty()) {
            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(400);
            errorResponse.setMessage("File is required");
            errorResponse.setData(null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            AOPMessageVM response = intersiteSteamTransferService
                    .importIntersiteSteamTransfer(plantIds, financialYear, file);
            logger.info("[POST /jmd/intersite-steam-transfer/import] code: {}, message: {}",
                    response.getCode(), response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[POST /jmd/intersite-steam-transfer/import] Error: {}", e.getMessage(), e);
            AOPMessageVM errorResponse = new AOPMessageVM();
            errorResponse.setCode(500);
            errorResponse.setMessage("Failed to import data: " + e.getMessage());
            errorResponse.setData(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
