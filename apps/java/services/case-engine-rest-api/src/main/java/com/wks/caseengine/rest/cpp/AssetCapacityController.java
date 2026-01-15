package com.wks.caseengine.rest.cpp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.AssetCapacityDTO;
import com.wks.caseengine.cpp.service.AssetCapacityService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("task")
public class AssetCapacityController {
  
    @Autowired
    private AssetCapacityService assetCapacityService;
      
    @GetMapping("/asset-capacity/{cppPlantId}/{financialYear}")
    public ResponseEntity<List<AssetCapacityDTO>> getAssetCapacityByCppAndFY(@PathVariable String cppPlantId, @PathVariable String financialYear) {

        return ResponseEntity.ok(assetCapacityService.getAssetCapacityByCppAndFY(cppPlantId, financialYear));
    }

    @PostMapping("/asset-capacity/{financialYear}")
    public void updateAssetCapacity(@PathVariable String financialYear, @RequestBody List<AssetCapacityDTO> assetCapacities) {
       
        assetCapacityService.updateAssetCapacities(assetCapacities, financialYear);
          
    }

    @PostMapping(value = "/asset-capacity/import-excel/{financialYear}",
    consumes = "multipart/form-data")
    public ResponseEntity<?> importExcel( @RequestParam("file") MultipartFile file, @PathVariable String financialYear) {
        assetCapacityService.importExcel(file, financialYear);
        return ResponseEntity.ok().build();
    }
}

