package com.wks.bpm.engine.camunda.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.camunda.http.request.CamundaHttpRequestFactory;
import com.wks.bpm.engine.model.impl.DeploymentImpl;
import com.wks.bpm.engine.model.impl.ProcessDefinitionImpl;
import com.wks.bpm.engine.model.spi.Form;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.rest.client.WksHttpRequest;

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

		WksHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey);

		return restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstance.class)
				.getBody();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey) {
		WksHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey,
				businessKey);

		return restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstance.class)
				.getBody();
	}

	@Override
	public void deleteProcessInstance(String processInstanceId) {
		WksHttpRequest request = camundaHttpRequestFactory.getProcessInstanceDeleteRequest(processInstanceId);

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
		WksHttpRequest request = camundaHttpRequestFactory.getTaskClaimRequest(taskId, taskAssignee);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public void unclaimTask(String taskId) {
		WksHttpRequest request = camundaHttpRequestFactory.getTaskUnclaimRequest(taskId);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public void complete(String taskId, JsonObject variables) {
		WksHttpRequest request = camundaHttpRequestFactory.getTaskCompleteRequest(taskId, variables);
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
		WksHttpRequest request = camundaHttpRequestFactory.getMessageSendRequest(processMesage);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

}
