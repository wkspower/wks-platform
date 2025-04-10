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
import com.wks.caseengine.service.ShutdownNormsService;

@RestController
@RequestMapping("task")
public class ShutdownNormsController {
	
	@Autowired
	private ShutdownNormsService shutdownNormsService;
	
	@GetMapping(value="/shutdown-norms")
	public List<ShutdownNormsValueDTO> getShutdownNormsData(@RequestParam String year,@RequestParam String plantId){
		return	shutdownNormsService.getShutdownNormsData(year, plantId);
	}
	
	@PostMapping(value="/shutdown-norms")
	public List<ShutdownNormsValueDTO> saveShutdownNormsData(@RequestBody List<ShutdownNormsValueDTO> shutdownNormsValueDTOList){
		return	shutdownNormsService.saveShutdownNormsData(shutdownNormsValueDTOList);
	}
	
	@GetMapping(value="/shutdown-norms/sp-data")
	public List<ShutdownNormsValueDTO> getShutdownNormsSPData(@RequestParam String year,@RequestParam String plantId){
		return	shutdownNormsService.getShutdownNormsSPData(year, plantId);
	}

}
