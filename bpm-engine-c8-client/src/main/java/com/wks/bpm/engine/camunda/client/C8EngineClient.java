package com.wks.bpm.engine.camunda.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.client.BpmEngineClient;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.bpm.engine.model.spi.TaskForm;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

/**
 * @author victor.franca
 *
 */
@Component
@Qualifier("c8EngineClient")
public class C8EngineClient implements BpmEngineClient {

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
	public String getProcessDefinitionXML(String processInstanceId, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, final BpmEngine bpmEngine) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	private static final String zeebeAPI = "1585e068-0f1b-46f4-8f28-56b65150bcc9.bru-2.zeebe.camunda.io";
	private static final String zeebeAPIPort = "443";
	private static final String clientId = "Y.J~QwS~ll5Yvx2dwR0m-fQTmqa5xeJi";
	private static final String clientSecret = "uZHspeQF4S8RU4BskpK2PGZbDkqmwdPA~1x-U_5wXhoVwe_ITc-oKq1GcnZprrtQ";

	@Override
	public ProcessInstance startProcess(String processDefinitionKey, String businessKey, final BpmEngine bpmEngine) {
		OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder().audience(zeebeAPI)
				.clientId(clientId).clientSecret(clientSecret).build();

		ProcessInstance processInstance = null;

		try (ZeebeClient client = ZeebeClient.newClientBuilder().gatewayAddress(zeebeAPI + ":" + zeebeAPIPort)
				.credentialsProvider(credentialsProvider).build()) {

			final ProcessInstanceEvent processInstanceEvent = client.newCreateInstanceCommand()
					.bpmnProcessId(processDefinitionKey).latestVersion().send().join();
			processInstance = ProcessInstance.builder()
					.businessKey(String.valueOf(processInstanceEvent.getProcessInstanceKey())).build();
		}
		return processInstance;
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
		throw new UnsupportedOperationException();
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
	public TaskForm getTaskForm(String taskId, final BpmEngine bpmEngine) {
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

}
