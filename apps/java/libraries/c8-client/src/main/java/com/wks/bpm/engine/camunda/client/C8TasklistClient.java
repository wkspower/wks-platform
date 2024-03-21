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

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.model.spi.Task;

@Component
public class C8TasklistClient {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${camunda.tasklist.client.url}")
	private String taskListUrl;

	@Value("${camunda.tasklist.client.username}")
	private String taskListUseName;

	@Value("${camunda.tasklist.client.password}")
	private String taskListPassword;

	/**
	 * @param taskId
	 * @param bpmEngine
	 * @return
	 */
	public Task getTask(String taskId, BpmEngine bpmEngine) {
		HttpHeaders headers = getHeaders();

		String url = taskListUrl + "/v1/tasks/" + taskId;

		HttpEntity<String> entity = new HttpEntity<>(headers);

		return restTemplate.exchange(url, HttpMethod.GET, entity, C8Task.class).getBody();
	}

	/**
	 * @param processInstanceBusinessKey
	 * @param bpmEngine
	 * @return a list of tasks
	 */
	public Task[] find(String processInstanceKey, BpmEngine bpmEngine) {

		HttpHeaders headers = getHeaders();

		String url = taskListUrl + "/v1/tasks/search";

		String requestBody = "{\n" + "  \"state\": \"CREATED\",\n" + "  \"processInstanceKey\": " + "\""
				+ processInstanceKey + "\"" + "}";

		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		return restTemplate.postForEntity(url, entity, C8Task[].class).getBody();
	}

	/**
	 * @param taskId
	 * @param taskAssignee
	 * @param bpmEngine
	 */
	public void claimTask(String taskId, String taskAssignee, BpmEngine bpmEngine) {
		HttpHeaders headers = getHeaders();

		String url = taskListUrl + "/v1/tasks/" + taskId + "/assign";

		String requestBody = "{\n" + "  \"assignee\": \"" + taskAssignee + "\",\n" + "  \"allowOverrideAssignment\": "
				+ "\"true\"" + "}";

		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		restTemplate.patchForObject(url, entity, String.class);
	}

	/**
	 * @param taskId
	 * @param bpmEngine
	 */
	public void unclaimTask(String taskId, BpmEngine bpmEngine) {
		HttpHeaders headers = getHeaders();

		String url = taskListUrl + "/v1/tasks/" + taskId + "/unassign";

		HttpEntity<String> entity = new HttpEntity<>(headers);

		restTemplate.patchForObject(url, entity, String.class);
	}

	/**
	 * @param taskId
	 * @param variables
	 * @param bpmEngine
	 */
	public C8Task complete(String taskId, BpmEngine bpmEngine) {
		HttpHeaders headers = getHeaders();

		String url = taskListUrl + "/v1/tasks/" + taskId + "/complete";

		String requestBody = "{\"variables\": []" + "}";

		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		return restTemplate.patchForObject(url, entity, C8Task.class);
	}

	private HttpHeaders getHeaders() {
		String authUrl = taskListUrl + "/api/login?username=" + taskListUseName + "&password=" + taskListPassword;
		ResponseEntity<String> authResponse = restTemplate.postForEntity(authUrl, null, String.class);

		HttpHeaders authHeaders = authResponse.getHeaders();
		List<String> cookies = authHeaders.get("Set-Cookie");

		String sessionCookie = "";
		if (cookies != null && !cookies.isEmpty()) {
			sessionCookie = cookies.get(0);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.set("Cookie", sessionCookie);
		return headers;
	}

}
