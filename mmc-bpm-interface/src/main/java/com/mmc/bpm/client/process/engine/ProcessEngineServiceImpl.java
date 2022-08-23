package com.mmc.bpm.client.process.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.camunda.client.ProcessEngineClient;

@Component
public class ProcessEngineServiceImpl implements ProcessEngineService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Override
	public String healthCheck() {
		processEngineClient.findDeployments();
		return "success";
	}

}
