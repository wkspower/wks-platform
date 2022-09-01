package com.wks.bpm.engine.camunda.client;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.TaskForm;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;

/**
 * @author victor.franca
 *
 */
public interface ProcessEngineClient {

	public Deployment[] findDeployments();

	
	public ProcessDefinition[] findProcessDefinitions();


	public ProcessInstance[] findProcessInstances(final String businessKey);

	public ProcessInstance startProcess(final String processDefinitionKey);

	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey);

	public void deleteProcessInstance(final String processInstanceId);

	
	public Task[] findTasks(final String processInstanceBusinessKey);

	public void claimTask(String taskId, String taskAssignee);

	public void unclaimTask(String taskId);

	public void complete(String taskId, JsonObject variables);

	public TaskForm getTaskForm(final String taskId);

	
	public String findVariables(final String processInstanceId);

	
	public void sendMessage(final ProcessMessage processMesage);

}
