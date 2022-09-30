package com.wks.bpm.engine.camunda.client;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.bpm.engine.model.spi.TaskForm;

/**
 * @author victor.franca
 *
 */
public interface ProcessEngineClient {

	Deployment[] findDeployments();

	ProcessDefinition[] findProcessDefinitions();

	ProcessInstance[] findProcessInstances(final Optional<String> businessKey);
	
	String getProcessDefinitionXML(final String processInstanceId);

	ProcessInstance startProcess(final String processDefinitionKey);

	ProcessInstance startProcess(final String processDefinitionKey, final String businessKey);

	void deleteProcessInstance(final String processInstanceId);

	ActivityInstance[] findActivityInstances(final String processInstanceId);

	Task[] findTasks(final String processInstanceBusinessKey);

	void claimTask(String taskId, String taskAssignee);

	void unclaimTask(String taskId);

	void complete(String taskId, JsonObject variables);

	TaskForm getTaskForm(final String taskId);

	String findVariables(final String processInstanceId);

	void sendMessage(final ProcessMessage processMesage);

}
