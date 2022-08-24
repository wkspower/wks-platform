package com.mmc.bpm.engine.camunda.http.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mmc.bpm.engine.model.spi.Deployment;
import com.mmc.bpm.engine.model.spi.ProcessDefinition;
import com.mmc.bpm.engine.model.spi.ProcessInstance;
import com.mmc.bpm.engine.model.spi.ProcessMessage;
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

	@Value("${camunda7.rest.correlate.url}")
	private String correlateUrl;

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

	public MmcHttpRequest getProcessInstanceListRequest(final String businessKey) {
		StringBuilder url = new StringBuilder().append(processInstanceUrl);
		if (businessKey != null) {
			url.append("?businessKey=" + businessKey);
		}
		return new CamundaHttpGetRequest<ProcessInstance>(url.toString(),
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	public MmcHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey) {
		return new CamundaHttpPostRequest(processDefinitionUrl + "/key/" + processDefinitionKey + "/start",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	public MmcHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey, final String businessKey) {
		// TODO refactor it - Payload Creator
		ProcessInstance processInstance = ProcessInstance.builder().businessKey(businessKey).caseInstanceId(businessKey)
				.build();

		return new CamundaHttpPostRequest(processDefinitionUrl + "/key/" + processDefinitionKey + "/start",
				new HttpEntity<>(processInstance, httpHeadersFactory.create()));
	}

	public MmcHttpRequest getProcessInstanceDeleteRequest(String processInstanceId) {
		return new CamundaHttpDeleteRequest(processInstanceUrl + "/" + processInstanceId,
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	//// Task ////

	public MmcHttpRequest getTaskListRequest(String processInstanceBusinessKey) {
		StringBuilder url = new StringBuilder().append(taskUrl + "?active=true");
		if (processInstanceBusinessKey != null) {
			url.append("&processInstanceBusinessKey=" + processInstanceBusinessKey);
		}

		return new CamundaHttpGetRequest<Task>(url.toString(), new HttpEntity<>(httpHeadersFactory.create()));
	}

	public MmcHttpRequest getTaskClaimRequest(final String taskId, final String taskAssignee) {

		String assigneeJson = "{ \"userId\": \"" + taskAssignee + "\" }";
		JsonObject assigneeJsonObject = JsonParser.parseString(assigneeJson).getAsJsonObject();

		return new CamundaHttpPostRequest(taskUrl + "/" + taskId + "/claim",
				new HttpEntity<String>(assigneeJsonObject.toString(), httpHeadersFactory.create()));
	}

	public MmcHttpRequest getTaskCompleteRequest(final String taskId, JsonObject variables) {
		return new CamundaHttpPostRequest(taskUrl + "/" + taskId + "/complete",
				new HttpEntity<String>(variables.toString(), httpHeadersFactory.create()));
	}

	public MmcHttpRequest getTaskUnclaimRequest(final String taskId) {
		return new CamundaHttpPostRequest(taskUrl + "/" + taskId + "/unclaim",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	public MmcHttpRequest getTaskFormGetRequest(final String taskId) {
		return new CamundaHttpGetRequest<>(taskUrl + "/" + taskId + "/deployed-form",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	/// Variables ///
	public MmcHttpRequest getVariablesListRequest(final String processIntanceId) {
		return new CamundaHttpGetRequest<>(processInstanceUrl + "/" + processIntanceId + "/variables",
				new HttpEntity<String>(httpHeadersFactory.create()));
	}

	/// Message ////
	public MmcHttpRequest getMessageSendRequest(final ProcessMessage processMessage) {
		return new CamundaHttpPostRequest(correlateUrl, new HttpEntity<>(processMessage, httpHeadersFactory.create()));
	}

}
