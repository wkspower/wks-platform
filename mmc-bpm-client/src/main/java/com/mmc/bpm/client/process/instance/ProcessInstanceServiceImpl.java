package com.mmc.bpm.client.process.instance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.camunda.data.provider.ProcessEngineClient;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

@Component
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

	@Autowired
	private ProcessEngineClient processEngineClient;

	@Override
	public ProcessInstance create(final String processDefinitionKey) {
		return processEngineClient.startProcess(processDefinitionKey);
	}

	@Override
	public ProcessInstance create(final String processDefinitionKey, final String businessKey) {
		return processEngineClient.startProcess(processDefinitionKey, businessKey);
	}

	@Override
	public void delete(final String processInstanceId) {
		processEngineClient.deleteProcessInstance(processInstanceId);
	}

	@Override
	public void delete(final List<ProcessInstance> processInstances) {
		processInstances.forEach(o -> delete(o.getId()));

	}

}