package com.wks.caseengine.rest.cpp;

import java.io.IOException;

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

import com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateLookupDTO;
import com.wks.caseengine.cpp.dto.heatrate.HeatRateDTO;
import com.wks.caseengine.cpp.dto.heatrate.STGExtractionLookupDTO;
import com.wks.caseengine.cpp.service.HeatRateService;

import java.util.List;


@RestController
@RequestMapping("task")
public class HeatRateController {
  
    private static final Logger logger = LoggerFactory.getLogger(HeatRateController.class);
    
    @Autowired
    private HeatRateService heatRateService;
    
    @GetMapping("/heat-rate/drop-down/{cppId}")
    public ResponseEntity<List<Object[]>> getMethodName(@PathVariable String cppId) {
        return   ResponseEntity.ok(heatRateService.getAssetNamesByCppIdAndAssetType(cppId));
    }



    @GetMapping("/heat-rate/{assetId}/{financialYear}")
    public ResponseEntity<List<HeatRateDTO>> getHeatRateByAssetId(@PathVariable String assetId, @PathVariable String financialYear) {
        logger.info("========== GET HEAT RATE REQUEST ==========");
        logger.info("Request Parameters - assetId: {}, financialYear: {}", assetId, financialYear);
        
        List<HeatRateDTO> result = heatRateService.getHeatRateByAssetId(assetId, financialYear);
        
        logger.info("Service returned {} records", result != null ? result.size() : 0);
        if (result != null && !result.isEmpty()) {
            HeatRateDTO firstRecord = result.get(0);
            logger.info("First record details:");
            logger.info("  - id: {}", firstRecord.getId());
            logger.info("  - equipType: {}", firstRecord.getEquipType());
            logger.info("  - gtLoad: {}", firstRecord.getGtLoad());
            logger.info("  - heatRate: {}", firstRecord.getHeatRate());
            logger.info("  - previousYearHeatRate: {}", firstRecord.getPreviousYearHeatRate());
            logger.info("  - finalHeatRate: {}", firstRecord.getFinalHeatRate());
            logger.info("  - freeSteamFactor: {}", firstRecord.getFreeSteamFactor());
        }
        logger.info("========== RESPONSE BEING SENT ==========");
        
        return ResponseEntity.ok(result);
    }

         // *****************
         
    @GetMapping("/stg-extraction-lookup")
    public ResponseEntity<List<STGExtractionLookupDTO>> getSTGExtractionLookup() {
        return ResponseEntity.ok(heatRateService.getSTGExtractionLookup());
    }

    // ============================================================
    // HRSG HEAT RATE LOOKUP ENDPOINTS
    // ============================================================

    @GetMapping("/hrsg-heat-rate-lookup")
    public ResponseEntity<List<HRSGHeatRateLookupDTO>> getHRSGHeatRateLookup() {
        return ResponseEntity.ok(heatRateService.getHRSGHeatRateLookup());
    }

    @GetMapping("/hrsg-heat-rate-lookup/equipment/{equipmentName}")
    public ResponseEntity<List<HRSGHeatRateLookupDTO>> getHRSGHeatRateByEquipmentName(@PathVariable String equipmentName) {
        return ResponseEntity.ok(heatRateService.getHRSGHeatRateByEquipmentName(equipmentName));
    }

    @GetMapping("/hrsg-heat-rate-lookup/cpp-utility/{cppUtility}")
    public ResponseEntity<List<HRSGHeatRateLookupDTO>> getHRSGHeatRateByCppUtility(@PathVariable String cppUtility) {
        return ResponseEntity.ok(heatRateService.getHRSGHeatRateByCppUtility(cppUtility));
    }


    // ============== Update Methods ====================

    @PostMapping("/heat-rate/{financialYear}")
    public ResponseEntity<Void> updateHeatRate(@RequestBody List<HeatRateDTO> heatRateDTOs, @PathVariable String financialYear) {
        logger.info("========== UPDATE HEAT RATE REQUEST ==========");
        logger.info("Request Parameters - financialYear: {}", financialYear);
        logger.info("Received {} heat rate records to update", heatRateDTOs != null ? heatRateDTOs.size() : 0);
        
        if (heatRateDTOs != null && !heatRateDTOs.isEmpty()) {
            HeatRateDTO firstRecord = heatRateDTOs.get(0);
            logger.info("First record to update:");
            logger.info("  - id: {}", firstRecord.getId());
            logger.info("  - gtLoad: {}", firstRecord.getGtLoad());
            logger.info("  - heatRate: {}", firstRecord.getHeatRate());
            logger.info("  - finalHeatRate: {}", firstRecord.getFinalHeatRate());
            logger.info("  - freeSteamFactor: {}", firstRecord.getFreeSteamFactor());
        }
        
        heatRateService.updateHeatRate(heatRateDTOs);
        logger.info("Heat rate update completed successfully");
        logger.info("==========================================");
        
        return ResponseEntity.ok().build();
    }

    // update stg extraction
    @PostMapping("/stg-extraction-lookup/{financialYear}")
    public ResponseEntity<Void> updateSTGExtraction(@RequestBody List<STGExtractionLookupDTO> stgExtractionLookupDTOs, @PathVariable String financialYear) {
        heatRateService.updateSTGExtraction(stgExtractionLookupDTOs);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/hrsg-heat-rate-lookup/{financialYear}")
    public ResponseEntity<Void> updateHRSGHeatRate(@RequestBody List<HRSGHeatRateLookupDTO> hrsgHeatRateLookupDTOs, @PathVariable String financialYear) {
        heatRateService.updateHRSGHeatRate(hrsgHeatRateLookupDTOs);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // EXPORT ENDPOINTS
    // ============================================================

    @GetMapping("/hrsg-heat-rate-lookup/export")
    public ResponseEntity<byte[]> exportHRSGHeatRateLookup() {
        try {
            byte[] excelData = heatRateService.exportHRSGHeatRateLookup();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "HRSG_Heat_Rate_Lookup.xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stg-extraction-lookup/export")
    public ResponseEntity<byte[]> exportSTGExtractionLookup() {
        try {
            byte[] excelData = heatRateService.exportSTGExtractionLookup();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "STG_Extraction_Lookup.xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/heat-rate/export/{assetId}/{financialYear}")
    public ResponseEntity<byte[]> exportHeatRate(@PathVariable String assetId, @PathVariable String financialYear) {
        try {
            byte[] excelData = heatRateService.exportHeatRate(assetId, financialYear);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "Heat_Rate.xlsx");
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============================================================
    // IMPORT ENDPOINTS
    // ============================================================

    @PostMapping("/hrsg-heat-rate-lookup/import")
    public ResponseEntity<Void> importHRSGHeatRateLookup(@RequestParam("file") MultipartFile file) {
        try {
            heatRateService.importHRSGHeatRateLookup(file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/stg-extraction-lookup/import")
    public ResponseEntity<Void> importSTGExtractionLookup(@RequestParam("file") MultipartFile file) {
        try {
            heatRateService.importSTGExtractionLookup(file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/heat-rate/import")
    public ResponseEntity<Void> importHeatRate(@RequestParam("file") MultipartFile file) {
        try {
            heatRateService.importHeatRate(file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

