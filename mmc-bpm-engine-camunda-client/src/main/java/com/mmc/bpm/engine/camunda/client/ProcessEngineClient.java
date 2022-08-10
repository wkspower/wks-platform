package com.mmc.bpm.engine.camunda.client;

import com.google.gson.JsonObject;
import com.mmc.bpm.engine.model.spi.CamundaForm;
import com.mmc.bpm.engine.model.spi.Deployment;
import com.mmc.bpm.engine.model.spi.ProcessDefinition;
import com.mmc.bpm.engine.model.spi.ProcessInstance;
import com.mmc.bpm.engine.model.spi.ProcessMessage;
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

	public void claimTask(String taskId, String taskAssignee);

	public void unclaimTask(String taskId);

	public void complete(String taskId, JsonObject variables);

	public CamundaForm getTaskForm(final String taskId);

	public ProcessInstance startProcess(final String processDefinitionKey);

	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey);

	public void deleteProcessInstance(final String processInstanceId);

	public String findVariables(final String processInstanceId);

	public void sendMessage(final ProcessMessage processMesage);

}
