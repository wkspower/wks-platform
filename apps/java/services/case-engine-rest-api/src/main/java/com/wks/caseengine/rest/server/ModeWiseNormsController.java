package com.wks.caseengine.rest.server;



import com.wks.caseengine.service.ModeWiseNormsService;

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
public class ModeWiseNormsController {
	
	@Autowired
	private ModeWiseNormsService modeWiseNormsService;
		
	@GetMapping(value="/mode-wise/norms")
	public AOPMessageVM getModeWiseNormsData(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String mode,@RequestParam(required=false) String method){
		return	modeWiseNormsService.getModeWiseNormsData(year, plantId,mode,method);
	}
	
	@PostMapping(value="/mode-wise/norms")
	public AOPMessageVM updateModeWiseNormsData(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String mode,@RequestParam(required=false) String method,@RequestBody List<ModeWiseNormsDTO> modeWiseNormsDTOList){
		return	modeWiseNormsService.updateModeWiseNormsData(year, plantId,mode,method,modeWiseNormsDTOList);
	}

	

	@GetMapping(value="/mode-wise/norms-monthwise-modetype")
	public AOPMessageVM getNormsMonthWiseModeTypeData(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String mode){
		return	modeWiseNormsService.getNormsMonthWiseModeTypeData(year, plantId,mode);
	}
		
}
