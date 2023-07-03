/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.bpm.engine.camunda.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.camunda.http.request.C7HttpRequestFactory;
import com.wks.bpm.engine.client.BpmEngineClient;
import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.model.impl.DeploymentImpl;
import com.wks.bpm.engine.model.impl.ProcessDefinitionImpl;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.rest.client.WksHttpRequest;

/**
 * @author victor.franca
 *
 */
@Component
@Qualifier("c7EngineClient")
public class C7EngineClient implements BpmEngineClient {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private VariablesMapper variablesMapper;

	@Autowired
	private C7HttpRequestFactory camundaHttpRequestFactory;

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Override
	public void deploy(final BpmEngine bpmEngine, final String fileName, final String bpmnXml) {
		WksHttpRequest request = camundaHttpRequestFactory.getDeploymentCreateRequest(bpmEngine, fileName, bpmnXml);

		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class).getBody();
	}

	@Override
	public DeploymentImpl[] findDeployments(final BpmEngine bpmEngine) {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getDeploymentListRequest(bpmEngine).getHttpRequestUrl(),
						DeploymentImpl[].class)
				.getBody();
	}

	@Override
	public ProcessDefinitionImpl[] findProcessDefinitions(final BpmEngine bpmEngine) {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getProcessDefinitionListRequest(bpmEngine).getHttpRequestUrl(),
						ProcessDefinitionImpl[].class)
				.getBody();
	}

	@Override
	public String getProcessDefinitionXMLById(final String processDefinitionId, final BpmEngine bpmEngine)
			throws Exception {
		ProcessDefinitionImpl processDefinition = restTemplate.getForEntity(camundaHttpRequestFactory
				.getProcessDefinitionXmlByIdRequest(processDefinitionId, bpmEngine).getHttpRequestUrl(),
				ProcessDefinitionImpl.class).getBody();

		if (processDefinition == null) {
			throw new Exception("Process Definition not found");
		}

		return processDefinition.getBpmn20Xml();
	}

	@Override
	public String getProcessDefinitionXMLByKey(final String processDefinitionKey, final BpmEngine bpmEngine)
			throws Exception {
		ProcessDefinitionImpl processDefinition = restTemplate.getForEntity(camundaHttpRequestFactory
				.getProcessDefinitionXmlByKeyRequest(processDefinitionKey, bpmEngine).getHttpRequestUrl(),
				ProcessDefinitionImpl.class).getBody();

		if (processDefinition == null) {
			throw new Exception("Process Definition not found");
		}

		return processDefinition.getBpmn20Xml();
	}

	@Override
	public ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final Optional<String> activityIdIn, final BpmEngine bpmEngine) {

		return restTemplate.getForEntity(camundaHttpRequestFactory
				.getProcessInstanceListRequest(processDefinitionKey, activityIdIn, businessKey, bpmEngine)
				.getHttpRequestUrl(), ProcessInstance[].class).getBody();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final BpmEngine bpmEngine) {

		WksHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey,
				bpmEngine, tenantHolder.getTenantId().get());

		return restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstance.class)
				.getBody();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey,
			final BpmEngine bpmEngine) {
		WksHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey,
				businessKey, bpmEngine, tenantHolder.getTenantId().get());

		return restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstance.class)
				.getBody();
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final String businessKey,
			final JsonArray caseAttributes, final BpmEngine bpmEngine) {

		JsonObject processVariables = variablesMapper.map(caseAttributes);

		WksHttpRequest request = camundaHttpRequestFactory.getProcessInstanceCreateRequest(processDefinitionKey,
				businessKey, processVariables, bpmEngine, tenantHolder.getTenantId().get());

		RestTemplate restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new GsonHttpMessageConverter());
		restTemplate.setMessageConverters(messageConverters);

		return restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), ProcessInstance.class)
				.getBody();
	}

	@Override
	public void deleteProcessInstance(final String processInstanceId, final BpmEngine bpmEngine) {
		WksHttpRequest request = camundaHttpRequestFactory.getProcessInstanceDeleteRequest(processInstanceId,
				bpmEngine);

		restTemplate.delete(request.getHttpRequestUrl());
	}

	@Override
	public ActivityInstance[] findActivityInstances(final String processInstanceId, final BpmEngine bpmEngine)
			throws Exception {
		ActivityInstance activityInstance = restTemplate
				.getForEntity(camundaHttpRequestFactory.getActivityInstancesGetRequest(processInstanceId, bpmEngine)
						.getHttpRequestUrl(), ActivityInstance.class)
				.getBody();

		if (activityInstance == null) {
			throw new Exception("Process Instance not found");
		}

		return activityInstance.getChildActivityInstances();
	}

	@Override
	public void createTask(Task task, BpmEngine bpmEngine) {

		UUID taskId = UUID.nameUUIDFromBytes((task.getDescription() + new Date()).getBytes());
		task.setId(taskId.toString());
		task.setTenantId(tenantHolder.getTenantId().get());

		WksHttpRequest request = camundaHttpRequestFactory.getTaskCreateRequest(task, bpmEngine);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public Task getTask(final String taskId, final BpmEngine bpmEngine) {
		return restTemplate
				.getForEntity(camundaHttpRequestFactory.getTaskGetRequest(taskId, bpmEngine).getHttpRequestUrl(),
						Task.class)
				.getBody();
	}

	@Override
	public Task[] findTasks(final String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		return restTemplate.getForEntity(
				camundaHttpRequestFactory.getTaskListRequest(processInstanceBusinessKey, bpmEngine).getHttpRequestUrl(),
				Task[].class).getBody();
	}

	@Override
	public void claimTask(final String taskId, final String taskAssignee, final BpmEngine bpmEngine) {
		WksHttpRequest request = camundaHttpRequestFactory.getTaskClaimRequest(taskId, taskAssignee, bpmEngine);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public void unclaimTask(final String taskId, final BpmEngine bpmEngine) {
		WksHttpRequest request = camundaHttpRequestFactory.getTaskUnclaimRequest(taskId, bpmEngine);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public void complete(final String taskId, final JsonObject variables, final BpmEngine bpmEngine) {
		WksHttpRequest request = camundaHttpRequestFactory.getTaskCompleteRequest(taskId, variables, bpmEngine);
		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

	@Override
	public String findVariables(final String processInstanceId, final BpmEngine bpmEngine) {
		return restTemplate.getForEntity(
				camundaHttpRequestFactory.getVariablesListRequest(processInstanceId, bpmEngine).getHttpRequestUrl(),
				String.class).getBody();
	}

	@Override
	public void sendMessage(final ProcessMessage processMessage, final Optional<JsonArray> variables,
			final BpmEngine bpmEngine) {
		processMessage.setTenantId(tenantHolder.getTenantId().get());

		Optional<JsonObject> variablesCamundaJson = variables.isPresent()
				? Optional.of(variablesMapper.map(variables.get()))
				: Optional.empty();

		WksHttpRequest request = camundaHttpRequestFactory.getMessageSendRequest(processMessage, variablesCamundaJson,
				bpmEngine);

		RestTemplate restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new GsonHttpMessageConverter());
		restTemplate.setMessageConverters(messageConverters);

		restTemplate.postForEntity(request.getHttpRequestUrl(), request.getHttpEntity(), String.class);
	}

}
