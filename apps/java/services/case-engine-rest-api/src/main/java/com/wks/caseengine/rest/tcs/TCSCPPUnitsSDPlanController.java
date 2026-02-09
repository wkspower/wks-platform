package com.wks.caseengine.rest.tcs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.tcs.dto.TCSCPPUnitsSDPlanDTO;
import com.wks.caseengine.tcs.service.TCSCPPUnitsSDPlanService;

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

    @PostMapping("/cpp-unit-sd-plan/{financialYear}/{siteId}")
    public ResponseEntity<Void> saveTCSCPPUnitsSDPlan(@RequestBody List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs, @PathVariable String financialYear, @PathVariable String siteId) {
        tcsCppUnitsSDPlanService.saveTCSCPPUnitsSDPlan(tcsCppUnitsSDPlanDTOs, UUID.fromString(siteId), financialYear);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cpp-unit-sd-plan/{id}")
    public ResponseEntity<Void> deleteTCSCPPUnitsSDPlan(@PathVariable String id) {
        tcsCppUnitsSDPlanService.deleteTCSCPPUnitsSDPlan(UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cpp-unit-sd-plan/export/{financialYear}/{siteId}")
    public ResponseEntity<byte[]> exportTCSCPPUnitsSDPlan(
        @PathVariable String financialYear, 
        @PathVariable String siteId) {
        
        byte[] excelBytes = tcsCppUnitsSDPlanService.exportTCSCPPUnitsSDPlan(financialYear, UUID.fromString(siteId));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "TCS_CPP_Units_SD_Plan_" + financialYear + ".xlsx");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(excelBytes);
    }

    @PostMapping("/cpp-unit-sd-plan/import/{financialYear}/{siteId}")
    public ResponseEntity<AOPMessageVM> importExcel(
        @PathVariable String financialYear,
        @PathVariable String siteId,
        @RequestParam("file") MultipartFile file) {
        
        AOPMessageVM response = tcsCppUnitsSDPlanService.importExcel(UUID.fromString(siteId), financialYear, file);
        return ResponseEntity.ok(response);
    }
}
      



