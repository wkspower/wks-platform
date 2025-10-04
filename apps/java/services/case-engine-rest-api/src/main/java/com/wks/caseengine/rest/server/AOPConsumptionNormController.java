package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.CalculatedConsumptionNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import com.wks.caseengine.service.AOPConsumptionNormService;

@RestController
@RequestMapping("task")
public class AOPConsumptionNormController {
	
	@Autowired
	private AOPConsumptionNormService aopConsumptionNormService;
	
	@GetMapping(value="/overall-consumption")
	public AOPMessageVM getAOPConsumptionNorm(@RequestParam String plantId,@RequestParam String year,@RequestParam(required = false) String gradeId){
		return aopConsumptionNormService.getAOPConsumptionNorm(plantId,year,gradeId);
	}
	
	@PostMapping(value="/overall-consumption")
	public List<AOPConsumptionNormDTO> saveAOPConsumptionNorm(@RequestBody List<AOPConsumptionNormDTO> aOPConsumptionNormDTOList){
		return aopConsumptionNormService.saveAOPConsumptionNorm(aOPConsumptionNormDTOList);
	}

	@GetMapping(value="/calculate-overall-consumption")
	public AOPMessageVM getNormalOperationNormsDataFromSP(@RequestParam String year,@RequestParam String plantId){
		return	 aopConsumptionNormService.calculateExpressionConsumptionNorms(year,plantId);	
	}
	
	@GetMapping(value="/getCalculatedConsumptionNorms")
	public  List<CalculatedConsumptionNormsDTO>  getCalculatedConsumptionNorms(@RequestParam String year,@RequestParam String plantId){
		return	 aopConsumptionNormService.getCalculatedConsumptionNorms(year,plantId);
	}
	
	@GetMapping(value="/consumption-aop/grades")
	public AOPMessageVM getConsumptionAOPGrades(@RequestParam String year,@RequestParam String plantId){
		return	aopConsumptionNormService.getConsumptionAOPGrades(year, plantId);
	}
	

}
