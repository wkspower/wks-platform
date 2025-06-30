package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.DecokingActivitiesService;

@RestController
@RequestMapping("task")
public class DecokingActivitiesController {
	
	@Autowired
	private DecokingActivitiesService decokingActivitiesService;
	
	@GetMapping(value="/decoking-activities")
	public AOPMessageVM getDecokingActivitiesData(@RequestParam String year,@RequestParam String plantId,@RequestParam String reportType) {
		return decokingActivitiesService.getDecokingActivitiesData(year,plantId,reportType);
	}
}
