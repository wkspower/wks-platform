package com.wks.caseengine.rest.server;


import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wks.caseengine.dto.FixedConsumptionDto;
import com.wks.caseengine.service.FixedConsumptionService;

@RestController
@RequestMapping("task")
public class FixedConsumptionController {

    @Autowired
    private FixedConsumptionService service;

    @GetMapping("/fixed-consumption/{plantId}")
    public List<FixedConsumptionDto> getData(@PathVariable UUID plantId) {
        return service.getData(plantId);
    }

    @PostMapping("/update-fixed-consumption")
    public void updateData(@RequestBody List<FixedConsumptionDto> fixedConsumptionDtoList) {   

        service.updateData(fixedConsumptionDtoList);
    }

    
    //process consumption
}

