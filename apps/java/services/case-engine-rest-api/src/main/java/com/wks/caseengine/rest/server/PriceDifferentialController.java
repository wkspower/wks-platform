package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.PriceDifferentialTransactionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PackagingConsumablesService;
import com.wks.caseengine.service.PriceDifferentialService;

@RestController
@RequestMapping("task")
public class PriceDifferentialController {
	
	@Autowired
	private PriceDifferentialService priceDifferentialService;
	
	@GetMapping(value="/price-differential")
	public AOPMessageVM getPriceDifferential(@RequestParam String plantId,@RequestParam String year){
		 return  priceDifferentialService.getPriceDifferential(plantId,year);
	}
	
	@GetMapping(value="/price-differential-transaction")
	public AOPMessageVM getPriceDifferentialTransaction(@RequestParam String plantId,@RequestParam String year){
		 return  priceDifferentialService.getPriceDifferentialTransaction(plantId,year);
	}
	
	@PostMapping(value="/price-differential")
	public AOPMessageVM savePriceDifferential(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<ConfigurationDTO> configurationDTOList) {
		return 	priceDifferentialService.savePriceDifferential(year,plantFKId,configurationDTOList);
	}
	
	@PostMapping(value="/price-differential-transaction")
	public AOPMessageVM savePriceDifferentialTransaction(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<PriceDifferentialTransactionDTO> priceDifferentialTransactionDTO) {
		return 	priceDifferentialService.savePriceDifferentialTransaction(year,plantFKId,priceDifferentialTransactionDTO);
	}
	
}
