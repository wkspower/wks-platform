package com.wks.caseengine.rest.server;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.AOPSummaryDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.AOPSummaryService;

@RestController
@RequestMapping("task")
public class AOPSummaryController {

    @Autowired
    private AOPSummaryService aopSummaryService;

    @PostMapping("/aop-summary")
    public AOPMessageVM saveAOPSummary(
            @RequestParam("plantId") String plantId,
            @RequestParam("aopYear") String aopYear,
            @RequestBody AOPSummaryDTO aopSummaryDTO) {

        return aopSummaryService.saveAOPSummary(plantId, aopYear, aopSummaryDTO);
    }

    @GetMapping("/aop-summary")
    public AOPMessageVM getAOPSummary(
            @RequestParam("plantId") String plantId,
            @RequestParam("aopYear") String aopYear) {
        return aopSummaryService.getAOPSummary(plantId, aopYear);
    }

}
