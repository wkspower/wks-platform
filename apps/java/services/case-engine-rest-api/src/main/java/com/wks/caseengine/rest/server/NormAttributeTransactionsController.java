package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.service.NormAttributeTransactionsService;
///
@RestController
@RequestMapping("task")
public class NormAttributeTransactionsController {
	
	@Autowired
	private NormAttributeTransactionsService normAttributeTransactionsService;
	
	@GetMapping(value="/getCatalystSelectivityData")
	public	List<Map<String, Object>> getCatalystSelectivityData(@RequestParam int year,@RequestParam UUID plantId,@RequestParam UUID siteId){	
		return normAttributeTransactionsService.getCatalystSelectivityData(year);
		
	}

}
