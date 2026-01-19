package com.wks.caseengine.rest.cpp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.PlantImportMappingDto;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.cpp.serviceimpl.PlantImportMappingServiceImpl;

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

    @GetMapping(value = "/asset-import-mapping/export/{cppPlantId}/{financialYear}")
    public ResponseEntity<byte[]> exportPlantImportMapping(@PathVariable String cppPlantId, @PathVariable String financialYear) {
        byte[] excelFile = plantImportMappingService.exportPlantImportMapping(cppPlantId, financialYear, false, null);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "PlantImportMapping_" + financialYear + ".xlsx");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelFile);
    }

    @PostMapping(value = "/asset-import-mapping/import/{cppPlantId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> importPlantImportMapping(
            @PathVariable String cppPlantId, 
            @PathVariable String financialYear,
            @RequestParam("file") MultipartFile file) {
        
        AOPMessageVM result = plantImportMappingService.importExcel(cppPlantId, financialYear, file);
        return ResponseEntity.ok(result);
    }

    // @PostMapping("/asset-import-mapping")
    // public AOPMessageVM savePivotData(@RequestBody AssetImportMappingPivotDTO payload) {
    //     return assetImportMappingService.savePivotData(payload);
    // }

}

