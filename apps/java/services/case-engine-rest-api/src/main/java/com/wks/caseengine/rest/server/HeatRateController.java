package com.wks.caseengine.rest.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.HeatRateDTO;
import com.wks.caseengine.dto.HeatRateDropDownProjection;
import com.wks.caseengine.dto.HeatRateProjection;
import com.wks.caseengine.service.HeatRateService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


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

}
