package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.service.NormParameterMonthlyTransactionService;

@RestController
@RequestMapping("task")
public class NormParameterMonthlyTransactionController{
	
	@Autowired
	private NormParameterMonthlyTransactionService normParameterMonthlyTransactionService;
	
	@GetMapping(value="/getBusinessDemandData")
	public	List<Map<String, Object>> getBusinessDemandData(@RequestParam int year,@RequestParam UUID plantId,@RequestParam UUID siteId){	
		return normParameterMonthlyTransactionService.getBusinessDemandData(year, plantId, siteId);
		
	}
}
