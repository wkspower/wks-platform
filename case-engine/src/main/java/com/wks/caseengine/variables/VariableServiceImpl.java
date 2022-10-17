package com.wks.caseengine.variables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.bpm.engine.client.ProcessEngineClient;
import com.wks.caseengine.repository.BpmEngineRepository;

@Component
public class VariableServiceImpl implements VariableService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Autowired
	private BpmEngineRepository bpmEngineRepository;

	@Override
	public String findVariables(final String processInstanceId, final String bpmEngineId) throws Exception {
		return processEngineClient.findVariables(processInstanceId, bpmEngineRepository.get(bpmEngineId));
	}

}
