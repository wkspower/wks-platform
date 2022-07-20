package com.mmc.bpm.engine.camunda.http.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.model.spi.Deployment;
import com.mmc.bpm.engine.model.spi.ProcessDefinition;
import com.mmc.bpm.engine.model.spi.ProcessInstance;
import com.mmc.bpm.engine.model.spi.Task;
import com.mmc.bpm.rest.client.MmcHttpRequest;
import com.mmc.bpm.rest.client.header.JSONHttpHeadersFactory;

/**
 * @author victor.franca
 *
 */
@Component
public class CamundaHttpRequestFactory {

	@Autowired
	private JSONHttpHeadersFactory httpHeadersFactory;

	@Value("${camunda7.rest.deployment.url}")
	private String deploymentUrl;

	@Value("${camunda7.rest.processdefinition.url}")
	private String processDefinitionUrl;

	@Value("${camunda7.rest.processinstance.url}")
	private String processInstanceUrl;

	@Value("${camunda7.rest.task.url}")
	private String taskUrl;

	//// Deployment ////

	public MmcHttpRequest getDeploymentListRequest() {
		return new CamundaHttpGetRequest<Deployment>(deploymentUrl, new HttpEntity<>(httpHeadersFactory.create()));
	}

	//// Process Definition ////

	public MmcHttpRequest getProcessDefinitionListRequest() {
		return new CamundaHttpGetRequest<ProcessDefinition>(processDefinitionUrl,
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	//// Process Instance ////

	public MmcHttpRequest getProcessInstanceListRequest() {
		return new CamundaHttpGetRequest<ProcessInstance>(processInstanceUrl,
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	public MmcHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey) {
		return new CamundaHttpPostRequest(processDefinitionUrl + "/key/" + processDefinitionKey + "/start",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	public MmcHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey, final String businessKey) {
		// TODO refactor it - Payload Creator
		ProcessInstance processInstance = ProcessInstance.builder().businessKey(businessKey).build();

		return new CamundaHttpPostRequest(processDefinitionUrl + "/key/" + processDefinitionKey + "/start",
				new HttpEntity<>(processInstance, httpHeadersFactory.create()));
	}

	public MmcHttpRequest getProcessInstanceDeleteRequest(String processInstanceId) {
		return new CamundaHttpDeleteRequest(processInstanceUrl + "/" + processInstanceId,
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	//// Task ////

	public MmcHttpRequest getTaskListRequest() {
		return new CamundaHttpGetRequest<Task>(taskUrl, new HttpEntity<>(httpHeadersFactory.create()));
	}

}
