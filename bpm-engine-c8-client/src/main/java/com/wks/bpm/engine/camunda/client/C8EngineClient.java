package com.wks.bpm.engine.camunda.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.client.BpmEngineClient;
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
@Qualifier("c8EngineClient")
public class C8EngineClient implements BpmEngineClient {

	@Autowired
	private C8ZeebeClient zeebeClient;

	@Autowired
	private C8OperateClient operateClient;

	@Autowired
	private C8TasklistClient tasklistClient;

	@Override
	public Deployment[] findDeployments(final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions(final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessInstance[] findProcessInstances(Optional<String> businessKey, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProcessDefinitionXML(String processDefinitionId, final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, final BpmEngine bpmEngine) {
		return zeebeClient.startProcess(processDefinitionKey, bpmEngine);
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, final BpmEngine bpmEngine) {
		return zeebeClient.startProcess(processDefinitionKey, businessKey, bpmEngine);
	}

	@Override
	public void deleteProcessInstance(String processInstanceId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();

	}

	@Override
	public ActivityInstance[] findActivityInstances(String processInstanceId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Task[] findTasks(String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return new Task[] {};
	}

	@Override
	public void claimTask(String taskId, String taskAssignee, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void unclaimTask(String taskId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void complete(String taskId, JsonObject variables, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String findVariables(String processInstanceId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendMessage(ProcessMessage processMesage, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, JsonArray caseAttributes,
			BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

}
