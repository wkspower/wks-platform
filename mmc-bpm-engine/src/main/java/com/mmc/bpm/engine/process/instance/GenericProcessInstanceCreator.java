package com.mmc.bpm.engine.process.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.camunda.data.provider.ProcessEngineDataProvider;
import com.mmc.bpm.engine.cases.businesskey.BusinessKey;

@Component
public class GenericProcessInstanceCreator implements ProcessInstanceCreator {

	@Autowired
	private ProcessEngineDataProvider processEngineDataProvider;

	@Override
	public ProcessInstance create(final String processDefinitionKey) {
		processEngineDataProvider.startProcess(processDefinitionKey);
		return ProcessInstance.builder().build();
	}

	@Override
	public ProcessInstance create(final String processDefinitionKey, final BusinessKey businessKey) {
		return ProcessInstance.builder().businessKey(businessKey).build();
	}

}
