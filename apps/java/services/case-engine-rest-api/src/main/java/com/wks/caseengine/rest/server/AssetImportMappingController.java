package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.dto.PlantImportMappingDto;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PlantImportMappingServiceImpl;

@RestController
@RequestMapping("task")
public class AssetImportMappingController {

    // @Autowired
    // private AssetImportMappingService assetImportMappingService;

    @Autowired
    private PlantImportMappingServiceImpl plantImportMappingService;

    @GetMapping("/asset-import-mapping/{cppPlantId}/{financialYear}")
    public List<PlantImportMappingDto> getPivotData(@PathVariable String cppPlantId, @PathVariable String financialYear) {
        return plantImportMappingService.getPivotData(cppPlantId, financialYear);
    }

    @PostMapping("/asset-import-mapping/{cppPlantId}/{financialYear}")
    public AOPMessageVM savePlantImportMapping(@PathVariable String cppPlantId,
                                               @PathVariable String financialYear,
                                               @RequestBody List<PlantImportMappingDto> payload) {
        plantImportMappingService.upsertPlantImportMapping(payload, cppPlantId, financialYear);
        return AOPMessageVM.builder().code(0).message("OK").data(null).build();
    }

    // @PostMapping("/asset-import-mapping")
    // public AOPMessageVM savePivotData(@RequestBody AssetImportMappingPivotDTO payload) {
    //     return assetImportMappingService.savePivotData(payload);
    // }

}