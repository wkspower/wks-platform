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
import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.service.AOPConsumptionNormService;

@RestController
@RequestMapping("task")
public class AOPConsumptionNormController {
	
	@Autowired
	private AOPConsumptionNormService aOPConsumptionNormService;
	
	@GetMapping(value="/aop-consumption-norms")
	public ResponseEntity<List<AOPConsumptionNormDTO>> getAOPConsumptionNorm(@RequestParam String plantId,@RequestParam String year){
		List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList	=aOPConsumptionNormService.getAOPConsumptionNorm(plantId,year);
		return ResponseEntity.ok(aOPConsumptionNormDTOList);
	}
	
	@PostMapping(value="/aop-consumption-norms")
	public List<AOPConsumptionNormDTO> saveAOPConsumptionNorm(@RequestBody List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList){
		return aOPConsumptionNormService.saveAOPConsumptionNorm(aOPConsumptionNormDTOList);
	}

	@GetMapping(value="/aop-consumption-norms/calculate")
	public int getNormalOperationNormsDataFromSP(@RequestParam String year,@RequestParam String plantId){
		return	 aOPConsumptionNormService.calculateExpressionConsumptionNorms(year,plantId);
	}
	
	@GetMapping(value="/aop-consumption-norms/calculated")
	public  List<CalculatedConsumptionNormsDTO>  getCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId){
		return	 aOPConsumptionNormService.getCalculatedConsumptionNorms(year,plantId);
		
	}
	

}
