package com.wks.bpm.engine.camunda.http.request;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.rest.client.WksHttpRequest;
import com.wks.rest.client.header.JSONHttpHeadersFactory;

/**
 * @author victor.franca
 *
 */
@Component
public class C7HttpRequestFactory {

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

	public WksHttpRequest getDeploymentListRequest(final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<Deployment>(extractUrl(bpmEngine) + deploymentUrl,
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	//// Process Definition ////

	public WksHttpRequest getProcessDefinitionListRequest(final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<ProcessDefinition>(extractUrl(bpmEngine) + processDefinitionUrl,
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	public WksHttpRequest getProcessDefinitionXmlRequest(final String processDefinitionId, final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<ProcessDefinition>(
				extractUrl(bpmEngine) + processDefinitionUrl + "/" + processDefinitionId + "/xml",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	//// Process Instance ////

	public WksHttpRequest getProcessInstanceListRequest(final Optional<String> businessKey, final BpmEngine bpmEngine) {
		StringBuilder url = new StringBuilder().append(extractUrl(bpmEngine) + processInstanceUrl);
		if (businessKey.isPresent()) {
			url.append("?businessKey=" + businessKey.get());
		}
		return new C7HttpGetRequest<ProcessInstance>(url.toString(), new HttpEntity<>(httpHeadersFactory.create()));
	}

	public WksHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey,
			final BpmEngine bpmEngine) {
		return new C7HttpPostRequest(
				extractUrl(bpmEngine) + processDefinitionUrl + "/key/" + processDefinitionKey + "/start",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	public WksHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey, final String businessKey,
			final BpmEngine bpmEngine) {
		// TODO refactor it - Payload Creator
		ProcessInstance processInstance = ProcessInstance.builder().businessKey(businessKey).caseInstanceId(businessKey)
				.build();

		return new C7HttpPostRequest(
				extractUrl(bpmEngine) + processDefinitionUrl + "/key/" + processDefinitionKey + "/start",
				new HttpEntity<>(processInstance, httpHeadersFactory.create()));
	}

	public WksHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey, final String businessKey,
			final JsonObject variables, final BpmEngine bpmEngine) {
		// TODO refactor it - Payload Creator
		ProcessInstance processInstance = ProcessInstance.builder().businessKey(businessKey).caseInstanceId(businessKey)
				.variables(variables).build();

		return new C7HttpPostRequest(
				extractUrl(bpmEngine) + processDefinitionUrl + "/key/" + processDefinitionKey + "/start",
				new HttpEntity<>(processInstance, httpHeadersFactory.create()));
	}

	public WksHttpRequest getProcessInstanceDeleteRequest(String processInstanceId, final BpmEngine bpmEngine) {
		return new C7HttpDeleteRequest(extractUrl(bpmEngine) + processInstanceUrl + "/" + processInstanceId,
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	//// Task ////

	public WksHttpRequest getTaskListRequest(String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		StringBuilder url = new StringBuilder().append(extractUrl(bpmEngine) + taskUrl + "?active=true");
		if (processInstanceBusinessKey != null) {
			url.append("&processInstanceBusinessKey=" + processInstanceBusinessKey);
		}

		return new C7HttpGetRequest<Task>(url.toString(), new HttpEntity<>(httpHeadersFactory.create()));
	}

	public WksHttpRequest getTaskClaimRequest(final String taskId, final String taskAssignee,
			final BpmEngine bpmEngine) {

		String assigneeJson = "{ \"userId\": \"" + taskAssignee + "\" }";
		JsonObject assigneeJsonObject = JsonParser.parseString(assigneeJson).getAsJsonObject();

		return new C7HttpPostRequest(extractUrl(bpmEngine) + taskUrl + "/" + taskId + "/claim",
				new HttpEntity<String>(assigneeJsonObject.toString(), httpHeadersFactory.create()));
	}

	public WksHttpRequest getTaskCompleteRequest(final String taskId, JsonObject variables, final BpmEngine bpmEngine) {
		return new C7HttpPostRequest(extractUrl(bpmEngine) + taskUrl + "/" + taskId + "/complete",
				new HttpEntity<String>(variables.toString(), httpHeadersFactory.create()));
	}

	public WksHttpRequest getTaskUnclaimRequest(final String taskId, final BpmEngine bpmEngine) {
		return new C7HttpPostRequest(extractUrl(bpmEngine) + taskUrl + "/" + taskId + "/unclaim",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	public WksHttpRequest getTaskFormGetRequest(final String taskId, final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<>(extractUrl(bpmEngine) + taskUrl + "/" + taskId + "/deployed-form",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	// Activity Instances
	public WksHttpRequest getActivityInstancesGetRequest(final String processInstanceId, final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<>(
				extractUrl(bpmEngine) + processInstanceUrl + "/" + processInstanceId + "/activity-instances",
				new HttpEntity<>(httpHeadersFactory.create()));
	}

	/// Variables ///
	public WksHttpRequest getVariablesListRequest(final String processIntanceId, final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<>(
				extractUrl(bpmEngine) + processInstanceUrl + "/" + processIntanceId + "/variables?deserializeValues=false",
				new HttpEntity<String>(httpHeadersFactory.create()));
	}

	/// Message ////
	public WksHttpRequest getMessageSendRequest(final ProcessMessage processMessage, final BpmEngine bpmEngine) {
		return new C7HttpPostRequest(extractUrl(bpmEngine) + correlateUrl,
				new HttpEntity<>(processMessage, httpHeadersFactory.create()));
	}

	private String extractUrl(final BpmEngine bpmEngine) {
		return bpmEngine.getParameters().get("url").getAsString();
	}

}
