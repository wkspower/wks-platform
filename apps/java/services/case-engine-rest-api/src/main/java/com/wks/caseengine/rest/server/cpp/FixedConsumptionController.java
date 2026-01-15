package com.wks.caseengine.rest.server.cpp;


import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.cpp.FixedConsumptionDto;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.cpp.FixedConsumptionService;

@RestController
@RequestMapping("task")
public class FixedConsumptionController {

    @Autowired
    private FixedConsumptionService service;

    @GetMapping("/fixed-consumption/{plantId}/{financialYear}")
    public List<FixedConsumptionDto> getData(@PathVariable UUID plantId, @PathVariable String financialYear) {
        return service.getData(plantId, financialYear);
    }

    @PostMapping("/update-fixed-consumption/{financialYear}")
    public void updateData(@PathVariable String financialYear, @RequestBody List<FixedConsumptionDto> fixedConsumptionDtoList) {   

        service.updateData(fixedConsumptionDtoList, financialYear);
    }

    @GetMapping(value = "/fixed-consumption/export/{plantId}/{financialYear}")
    public ResponseEntity<byte[]> exportFixedConsumption(@PathVariable UUID plantId, @PathVariable String financialYear) {
        byte[] excelFile = service.exportFixedConsumption(plantId, financialYear, false, null);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "FixedConsumption_" + financialYear + ".xlsx");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelFile);
    }

    @PostMapping(value = "/fixed-consumption/import/{plantId}/{financialYear}")
    public ResponseEntity<AOPMessageVM> importFixedConsumption(
            @PathVariable UUID plantId, 
            @PathVariable String financialYear,
            @RequestParam("file") MultipartFile file) {
        
        AOPMessageVM result = service.importExcel(plantId, financialYear, file);
        return ResponseEntity.ok(result);
    }
    
    //process consumption
}

