package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.AssetOperationalResponseDTO;
import com.wks.caseengine.service.PowerGenerationService;

@RestController
@RequestMapping("/task")
public class PowerGenerationController {

    @Autowired
    private PowerGenerationService powerGenerationService;


    @GetMapping("/assets/operational-hours/{plantId}/{financialYear}")
    public ResponseEntity<List<AssetOperationalResponseDTO>> getAssetOperationalHours(
            @PathVariable UUID plantId,
            @PathVariable String financialYear) {

        List<AssetOperationalResponseDTO> response =
                powerGenerationService.getAssetOperationalHours(plantId, financialYear);

        return ResponseEntity.ok(response);
    }
}

