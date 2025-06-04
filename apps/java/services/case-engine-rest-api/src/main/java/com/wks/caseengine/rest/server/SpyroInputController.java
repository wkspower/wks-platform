package com.wks.caseengine.rest.server;

import java.util.List;
import com.wks.caseengine.service.SpyroInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.SlowdownNormsValueDTO;
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

}
