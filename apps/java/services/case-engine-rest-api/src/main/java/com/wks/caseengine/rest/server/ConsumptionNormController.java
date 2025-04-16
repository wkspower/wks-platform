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

import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;
import com.wks.caseengine.dto.ConsumptionNormDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ConsumptionNormService;

@RestController
@RequestMapping("api/consumption-norms")
public class ConsumptionNormController {
	
	@Autowired
	private ConsumptionNormService consumptionNormService;

	@GetMapping
	public ResponseEntity<AOPMessageVM> getAOPConsumptionNorm(@RequestParam String plantId,@RequestParam String year){
		 AOPMessageVM response	=consumptionNormService.getAOPConsumptionNorm(plantId,year);
		 return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@PostMapping
	public ResponseEntity<AOPMessageVM> saveAOPConsumptionNorm(@RequestBody List<ConsumptionNormDTO> aOPConsumptionNormDTOList){
		AOPMessageVM response	= consumptionNormService.saveAOPConsumptionNorm(aOPConsumptionNormDTOList);
		return ResponseEntity.status(response.getCode()).body(response);
		
	}

	@GetMapping(value="/sp-calculate")
	public ResponseEntity<AOPMessageVM> getNormalOperationNorms(@RequestParam String year,@RequestParam String plantId){
		AOPMessageVM response =	 consumptionNormService.calculateExpressionConsumptionNorms(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/calculate")
	public  ResponseEntity<AOPMessageVM>  getCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId){
		AOPMessageVM response	=	 consumptionNormService.getCalculatedConsumptionNorms(year,plantId);
		return ResponseEntity.status(response.getCode()).body(response);
	}

}
