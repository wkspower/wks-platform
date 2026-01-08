package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.service.TCSCPPUnitsSDPlanService;
import com.wks.caseengine.dto.TCSCPPUnitsSDPlanDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("task")
public class TCSCPPUnitsSDPlanController {

    @Autowired
    private TCSCPPUnitsSDPlanService tcsCppUnitsSDPlanService;

    @GetMapping("/cpp-unit-sd-plan/{financialYear}/{siteId}")
    public ResponseEntity<List<TCSCPPUnitsSDPlanDTO>> getTCSCPPUnitsSDPlan(@PathVariable String financialYear, @PathVariable String siteId) {
        List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs = tcsCppUnitsSDPlanService.getTCSCPPUnitsSDPlan(financialYear, UUID.fromString(siteId));

        System.out.println("tcsCppUnitsSDPlanDTOs: " + tcsCppUnitsSDPlanDTOs);
        return ResponseEntity.ok(tcsCppUnitsSDPlanDTOs);
    }

    @PostMapping("/cpp-unit-sd-plan")
    public ResponseEntity<Void> saveTCSCPPUnitsSDPlan(@RequestBody List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs) {
        tcsCppUnitsSDPlanService.saveTCSCPPUnitsSDPlan(tcsCppUnitsSDPlanDTOs);
        return ResponseEntity.ok().build();
    }
}
      


