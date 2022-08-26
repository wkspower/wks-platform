package com.wks.caseengine.process.instance;

import java.util.List;

import com.wks.bpm.engine.model.spi.ProcessInstance;

public interface ProcessInstanceService {

	public ProcessInstance create(final String processDefinitionKey);

	public ProcessInstance create(final String processDefinitionKey, String businessKey);

	public void delete(final List<ProcessInstance> processInstances);

	public void delete(final String processInstanceId);

	public List<ProcessInstance> find(final String businessKey);

}
