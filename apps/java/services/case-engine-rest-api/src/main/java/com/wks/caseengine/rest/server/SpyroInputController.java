package com.wks.caseengine.rest.server;

import java.util.List;
import com.wks.caseengine.service.SpyroInputService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.wks.caseengine.dto.SpyroInputDTO;

import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("task")
public class SpyroInputController {
	
	@Autowired
	private SpyroInputService spyroInputService;
	
	@GetMapping(value="/spyro-input")
	public AOPMessageVM getSpyroInputData(@RequestParam String year,@RequestParam String plantId,@RequestParam String Mode){
		return	spyroInputService.getSpyroInputData(year, plantId,Mode);
	}

	@PostMapping(value="/spyro-input")
	public AOPMessageVM updateSpyroInputData(@RequestBody List<SpyroInputDTO> spyroInputDTOList){
		return spyroInputService.updateSpyroInputData(spyroInputDTOList);
		
	}
}
