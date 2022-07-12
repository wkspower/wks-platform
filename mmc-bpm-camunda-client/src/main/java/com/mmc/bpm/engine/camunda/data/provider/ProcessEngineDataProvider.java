package com.mmc.bpm.engine.camunda.data.provider;

import com.mmc.bpm.engine.camunda.model.Deployment;
import com.mmc.bpm.engine.camunda.model.ProcessDefinition;
import com.mmc.bpm.engine.camunda.model.ProcessInstance;
import com.mmc.bpm.engine.camunda.model.Task;

/**
 * @author victor.franca
 *
 */
public interface ProcessEngineDataProvider {

	public Deployment[] findDeployments();

	public ProcessDefinition[] findProcessDefinitions();

	public ProcessInstance[] findProcessInstances();

	public Task[] findTasks();

	public ProcessInstance startProcess(String processDefinitionKey);

}
