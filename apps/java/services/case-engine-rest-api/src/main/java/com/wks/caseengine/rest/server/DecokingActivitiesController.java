package com.wks.caseengine.rest.server;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.DecokePlanningIBRDTO;
import com.wks.caseengine.dto.DecokeRunLengthDTO;
import com.wks.caseengine.dto.DecokingActivitiesDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.DecokingActivitiesService;

@RestController
@RequestMapping("task")
public class DecokingActivitiesController {
	
	@Autowired
	private DecokingActivitiesService decokingActivitiesService;
	
	@GetMapping(value="/decoking-activities")
	public AOPMessageVM getDecokingActivitiesData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType) {
		return decokingActivitiesService.getDecokingActivitiesData(year,plantId,reportType);
	}
	
	@PostMapping(value="/decoking-activities")
	public AOPMessageVM updateDecokingActivitiesData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType, @RequestBody List<DecokingActivitiesDTO> decokingActivitiesDTOList) {
		return decokingActivitiesService.updateDecokingActivitiesData(year,plantId,reportType,decokingActivitiesDTOList);
	}
	
	@PostMapping(value="/decoking-activities/ibr")
	public AOPMessageVM updateDecokingActivitiesIBRData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType, @RequestBody List<DecokePlanningIBRDTO> decokePlanningIBRDTOList) {
		return decokingActivitiesService.updateDecokingActivitiesIBRData(year,plantId,reportType,decokePlanningIBRDTOList);
	}
	@PostMapping(value="/decoking-activities/run-length")
	public AOPMessageVM updateDecokingActivitiesRunLengthData(@RequestParam(value = "year", required = false) String year,@RequestParam String plantId,@RequestParam(value = "reportType", required = false) String reportType, @RequestBody List<DecokeRunLengthDTO> decokeRunLengthDTOList) {
		return decokingActivitiesService.updateDecokingActivitiesRunLengthData(year,plantId,reportType,decokeRunLengthDTOList);
	}
}
