package com.wks.caseengine.rest.server;
import com.wks.caseengine.dto.CatalystAttributesDTO;
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.service.ConfigurationService;
import com.wks.caseengine.service.NormAttributeTransactionsService;
///
@RestController
@RequestMapping("task")
public class NormAttributeTransactionsController {
	
	@Autowired
	private NormAttributeTransactionsService normAttributeTransactionsService;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@GetMapping(value="/getCatalystSelectivityData")
	public	String getCatalystSelectivityData(@RequestParam String year,@RequestParam UUID plantId,@RequestParam UUID siteId){
		try {
			System.out.println("result for configuration"+configurationService.getConfigurationData(year,plantId));
			return normAttributeTransactionsService.getCatalystSelectivityData(year,plantId);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@PutMapping(value="/updateNormAttributeTransactions")
	public NormAttributeTransactionsDTO updateNormAttributeTransactions(@RequestBody NormAttributeTransactionsDTO normAttributeTransactionsDTO) {
		return normAttributeTransactionsService.updateNormAttributeTransactions(normAttributeTransactionsDTO);
	}
	
	@PutMapping(value="/updateCatalystData")
	public Boolean updateCatalystData(@RequestBody CatalystAttributesDTO catalystAttributesDTO) {
		return normAttributeTransactionsService.updateCatalystData(catalystAttributesDTO);
	}
	
	@PostMapping(value = "/saveCatalystData")
	public Boolean saveCatalystData(@RequestBody CatalystAttributesDTO catalystAttributesDTO) {
		normAttributeTransactionsService.saveCatalystData(catalystAttributesDTO);
		return true;
	}
	
	@DeleteMapping(value="/deleteCatalystData")
	public Boolean deleteCatalystData(@RequestBody CatalystAttributesDTO catalystAttributesDTO) {
		normAttributeTransactionsService.deleteCatalystData(catalystAttributesDTO);
		return true;
	}
	
	

}
