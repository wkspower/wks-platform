package com.wks.caseengine.rest.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.HeatRateDTO;
import com.wks.caseengine.dto.HeatRateDropDownProjection;
import com.wks.caseengine.dto.HeatRateProjection;
import com.wks.caseengine.dto.HRSGHeatRateLookupDTO;
import com.wks.caseengine.dto.STGExtractionLookupDTO;
import com.wks.caseengine.service.HeatRateService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("task")
public class HeatRateController {
  
    @Autowired
    private HeatRateService heatRateService;
    
    @GetMapping("/heat-rate/drop-down/{cppId}")
    public ResponseEntity<List<Object[]>> getMethodName(@PathVariable String cppId) {
        return   ResponseEntity.ok(heatRateService.getAssetNamesByCppIdAndAssetType(cppId));
    }



    @GetMapping("/heat-rate/{assetId}")
    public ResponseEntity<List<HeatRateDTO>> getHeatRateByAssetId(@PathVariable String assetId) {
        return ResponseEntity.ok(heatRateService.getHeatRateByAssetId(assetId));
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
        heatRateService.updateHeatRate(heatRateDTOs);
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

}
