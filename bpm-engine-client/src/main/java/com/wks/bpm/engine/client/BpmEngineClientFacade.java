package com.wks.bpm.engine.client;

import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;

/**
 * @author victor.franca
 *
 */
public interface BpmEngineClientFacade {

	void deploy(final String fileName, final String bpmnXml);

	Deployment[] findDeployments();

	ProcessDefinition[] findProcessDefinitions();

	String getProcessDefinitionXMLById(final String processDefinitionId) throws Exception;

	String getProcessDefinitionXMLByKey(final String processDefinitionKey) throws Exception;

	ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey);

	ProcessInstance startProcess(final String processDefinitionKey);

	ProcessInstance startProcess(final String processDefinitionKey, final String businessKey);

	ProcessInstance startProcess(final String processDefinitionKey, final String businessKey,
			final JsonArray caseAttributes);

	void deleteProcessInstance(String processInstanceId);

	ActivityInstance[] findActivityInstances(String processInstanceId) throws Exception;
	
	void createTask(final Task task);
	
	Task getTask(final String taskId);

	Task[] findTasks(final String processInstanceBusinessKey);

	void claimTask(String taskId, String taskAssignee);

	void unclaimTask(String taskId);

	void complete(String taskId, JsonObject variables);

	String findVariables(String processInstanceId);

	void sendMessage(ProcessMessage processMesage, Optional<JsonArray> variables);

}
