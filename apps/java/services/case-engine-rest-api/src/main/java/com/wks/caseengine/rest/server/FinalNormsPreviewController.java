package com.wks.caseengine.rest.server;



import com.wks.caseengine.service.FinalNormsService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ModeWiseNormsDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("task")
public class FinalNormsPreviewController {
	
	@Autowired
	private FinalNormsService finalNormsService;
		
	@GetMapping(value="/final-norms")
	public AOPMessageVM getFinalNorms(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String mode,@RequestParam(required=false) String method){
		return	finalNormsService.getFinalNorms(year, plantId,mode,method);
	}
	
	@GetMapping(value="/calculate-final-norms")
	public AOPMessageVM calculateFinalNorms(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String mode,@RequestParam(required=false) String method){
		return	finalNormsService.calculateFinalNorms(year, plantId,mode,method);
	}
	
	@PostMapping(value="/final-norms")
	public AOPMessageVM updateModeWiseNormsData(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String mode,@RequestParam(required=false) String method,@RequestBody List<ModeWiseNormsDTO> modeWiseNormsDTOList){
		return	finalNormsService.updateFinalNorms(year, plantId,mode,method,modeWiseNormsDTOList);
	}
		
}
