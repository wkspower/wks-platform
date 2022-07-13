package com.mmc.bpm.client.process.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.camunda.data.provider.ProcessEngineDataProvider;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

@Component
public class GenericProcessInstanceCreator implements ProcessInstanceCreator {

	@Autowired
	private ProcessEngineDataProvider processEngineDataProvider;

	@Override
	public ProcessInstance create(final String processDefinitionKey) {
		return processEngineDataProvider.startProcess(processDefinitionKey);
	}

	@Override
	public ProcessInstance create(final String processDefinitionKey, final String businessKey) {
		return processEngineDataProvider.startProcess(processDefinitionKey, businessKey);
	}

}
