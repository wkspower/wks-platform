package com.wks.caseengine.rest.server.cpp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.cpp.AssetCapacityDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
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

    @GetMapping(value = "/asset-capacity/export/{cppId}/{financialYear}")
    public ResponseEntity<byte[]> exportAssetCapacity(@PathVariable String cppId, @PathVariable String financialYear) {
        byte[] excelFile = assetCapacityService.exportAssetCapacity(cppId, financialYear, false, null);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "AssetCapacity_" + financialYear + ".xlsx");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelFile);
    }

    @PostMapping(value = "/asset-capacity/import/{cppId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> importAssetCapacity(
            @PathVariable String cppId, 
            @PathVariable String financialYear,
            @RequestParam("file") MultipartFile file) {
        
        AOPMessageVM result = assetCapacityService.importExcel(cppId, financialYear, file);
        return ResponseEntity.ok(result);
    }
  
}
