package com.mmc.bpm.engine.camunda.data.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mmc.bpm.engine.camunda.http.request.CamundaHttpRequestFactory;
import com.mmc.bpm.engine.camunda.model.CamundaDeployment;
import com.mmc.bpm.engine.camunda.model.CamundaProcessDefinition;
import com.mmc.bpm.engine.camunda.model.CamundaProcessInstance;
import com.mmc.bpm.engine.camunda.model.CamundaTask;
import com.mmc.bpm.engine.camunda.model.Deployment;
import com.mmc.bpm.engine.camunda.model.ProcessDefinition;
import com.mmc.bpm.engine.camunda.model.ProcessInstance;
import com.mmc.bpm.engine.camunda.model.Task;
import com.mmc.bpm.rest.client.MmcHttpRequest;

/**
 * @author victor.franca
 *
 */
@Component
public class CamundaEngineDataProvider implements ProcessEngineDataProvider {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private CamundaHttpRequestFactory camundaHttpRequestFactory;

	@Override
	public Deployment[] findDeployments() {
		return restTemplate.getForEntity(camundaHttpRequestFactory.getDeploymentListRequest().getHttpRequestUrl(),
				CamundaDeployment[].class).getBody();
	}

	@Override
	public ProcessDefinition[] findProcessDefinitions() {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getProcessDefinitionListRequest().getHttpRequestUrl(),
						CamundaProcessDefinition[].class)
				.getBody();
	}

	@Override
	public ProcessInstance[] findProcessInstances() {
		return restTemplate.getForEntity(camundaHttpRequestFactory.getProcessInstanceListRequest().getHttpRequestUrl(),
				CamundaProcessInstance[].class).getBody();
	}

	@Override
	public Task[] findTasks() {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getTaskListRequest().getHttpRequestUrl(), CamundaTask[].class)
				.getBody();
	}

	@Override
	public ProcessInstance startProcess(String processDefinitionKey) {

		MmcHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey);

		return restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), CamundaProcessInstance.class)
				.getBody();
	}

}
