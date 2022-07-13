package com.mmc.bpm.client.process.instance;

import com.mmc.bpm.engine.model.spi.ProcessInstance;

public interface ProcessInstanceService {

	public ProcessInstance create(final String processDefinitionKey);

	public ProcessInstance create(final String processDefinitionKey, String businessKey);
	
	public void delete(final String processInstanceId);

}
