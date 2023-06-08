package com.wks.bpm.engine.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.bpm.engine.BpmEngine;
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
@Component
public class DefaultBpmEngineClientFacade implements BpmEngineClientFacade {

	@Autowired
	private BpmEngineClient c7EngineClient;
	
	private BpmEngine bpmEngine = new DefaultC7BpmEngine();

	private BpmEngine getBpmEngine() {
		return bpmEngine;
	}

	private BpmEngineClient getEngineClient() {
		return c7EngineClient;
	}

	public void deploy(final String fileName, final String bpmnXml) {
		getEngineClient().deploy(getBpmEngine(), fileName, bpmnXml);
	}

	public Deployment[] findDeployments() {
		return getEngineClient().findDeployments(getBpmEngine());
	}

	public ProcessDefinition[] findProcessDefinitions() {
		return getEngineClient().findProcessDefinitions(getBpmEngine());
	}

	public String getProcessDefinitionXMLById(final String processDefinitionId) throws Exception {
		return getEngineClient().getProcessDefinitionXMLById(processDefinitionId, getBpmEngine());
	}

	public String getProcessDefinitionXMLByKey(final String processDefinitionKey) throws Exception {
		return getEngineClient().getProcessDefinitionXMLByKey(processDefinitionKey, getBpmEngine());
	}

	public ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final Optional<String> activityIdIn) {
		return getEngineClient().findProcessInstances(processDefinitionKey, businessKey, activityIdIn, getBpmEngine());
	}

	public ProcessInstance startProcess(final String processDefinitionKey) {
		return getEngineClient().startProcess(processDefinitionKey, getBpmEngine());
	}

	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey) {
		return getEngineClient().startProcess(processDefinitionKey, businessKey, getBpmEngine());
	}

	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey,
			final JsonArray caseAttributes) {
		return getEngineClient().startProcess(processDefinitionKey, businessKey, caseAttributes, getBpmEngine());
	}

	public void deleteProcessInstance(String processInstanceId) {
		getEngineClient().deleteProcessInstance(processInstanceId, getBpmEngine());
	}

	public ActivityInstance[] findActivityInstances(String processInstanceId) throws Exception {
		return getEngineClient().findActivityInstances(processInstanceId, getBpmEngine());
	}
	
	@Override
	public void createTask(Task task) {
		getEngineClient().createTask(task, getBpmEngine());
	}

	public Task getTask(final String taskId) {
		return getEngineClient().getTask(taskId, getBpmEngine());
	}
	
	public Task[] findTasks(final String processInstanceBusinessKey) {
		return getEngineClient().findTasks(processInstanceBusinessKey, getBpmEngine());
	}

	public void claimTask(String taskId, String taskAssignee) {
		getEngineClient().claimTask(taskId, taskAssignee, getBpmEngine());
	}

	public void unclaimTask(String taskId) {
		getEngineClient().unclaimTask(taskId, getBpmEngine());
	}

	public void complete(String taskId, JsonObject variables) {
		getEngineClient().complete(taskId, variables, getBpmEngine());
	}

	public String findVariables(String processInstanceId) {
		return getEngineClient().findVariables(processInstanceId, getBpmEngine());
	}

	public void sendMessage(ProcessMessage processMesage, Optional<JsonArray> variables) {
		getEngineClient().sendMessage(processMesage, variables, getBpmEngine());
	}

}
