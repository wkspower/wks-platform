package com.wks.caseengine.rest.merox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.merox.service.SignOffReportService;
import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("/task")
public class SignOffReportController {

    @Autowired
    private SignOffReportService signOffReportService;

    @GetMapping(value = "/sign-off-report")
    public ResponseEntity<AOPMessageVM> getSignOffReport(
            @RequestParam String plantId,
            @RequestParam String year,
            @RequestParam(value = "periodFrom", required = false) String periodFrom,
            @RequestParam(value = "periodTo", required = false) String periodTo) {

        AOPMessageVM response = signOffReportService.getSignOffReport(plantId, year, periodFrom, periodTo);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
