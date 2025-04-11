package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.dto.ConsumptionNormDTO;
import com.wks.caseengine.service.ConsumptionNormService;

@RestController
@RequestMapping("task")
public class AOPConsumptionNormController {
	
	@Autowired
	private ConsumptionNormService aOPConsumptionNormService;
	
	@GetMapping(value="/aop-consumption-norms")
	public ResponseEntity<AOPMessageVM> getAOPConsumptionNorm(@RequestParam String plantId,@RequestParam String year){
		 AOPMessageVM response	=aOPConsumptionNormService.getAOPConsumptionNorm(plantId,year);
		 return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@PostMapping(value="/aop-consumption-norms")
	public ResponseEntity<AOPMessageVM> saveAOPConsumptionNorm(@RequestBody List<ConsumptionNormDTO> aOPConsumptionNormDTOList){
		AOPMessageVM response	= aOPConsumptionNormService.saveAOPConsumptionNorm(aOPConsumptionNormDTOList);
		return ResponseEntity.status(response.getCode()).body(response);
		
	}

	@GetMapping(value="/aop-consumption-norms/calculate")
	public int getNormalOperationNormsDataFromSP(@RequestParam String year,@RequestParam String plantId){
		return	 aOPConsumptionNormService.calculateExpressionConsumptionNorms(year,plantId);
	}
	
	@GetMapping(value="/aop-consumption-norms/calculated")
	public  ResponseEntity<AOPMessageVM>  getCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId){
		plantId="12345";
		AOPMessageVM response	=	 aOPConsumptionNormService.getCalculatedConsumptionNorms(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);
		
	}
	

}
