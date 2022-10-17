package com.wks.bpm.engine.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.BpmEngineType;
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
@Component
public class BpmEngineClientFacade {

	@Autowired
	private BpmEngineClient c7EngineClient;

	@Autowired
	private BpmEngineClient c8EngineClient;

	private BpmEngineClient getEngineClient(final BpmEngine bpmEngine) {
		return bpmEngine.getType().equals(BpmEngineType.BPM_ENGINE_CAMUNDA7) ? c7EngineClient : c8EngineClient;
	}

	public Deployment[] findDeployments(final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).findDeployments(bpmEngine);
	}

	public ProcessDefinition[] findProcessDefinitions(final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).findProcessDefinitions(bpmEngine);
	}

	public String getProcessDefinitionXML(final String processDefinitionId, final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).getProcessDefinitionXML(processDefinitionId, bpmEngine);
	}

	public ProcessInstance[] findProcessInstances(final Optional<String> businessKey, final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).findProcessInstances(businessKey, bpmEngine);
	}

	public ProcessInstance startProcess(final String processDefinitionKey, final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).startProcess(processDefinitionKey, bpmEngine);
	}

	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey,
			final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).startProcess(processDefinitionKey, businessKey, bpmEngine);

	}

	public void deleteProcessInstance(String processInstanceId, final BpmEngine bpmEngine) {
		getEngineClient(bpmEngine).deleteProcessInstance(processInstanceId, bpmEngine);
	}

	public ActivityInstance[] findActivityInstances(String processInstanceId, final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).findActivityInstances(processInstanceId, bpmEngine);
	}

	public Task[] findTasks(final String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).findTasks(processInstanceBusinessKey, bpmEngine);
	}

	public void claimTask(String taskId, String taskAssignee, final BpmEngine bpmEngine) {
		getEngineClient(bpmEngine).claimTask(taskId, taskAssignee, bpmEngine);
	}

	public void unclaimTask(String taskId, final BpmEngine bpmEngine) {
		getEngineClient(bpmEngine).unclaimTask(taskId, bpmEngine);
	}

	public void complete(String taskId, JsonObject variables, final BpmEngine bpmEngine) {
		getEngineClient(bpmEngine).complete(taskId, variables, bpmEngine);
	}

	public TaskForm getTaskForm(final String taskId, final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).getTaskForm(taskId, bpmEngine);
	}

	public String findVariables(String processInstanceId, final BpmEngine bpmEngine) {
		return getEngineClient(bpmEngine).findVariables(processInstanceId, bpmEngine);
	}

	public void sendMessage(ProcessMessage processMesage, final BpmEngine bpmEngine) {
		getEngineClient(bpmEngine).sendMessage(processMesage, bpmEngine);
	}

}
