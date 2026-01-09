package com.wks.caseengine.rest.server.cpp;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.AssetOperationalResponseDTO;
import com.wks.caseengine.dto.MasterAssetOperationalResponseDTO;
import com.wks.caseengine.service.cpp.PowerGenerationService;

@RestController
@RequestMapping("/task")
public class PowerGenerationController {

    @Autowired
    private PowerGenerationService powerGenerationService;


    @GetMapping("/assets/operational-hours/{plantId}/{financialYear}")
    public ResponseEntity<MasterAssetOperationalResponseDTO> getAssetOperationalHours(
            @PathVariable UUID plantId,
            @PathVariable String financialYear) {

        MasterAssetOperationalResponseDTO response =
                powerGenerationService.getAssetOperationalHours(plantId, financialYear);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/assets/operational-hours/{financialYear}")
     public ResponseEntity<Void> saveOperationalHours(
        @PathVariable String financialYear,
        @RequestBody MasterAssetOperationalResponseDTO payload) {

    powerGenerationService.setAssetOperationalHours(financialYear, payload);
    return ResponseEntity.ok().build();
}

}