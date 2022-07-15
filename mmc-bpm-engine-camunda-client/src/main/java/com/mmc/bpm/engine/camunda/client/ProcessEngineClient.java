package com.mmc.bpm.engine.camunda.client;

import com.mmc.bpm.engine.model.spi.Deployment;
import com.mmc.bpm.engine.model.spi.ProcessDefinition;
import com.mmc.bpm.engine.model.spi.ProcessInstance;
import com.mmc.bpm.engine.model.spi.Task;

/**
 * @author victor.franca
 *
 */
public interface ProcessEngineClient {

	public Deployment[] findDeployments();

	public ProcessDefinition[] findProcessDefinitions();

	public ProcessInstance[] findProcessInstances();

	public Task[] findTasks();

	public ProcessInstance startProcess(final String processDefinitionKey);

	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey);

	public void deleteProcessInstance(final String processInstanceId);

}
