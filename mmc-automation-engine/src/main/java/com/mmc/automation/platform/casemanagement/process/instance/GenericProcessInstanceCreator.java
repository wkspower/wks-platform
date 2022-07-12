package com.mmc.automation.platform.casemanagement.process.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.automation.platform.casemanagement.cases.businesskey.BusinessKey;
import com.mmc.automation.process.engine.camunda.data.provider.ProcessEngineDataProvider;

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
