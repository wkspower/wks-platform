package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.service.NormalOperationNormsService;

@RestController
@RequestMapping("task")
public class NormalOperationNormsController {
	
	@Autowired
	private NormalOperationNormsService normalOperationNormsService;
	
	@GetMapping(value="/normal-ops-norms")
	public List<MCUNormsValueDTO> getNormalOperationNormsData(@RequestParam String year,@RequestParam String plantId){
		return	normalOperationNormsService.getNormalOperationNormsData(year, plantId);
	}
	
	@PostMapping(value="/normal-ops-norms")
	public List<MCUNormsValueDTO> saveNormalOperationNormsData(@RequestBody List<MCUNormsValueDTO> mCUNormsValueDTOList){
		try {
			return	normalOperationNormsService.saveNormalOperationNormsData(mCUNormsValueDTOList);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@GetMapping(value="/normal-ops-norms/calculate")
	public int getNormalOperationNormsDataFromSP(@RequestParam String year,@RequestParam String plantId){
		return normalOperationNormsService.calculateExpressionConsumptionNorms(year,plantId);
	}
	
	// @GetMapping(value="/getCalculatedNormalOpsNorms")
	// public List<Object[]> getCalculatedNormalOpsNorms(@RequestParam String year,@RequestParam String plantId){
	// 	return normalOperationNormsService.getCalculatedNormalOpsNorms(year,plantId);
	// }

}
