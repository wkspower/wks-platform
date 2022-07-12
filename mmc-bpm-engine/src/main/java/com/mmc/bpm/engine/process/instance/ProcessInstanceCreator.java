package com.mmc.bpm.engine.process.instance;

import com.mmc.bpm.engine.cases.businesskey.BusinessKey;

public interface ProcessInstanceCreator {

	public ProcessInstance create(final String processDefinitionKey);

	public ProcessInstance create(final String processDefinitionKey, BusinessKey businessKey);

}
