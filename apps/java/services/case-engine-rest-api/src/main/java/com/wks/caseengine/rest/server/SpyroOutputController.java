package com.wks.caseengine.rest.server;

import java.util.List;
import com.wks.caseengine.service.SpyroOutputService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.wks.caseengine.dto.SpyroOutputDTO;

import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("task")
public class SpyroOutputController {
	
	@Autowired
	private SpyroOutputService spyroOutputService;
	
	@GetMapping(value="/spyro-output")
	public AOPMessageVM getSpyroOutputData(@RequestParam String year,@RequestParam String plantId,@RequestParam String Mode,@RequestParam(value = "type", required = false) String type){
		return	spyroOutputService.getSpyroOutputData(year, plantId,Mode,type);
	}

	@PostMapping(value="/spyro-output")
	public AOPMessageVM updateSpyroOutputData(@RequestBody List<SpyroOutputDTO> spyroOutputDTOList){
		return spyroOutputService.updateSpyroOutputData(spyroOutputDTOList);
	}
}
