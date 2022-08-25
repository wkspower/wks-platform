package com.mmc.bpm.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mmc.bpm.client.process.engine.ProcessEngineService;

@RestController
public class HealthCheckController {

	@Autowired
	private ProcessEngineService processEngineService;

	@GetMapping(value = "/healthCheck")
	public String check() {
		return processEngineService.healthCheck();
	}

}
