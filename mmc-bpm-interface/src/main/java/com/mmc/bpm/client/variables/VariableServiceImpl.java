package com.mmc.bpm.client.variables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.camunda.client.ProcessEngineClient;

@Component
public class VariableServiceImpl implements VariableService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Override
	public String findVariables(String processInstanceId) {
		return processEngineClient.findVariables(processInstanceId);
	}

}
