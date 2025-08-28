package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ShutdownNormsService;

@RestController
@RequestMapping("task")
public class ShutdownNormsController {
	
	@Autowired
	private ShutdownNormsService shutdownNormsService;
	
	@GetMapping(value="/shutdownNorms")
	public AOPMessageVM getShutdownNormsData(@RequestParam String year,@RequestParam String plantId,@RequestParam(required=false) String gradeId){
		return	shutdownNormsService.getShutdownNormsData(year,plantId,gradeId);
	}
	
	@PostMapping(value="/shutdownNorms")
	public AOPMessageVM saveShutdownNormsData(@RequestParam String plantId,@RequestBody List<ShutdownNormsValueDTO> shutdownNormsValueDTOList){
		return	shutdownNormsService.saveShutDownNorms(plantId,shutdownNormsValueDTOList);
	}
	
	@GetMapping(value="/getShutdownNormsSPData")
	public AOPMessageVM getShutdownNormsSPData(@RequestParam String year,@RequestParam String plantId){
		return	shutdownNormsService.getShutdownNormsSPData(year,plantId);
	}
	
	@GetMapping(value="/unique/grades")
	public AOPMessageVM getUniqueGrades(@RequestParam String year,@RequestParam String plantId){
		return	shutdownNormsService.getUniqueGrades(year,plantId);
	}

}
