package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.FinishingShutdownConfigDTO;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.FinishingShutdownService;


@RestController
@RequestMapping("task")
public class FinishingShutdownController {

	@Autowired
	private FinishingShutdownService finishingShutdownService;
	
	@GetMapping(value="/finishing-shutdown")
	public AOPMessageVM getFinishingShutdown(@RequestParam String plantId,@RequestParam String year){
		 return  finishingShutdownService.getFinishingShutdown(plantId,year);
	}
	
	@PostMapping(value="/finishing-shutdown")
	public AOPMessageVM saveFinishingShutdown(@RequestParam String year,@RequestParam String siteId, @RequestBody List<FinishingShutdownConfigDTO> finishingShutdownConfigDTOs) {
		return 	finishingShutdownService.saveFinishingShutdown(year,siteId,finishingShutdownConfigDTOs);
	}
	@DeleteMapping(value="/finishing-shutdown")
	public AOPMessageVM deleteFinishingShutdown(@RequestParam String id){
		 return  finishingShutdownService.deleteFinishingShutdown(id);
	}
}
