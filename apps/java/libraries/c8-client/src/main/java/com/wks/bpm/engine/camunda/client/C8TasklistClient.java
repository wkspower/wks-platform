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
	 * @param processInstanceBusinessKey
	 * @param bpmEngine
	 * @return a list of tasks
	 */
	public Task[] find(String processInstanceKey, BpmEngine bpmEngine) {
		
        String authUrl = taskListUrl
        		+ "/api/login?username=" + taskListUseName + "&password=" + taskListPassword;
        ResponseEntity<String> authResponse = restTemplate.postForEntity(authUrl, null, String.class);

        // Retrieve the session cookie from the response headers
        HttpHeaders authHeaders = authResponse.getHeaders();
        List<String> cookies = authHeaders.get("Set-Cookie");

        // Prepare the session cookie for the subsequent request
        String sessionCookie = "";
        if (cookies != null && !cookies.isEmpty()) {
            sessionCookie = cookies.get(0);
        }
        
		// Define the URL
		String url = taskListUrl + "/v1/tasks/search";

		// Define the request headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.set("Cookie", sessionCookie); 

		// Define the request body
		String requestBody = 
		"{\n" +
        "  \"state\": \"CREATED\",\n" +
        "  \"processInstanceKey\": " + "\"" + processInstanceKey + "\"" +
        "}";

		// Create the RestTemplate instance
		RestTemplate restTemplate = new RestTemplate();

		// Create the HTTP entity with headers and body
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		// Send the request and get the response
		return restTemplate.postForEntity(url, entity, C8Task[].class).getBody();
	}

}
