package com.mmc.automation.platform.casemanagement.process.instance;

import com.mmc.automation.platform.casemanagement.cases.businesskey.BusinessKey;

public interface ProcessInstanceCreator {

	public ProcessInstance create(final String processDefinitionKey);

	public ProcessInstance create(final String processDefinitionKey, BusinessKey businessKey);

}
