package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/shutdown-norms")
public class ShutdownNormsController {
	
	@Autowired
	private ShutdownNormsService shutdownNormsService;
	
	@GetMapping
	public ResponseEntity<AOPMessageVM> getShutdownNormsData(@RequestParam String year,@RequestParam String plantId){
		AOPMessageVM response =	shutdownNormsService.getShutdownNormsData(year, plantId);
		return ResponseEntity.status(response.getCode()).body(response);
	}
		
	@PostMapping
	public ResponseEntity<AOPMessageVM> saveShutdownNormsData(@RequestBody List<ShutdownNormsValueDTO> shutdownNormsValueDTOList){
		AOPMessageVM response =	shutdownNormsService.saveShutdownNormsData(shutdownNormsValueDTOList);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/sp")
	public ResponseEntity<AOPMessageVM> getShutdownNormsSPData(@RequestParam String year,@RequestParam String plantId){
		AOPMessageVM response =		shutdownNormsService.getShutdownNormsSPData(year, plantId);
		return ResponseEntity.status(response.getCode()).body(response);
	}

}
