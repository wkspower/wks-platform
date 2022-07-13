package com.mmc.bpm.engine.camunda.data.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mmc.bpm.engine.camunda.http.request.CamundaHttpRequestFactory;
import com.mmc.bpm.engine.model.impl.ProcessDefinitionImpl;
import com.mmc.bpm.engine.model.impl.ProcessInstanceImpl;
import com.mmc.bpm.engine.model.impl.TaskImpl;
import com.mmc.bpm.engine.model.spi.Deployment;
import com.mmc.bpm.engine.model.spi.ProcessDefinition;
import com.mmc.bpm.engine.model.spi.ProcessInstance;
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
	public Deployment[] findDeployments() {
		return restTemplate.getForEntity(camundaHttpRequestFactory.getDeploymentListRequest().getHttpRequestUrl(),
				Deployment[].class).getBody();
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions() {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getProcessDefinitionListRequest().getHttpRequestUrl(),
						ProcessDefinitionImpl[].class)
				.getBody();
	}

	@Override
	public ProcessInstance[] findProcessInstances() {
		return restTemplate.getForEntity(camundaHttpRequestFactory.getProcessInstanceListRequest().getHttpRequestUrl(),
				ProcessInstanceImpl[].class).getBody();
	}

	@Override
	public Task[] findTasks() {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getTaskListRequest().getHttpRequestUrl(), TaskImpl[].class)
				.getBody();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey) {

		MmcHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey);

		return restTemplate
				.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstanceImpl.class)
				.getBody();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey) {
		MmcHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey,
				businessKey);

		return restTemplate
				.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstanceImpl.class)
				.getBody();
	}

	@Override
	public void deleteProcessInstance(String processInstanceId) {
		MmcHttpRequest request = camundaHttpRequestFactory.getProcessInstanceDeleteRequest(processInstanceId);

		restTemplate.delete(request.getHttpRequestUrl());
	}

}
