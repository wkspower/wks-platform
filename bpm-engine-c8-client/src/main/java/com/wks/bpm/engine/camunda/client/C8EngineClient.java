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

//	@Autowired
//	private C8OperateClient operateClient;
//
//	@Autowired
//	private C8TasklistClient tasklistClient;

	@Override
	public void deploy(final BpmEngine bpmEngine, final String fileName, final String bpmnXml) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

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
	public ProcessInstance[] findProcessInstances(final Optional<String> businessKey, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getProcessDefinitionXMLById(final String processDefinitionId, final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProcessDefinitionXMLByKey(final String processDefinitionKey, final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final BpmEngine bpmEngine) {
		return zeebeClient.startProcess(processDefinitionKey, bpmEngine);
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey,
			final BpmEngine bpmEngine) {
		return zeebeClient.startProcess(processDefinitionKey, businessKey, bpmEngine);
	}
	
	@Override
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, JsonArray caseAttributes,
			BpmEngine bpmEngine, String tenantId) {
		return startProcess(processDefinitionKey, businessKey, caseAttributes, bpmEngine);
	}

	@Override
	public void deleteProcessInstance(final String processInstanceId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ActivityInstance[] findActivityInstances(final String processInstanceId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Task[] findTasks(final String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return new Task[] {};
	}

	@Override
	public void claimTask(final String taskId, final String taskAssignee, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void unclaimTask(final String taskId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void complete(final String taskId, final JsonObject variables, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String findVariables(final String processInstanceId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendMessage(final ProcessMessage processMesage, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey,
			final JsonArray caseAttributes, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		return null;
	}

}
