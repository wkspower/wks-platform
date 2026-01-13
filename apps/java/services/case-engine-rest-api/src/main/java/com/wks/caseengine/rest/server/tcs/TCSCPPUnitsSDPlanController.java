package com.wks.caseengine.rest.server.tcs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.tcs.TCSCPPUnitsSDPlanDTO;
import com.wks.caseengine.service.tcs.TCSCPPUnitsSDPlanService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @PostMapping("/cpp-unit-sd-plan/{siteId}")
    public ResponseEntity<Void> saveTCSCPPUnitsSDPlan(@RequestBody List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs, @PathVariable String siteId) {
        tcsCppUnitsSDPlanService.saveTCSCPPUnitsSDPlan(tcsCppUnitsSDPlanDTOs, UUID.fromString(siteId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cpp-unit-sd-plan/{id}")
    public ResponseEntity<Void> deleteTCSCPPUnitsSDPlan(@PathVariable String id) {
        tcsCppUnitsSDPlanService.deleteTCSCPPUnitsSDPlan(UUID.fromString(id));
        return ResponseEntity.ok().build();
    }
}
      


