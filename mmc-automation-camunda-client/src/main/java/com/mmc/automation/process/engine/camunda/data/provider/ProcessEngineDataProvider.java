package com.mmc.automation.process.engine.camunda.data.provider;

import com.mmc.automation.process.engine.camunda.model.Deployment;
import com.mmc.automation.process.engine.camunda.model.ProcessDefinition;
import com.mmc.automation.process.engine.camunda.model.ProcessInstance;
import com.mmc.automation.process.engine.camunda.model.Task;

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
