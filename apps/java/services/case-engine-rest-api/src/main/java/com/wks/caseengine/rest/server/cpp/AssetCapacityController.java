package com.wks.caseengine.rest.server.cpp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.cpp.AssetCapacityDTO;
import com.wks.caseengine.service.cpp.AssetCapacityService;

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
    
}
