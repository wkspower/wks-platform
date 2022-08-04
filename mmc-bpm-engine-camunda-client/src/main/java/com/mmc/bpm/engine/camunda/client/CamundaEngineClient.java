package com.mmc.bpm.engine.camunda.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mmc.bpm.engine.camunda.http.request.CamundaHttpRequestFactory;
import com.mmc.bpm.engine.model.impl.DeploymentImpl;
import com.mmc.bpm.engine.model.impl.ProcessDefinitionImpl;
import com.mmc.bpm.engine.model.spi.CamundaForm;
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
	public ProcessInstance[] findProcessInstances() {
		return restTemplate.getForEntity(camundaHttpRequestFactory.getProcessInstanceListRequest().getHttpRequestUrl(),
				ProcessInstance[].class).getBody();
	}

	@Override
	public Task[] findTasks() {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getTaskListRequest().getHttpRequestUrl(), Task[].class)
				.getBody();
	}

	@Override
	public CamundaForm getTaskForm(final String taskId) {
		return restTemplate.getForEntity(camundaHttpRequestFactory.getTaskFormGetRequest(taskId).getHttpRequestUrl(),
				CamundaForm.class).getBody();
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
	public void sendMessage(ProcessMessage processMesage) {
		MmcHttpRequest request = camundaHttpRequestFactory.getMessageSendRequest(processMesage);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

}
