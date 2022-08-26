package com.wks.caseengine.variables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.camunda.client.ProcessEngineClient;

@Component
public class VariableServiceImpl implements VariableService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Override
	public String findVariables(String processInstanceId) {
		return processEngineClient.findVariables(processInstanceId);
	}

}
