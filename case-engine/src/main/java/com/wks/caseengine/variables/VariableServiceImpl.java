package com.wks.caseengine.variables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.BpmEngineClientFacade;

@Component
public class VariableServiceImpl implements VariableService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Override
	public String findVariables(final String processInstanceId) throws Exception {
		return processEngineClient.findVariables(processInstanceId);
	}

}
