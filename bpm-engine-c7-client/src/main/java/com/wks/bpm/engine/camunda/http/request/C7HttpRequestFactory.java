package com.wks.bpm.engine.camunda.http.request;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.model.spi.Deployment;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.rest.client.WksHttpRequest;
import com.wks.rest.client.header.HttpHeadersFactory;

/**
 * @author victor.franca
 *
 */
@Component
public class C7HttpRequestFactory {

	@Autowired
	private HttpHeadersFactory httpHeadersFactory;

	@Value("${camunda7.rest.base-url}")
	private String baseUrl;

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

	public WksHttpRequest getDeploymentCreateRequest(final BpmEngine bpmEngine, final String fileName, String bpmnXml) {

		// This nested HttpEntiy is important to create the correct
		// Content-Disposition entry with metadata "name" and "filename"
		MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
		ContentDisposition contentDisposition = ContentDisposition.builder("form-data").name("data").filename(fileName)
				.build();
		fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
		HttpEntity<byte[]> fileEntity = new HttpEntity<>(bpmnXml.getBytes(), fileMap);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("data", fileEntity);

		return new C7HttpPostRequest(extractUrl(bpmEngine) + deploymentUrl + "/create",
				new HttpEntity<MultiValueMap<String, Object>>(body, httpHeadersFactory.multipart()));
	}

	public WksHttpRequest getDeploymentListRequest(final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<Deployment>(extractUrl(bpmEngine) + deploymentUrl,
				new HttpEntity<>(httpHeadersFactory.json()));
	}

	//// Process Definition ////

	public WksHttpRequest getProcessDefinitionListRequest(final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<ProcessDefinition>(
				extractUrl(bpmEngine) + processDefinitionUrl + "?latestVersion=true",
				new HttpEntity<>(httpHeadersFactory.json()));
	}

	public WksHttpRequest getProcessDefinitionXmlByIdRequest(final String processDefinitionId,
			final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<ProcessDefinition>(
				extractUrl(bpmEngine) + processDefinitionUrl + "/" + processDefinitionId + "/xml",
				new HttpEntity<>(httpHeadersFactory.json()));
	}

	public WksHttpRequest getProcessDefinitionXmlByKeyRequest(final String processDefinitionKey,
			final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<ProcessDefinition>(
				extractUrl(bpmEngine) + processDefinitionUrl + "/key/" + processDefinitionKey + "/xml",
				new HttpEntity<>(httpHeadersFactory.json()));
	}

	//// Process Instance ////

	public WksHttpRequest getProcessInstanceListRequest(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final BpmEngine bpmEngine) {

		StringBuilder url = new StringBuilder().append(extractUrl(bpmEngine) + processInstanceUrl);
		url.append("?");
		if (processDefinitionKey.isPresent()) {
			url.append("&processDefinitionKey=" + processDefinitionKey.get());
		}
		if (businessKey.isPresent()) {
			url.append("&businessKey=" + businessKey.get());
		}

		return new C7HttpGetRequest<ProcessInstance>(url.toString(), new HttpEntity<>(httpHeadersFactory.json()));
	}

	public WksHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey, final BpmEngine bpmEngine,
			final String tenantId) {
		// TODO refactor it - Payload Creator
		ProcessInstance processInstance = ProcessInstance.builder().tenantId(tenantId).build();

		String root = extractUrl(bpmEngine) + processDefinitionUrl;
		String url = String.format("%s/key/%s/tenant-id/%s/start", root, processDefinitionKey, tenantId);

		return new C7HttpPostRequest(url, new HttpEntity<>(processInstance, httpHeadersFactory.json()));
	}

	public WksHttpRequest getProcessInstanceCreateRequest(final String processDefinitionKey, final String businessKey,
			final BpmEngine bpmEngine, String tenantId) {
		// TODO refactor it - Payload Creator
		ProcessInstance processInstance = ProcessInstance.builder().businessKey(businessKey).caseInstanceId(businessKey)
				.tenantId(tenantId).build();

		String root = extractUrl(bpmEngine) + processDefinitionUrl;
		String url = String.format("%s/key/%s/tenant-id/%s/start", root, processDefinitionKey, tenantId);
		return new C7HttpPostRequest(url, new HttpEntity<>(processInstance, httpHeadersFactory.json()));
	}

	public WksHttpRequest getProcessInstanceCreateRequest(String processDefinitionKey, String businessKey,
			JsonObject variables, BpmEngine bpmEngine, final String tenantId) {
		ProcessInstance processInstance = ProcessInstance.builder().businessKey(businessKey).caseInstanceId(businessKey)
				.variables(variables).tenantId(tenantId).build();

		String root = extractUrl(bpmEngine) + processDefinitionUrl;
		String url = String.format("%s/key/%s/tenant-id/%s/start", root, processDefinitionKey, tenantId);
		return new C7HttpPostRequest(url, new HttpEntity<>(processInstance, httpHeadersFactory.json()));
	}

	public WksHttpRequest getProcessInstanceDeleteRequest(String processInstanceId, final BpmEngine bpmEngine) {
		return new C7HttpDeleteRequest(extractUrl(bpmEngine) + processInstanceUrl + "/" + processInstanceId,
				new HttpEntity<>(httpHeadersFactory.json()));
	}

	//// Task ////

	public WksHttpRequest getTaskGetRequest(final String taskId, final BpmEngine bpmEngine) {
		StringBuilder url = new StringBuilder().append(extractUrl(bpmEngine) + taskUrl + "/" + taskId);

		return new C7HttpGetRequest<Task>(url.toString(), new HttpEntity<>(httpHeadersFactory.json()));
	}

	public WksHttpRequest getTaskListRequest(String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		StringBuilder url = new StringBuilder().append(extractUrl(bpmEngine) + taskUrl + "?active=true");
		if (processInstanceBusinessKey != null) {
			url.append("&caseInstanceId=" + processInstanceBusinessKey);
		}

		return new C7HttpGetRequest<Task>(url.toString(), new HttpEntity<>(httpHeadersFactory.json()));
	}

	public WksHttpRequest getTaskClaimRequest(final String taskId, final String taskAssignee,
			final BpmEngine bpmEngine) {

		String assigneeJson = "{ \"userId\": \"" + taskAssignee + "\" }";
		JsonObject assigneeJsonObject = JsonParser.parseString(assigneeJson).getAsJsonObject();

		return new C7HttpPostRequest(extractUrl(bpmEngine) + taskUrl + "/" + taskId + "/claim",
				new HttpEntity<String>(assigneeJsonObject.toString(), httpHeadersFactory.json()));
	}

	public WksHttpRequest getTaskCreateRequest(final Task task, final BpmEngine bpmEngine) {
		return new C7HttpPostRequest(extractUrl(bpmEngine) + taskUrl + "/create",
				new HttpEntity<>(task, httpHeadersFactory.json()));
	}

	public WksHttpRequest getTaskCompleteRequest(final String taskId, JsonObject variables, final BpmEngine bpmEngine) {
		return new C7HttpPostRequest(extractUrl(bpmEngine) + taskUrl + "/" + taskId + "/complete",
				new HttpEntity<String>(variables.toString(), httpHeadersFactory.json()));
	}

	public WksHttpRequest getTaskUnclaimRequest(final String taskId, final BpmEngine bpmEngine) {
		return new C7HttpPostRequest(extractUrl(bpmEngine) + taskUrl + "/" + taskId + "/unclaim",
				new HttpEntity<>(httpHeadersFactory.json()));
	}

	public WksHttpRequest getTaskFormGetRequest(final String taskId, final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<>(extractUrl(bpmEngine) + taskUrl + "/" + taskId + "/deployed-form",
				new HttpEntity<>(httpHeadersFactory.json()));
	}

	// Activity Instances
	public WksHttpRequest getActivityInstancesGetRequest(final String processInstanceId, final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<>(
				extractUrl(bpmEngine) + processInstanceUrl + "/" + processInstanceId + "/activity-instances",
				new HttpEntity<>(httpHeadersFactory.json()));
	}

	/// Variables ///
	public WksHttpRequest getVariablesListRequest(final String processIntanceId, final BpmEngine bpmEngine) {
		return new C7HttpGetRequest<>(extractUrl(bpmEngine) + processInstanceUrl + "/" + processIntanceId
				+ "/variables?deserializeValues=false", new HttpEntity<String>(httpHeadersFactory.json()));
	}

	/// Message ////
	public WksHttpRequest getMessageSendRequest(final ProcessMessage processMessage,
			final Optional<JsonObject> variables, final BpmEngine bpmEngine) {
		
		if(variables.isPresent()){
			processMessage.setProcessVariables(variables.get());	
		}

		return new C7HttpPostRequest(extractUrl(bpmEngine) + correlateUrl,
				new HttpEntity<>(processMessage, httpHeadersFactory.json()));
	}

	private String extractUrl(final BpmEngine bpmEngine) {
		return baseUrl;
	}

}
