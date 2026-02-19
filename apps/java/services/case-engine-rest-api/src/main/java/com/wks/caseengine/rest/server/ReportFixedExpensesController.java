package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ReportFixedExpensesDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ReportFixedExpensesService;

@RestController
@RequestMapping("task")
public class ReportFixedExpensesController {
	
	@Autowired
	private ReportFixedExpensesService reportFixedExpensesService;
	
	@GetMapping(value="/report-fixed-expenses")
	public AOPMessageVM getReportFixedExpensesTransaction(@RequestParam String siteId,@RequestParam String year){
		 return  reportFixedExpensesService.getReportFixedExpensesTransaction(siteId,year);
	}
	
	@PostMapping(value="/report-fixed-expenses")
	public AOPMessageVM saveReportFixedExpensesTransaction(@RequestParam String year,@RequestParam String siteId, @RequestBody List<ReportFixedExpensesDTO> ReportFixedExpensesDTOs) {
		return 	reportFixedExpensesService.saveReportFixedExpensesTransaction(year,siteId,ReportFixedExpensesDTOs);
	}

}
