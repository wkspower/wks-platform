package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.dto.AssetImportMappingPivotDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.AssetImportMappingService;

@RestController
@RequestMapping("task")
public class AssetImportMappingController {

    @Autowired
    private AssetImportMappingService assetImportMappingService;

    @GetMapping("/asset-import-mapping")
    public AOPMessageVM getPivotData(@RequestParam String financialYear) {
        return assetImportMappingService.getPivotData(financialYear);
    }

    @PostMapping("/asset-import-mapping")
    public AOPMessageVM savePivotData(@RequestBody AssetImportMappingPivotDTO payload) {
        return assetImportMappingService.savePivotData(payload);
    }
}
