package com.mmc.bpm.engine.camunda.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.mmc.bpm.engine.camunda.http.request.CamundaHttpRequestFactory;
import com.mmc.bpm.engine.model.impl.DeploymentImpl;
import com.mmc.bpm.engine.model.impl.ProcessDefinitionImpl;
import com.mmc.bpm.engine.model.spi.Form;
import com.mmc.bpm.engine.model.spi.ProcessInstance;
import com.mmc.bpm.engine.model.spi.ProcessMessage;
import com.mmc.bpm.engine.model.spi.Task;
import com.mmc.bpm.rest.client.MmcHttpRequest;

/**
 * @author victor.franca
 *
 */
@Component
public class CamundaEngineClient implements ProcessEngineClient {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private CamundaHttpRequestFactory camundaHttpRequestFactory;

	@Override
	public DeploymentImpl[] findDeployments() {
		return restTemplate.getForEntity(camundaHttpRequestFactory.getDeploymentListRequest().getHttpRequestUrl(),
				DeploymentImpl[].class).getBody();
	}

	@Override
	public ProcessDefinitionImpl[] findProcessDefinitions() {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getProcessDefinitionListRequest().getHttpRequestUrl(),
						ProcessDefinitionImpl[].class)
				.getBody();
	}

	@Override
	public ProcessInstance[] findProcessInstances(final String businessKey) {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getProcessInstanceListRequest(businessKey).getHttpRequestUrl(),
						ProcessInstance[].class)
				.getBody();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey) {

		MmcHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey);

		return restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstance.class)
				.getBody();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey) {
		MmcHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey,
				businessKey);

		return restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstance.class)
				.getBody();
	}

	@Override
	public void deleteProcessInstance(String processInstanceId) {
		MmcHttpRequest request = camundaHttpRequestFactory.getProcessInstanceDeleteRequest(processInstanceId);

		restTemplate.delete(request.getHttpRequestUrl());
	}

	@Override
	public Task[] findTasks(final String processInstanceBusinessKey) {
		return restTemplate.getForEntity(
				camundaHttpRequestFactory.getTaskListRequest(processInstanceBusinessKey).getHttpRequestUrl(),
				Task[].class).getBody();
	}

	@Override
	public void claimTask(String taskId, String taskAssignee) {
		MmcHttpRequest request = camundaHttpRequestFactory.getTaskClaimRequest(taskId, taskAssignee);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public void unclaimTask(String taskId) {
		MmcHttpRequest request = camundaHttpRequestFactory.getTaskUnclaimRequest(taskId);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public void complete(String taskId, JsonObject variables) {
		MmcHttpRequest request = camundaHttpRequestFactory.getTaskCompleteRequest(taskId, variables);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public Form getTaskForm(final String taskId) {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getTaskFormGetRequest(taskId).getHttpRequestUrl(), Form.class)
				.getBody();
	}

	@Override
	public String findVariables(String processInstanceId) {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getVariablesListRequest(processInstanceId).getHttpRequestUrl(),
						String.class)
				.getBody();
	}

	@Override
	public void sendMessage(ProcessMessage processMesage) {
		MmcHttpRequest request = camundaHttpRequestFactory.getMessageSendRequest(processMesage);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

}
