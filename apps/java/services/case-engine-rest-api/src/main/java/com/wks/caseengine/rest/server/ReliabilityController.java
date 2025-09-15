package com.wks.caseengine.rest.server;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ReliabilityService;

@RestController
@RequestMapping("task")
public class ReliabilityController {
	
	@Autowired
	private ReliabilityService reliabilityService;
	
	@GetMapping(value="/reliability-performance")
	public AOPMessageVM getReliabilityPerformance(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String type){
		 return  reliabilityService.getReliabilityPerformance(plantId,year,type);
	}
	
	
  
}
