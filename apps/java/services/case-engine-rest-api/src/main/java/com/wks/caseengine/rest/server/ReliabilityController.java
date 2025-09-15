package com.wks.caseengine.rest.server;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ReliabilityService;
import com.wks.caseengine.dto.ReliabilityPerformanceDto;
import com.wks.caseengine.dto.ReliabilityRecordDto;

@RestController
@RequestMapping("task")
public class ReliabilityController {
	
	@Autowired
	private ReliabilityService reliabilityService;
	
	@GetMapping(value="/reliability-performance")
	public AOPMessageVM getReliabilityPerformance(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String type){
		 return  reliabilityService.getReliabilityPerformance(plantId,year,type);
	}
	
	@PostMapping(value="/reliability-performance")
	public AOPMessageVM updateReliabilityPerformance(@RequestBody List<ReliabilityPerformanceDto> reliabilityPerformanceDtos){
		 return  reliabilityService.updateReliabilityPerformance(reliabilityPerformanceDtos);
	}
	
	@GetMapping(value="/reliability-records")
	public AOPMessageVM getReliabilityRecords(@RequestParam String plantId,@RequestParam String year,@RequestParam(required=false) String type){
		 return  reliabilityService.getReliabilityRecords(plantId,year,type);
	}
	
	@PostMapping(value="/reliability-records")
	public AOPMessageVM updateReliabilityRecords(@RequestBody List<ReliabilityRecordDto> reliabilityRecordDtos){
		 return  reliabilityService.updateReliabilityRecords(reliabilityRecordDtos);
	}
	
	
  
}
