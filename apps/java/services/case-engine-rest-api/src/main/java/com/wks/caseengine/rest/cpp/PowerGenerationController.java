package com.wks.caseengine.rest.cpp;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.MasterAssetOperationalResponseDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.cpp.service.PowerGenerationService;

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

    // ========================================
    // POWER RESPONSE EXPORT/IMPORT ENDPOINTS
    // ========================================

    @GetMapping("/assets/power-response/export/{plantId}/{financialYear}")
    public ResponseEntity<byte[]> exportPowerResponse(
            @PathVariable UUID plantId,
            @PathVariable String financialYear) {

        byte[] excelData = powerGenerationService.exportPowerResponse(plantId, financialYear, false, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Power_Generation_" + financialYear + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @PostMapping("/assets/power-response/import/{plantId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> importPowerResponse(
            @PathVariable UUID plantId,
            @PathVariable String financialYear,
            @RequestParam("file") MultipartFile file) {

        AOPMessageVM response = powerGenerationService.importPowerResponseExcel(plantId, financialYear, file);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // STEAM RESPONSE EXPORT/IMPORT ENDPOINTS
    // ========================================

    @GetMapping("/assets/steam-response/export/{plantId}/{financialYear}")
    public ResponseEntity<byte[]> exportSteamResponse(
            @PathVariable UUID plantId,
            @PathVariable String financialYear) {

        byte[] excelData = powerGenerationService.exportSteamResponse(plantId, financialYear, false, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Steam_Generation_" + financialYear + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @PostMapping("/assets/steam-response/import/{plantId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> importSteamResponse(
            @PathVariable UUID plantId,
            @PathVariable String financialYear,
            @RequestParam("file") MultipartFile file) {

        AOPMessageVM response = powerGenerationService.importSteamResponseExcel(plantId, financialYear, file);
        return ResponseEntity.ok(response);
    }

}
