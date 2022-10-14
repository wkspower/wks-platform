package com.wks.bpm.engine.camunda.client;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.client.ProcessEngineClient;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.bpm.engine.model.spi.TaskForm;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.spring.client.lifecycle.ZeebeClientLifecycle;

/**
 * @author victor.franca
 *
 */
@Component
public class C8EngineClient implements ProcessEngineClient {

	@Autowired
	private ZeebeClientLifecycle client;

	@Override
	public Deployment[] findDeployments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessInstance[] findProcessInstances(Optional<String> businessKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProcessDefinitionXML(String processInstanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey) {
		final ProcessInstanceEvent event = client.newCreateInstanceCommand().bpmnProcessId(processDefinitionKey)
				.latestVersion()
				.variables("{\"a\": \"" + UUID.randomUUID().toString() + "\",\"b\": \"" + new Date().toString() + "\"}")
				.send().join();
		return null;
	}

	@Override
	public void deleteProcessInstance(String processInstanceId) {
		// TODO Auto-generated method stub

	}

	@Override
	public ActivityInstance[] findActivityInstances(String processInstanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task[] findTasks(String processInstanceBusinessKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void claimTask(String taskId, String taskAssignee) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unclaimTask(String taskId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void complete(String taskId, JsonObject variables) {
		// TODO Auto-generated method stub

	}

	@Override
	public TaskForm getTaskForm(String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findVariables(String processInstanceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(ProcessMessage processMesage) {
		// TODO Auto-generated method stub

	}

}
