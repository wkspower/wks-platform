package com.wks.caseengine.rest.server.cpp;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.cpp.AssetPrioriryDTO;
import com.wks.caseengine.service.cpp.AssetPriorityService;


@RestController
@RequestMapping("task")
public class AssetPriorityController {

    @Autowired
	private AssetPriorityService assetPriorityService; 
	
	@GetMapping(value = "/asset-priority/{plantId}/{financialYear}")
	public ResponseEntity<List<AssetPrioriryDTO>> getAllSites(@PathVariable UUID plantId, @PathVariable String financialYear) {
		List<AssetPrioriryDTO> listOfAssetPriority = assetPriorityService.getAssetPriority(plantId, financialYear);
	    return ResponseEntity.ok(listOfAssetPriority);
	}

    @PostMapping(value = "/asset-priority/{financialYear}")
    public ResponseEntity<Void> saveAssetPriority(@PathVariable String financialYear,   
        @org.springframework.web.bind.annotation.RequestBody List<AssetPrioriryDTO> payload) {

        assetPriorityService.setAssetPriority( payload, financialYear);
        return ResponseEntity.ok().build();
    }

    @PostMapping(  value = "/asset-priority/import-excel/{financialYear}",
    consumes = "multipart/form-data")  
    public ResponseEntity<?> importExcel(
        @RequestParam("file") MultipartFile file, @PathVariable String financialYear) {

    assetPriorityService.importExcel(file, financialYear);
    return ResponseEntity.ok().build();
}



}
