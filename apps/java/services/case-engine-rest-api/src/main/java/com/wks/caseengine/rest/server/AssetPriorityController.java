package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.AssetPrioriryDTO;
import com.wks.caseengine.service.AssetPriorityService;


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
}
