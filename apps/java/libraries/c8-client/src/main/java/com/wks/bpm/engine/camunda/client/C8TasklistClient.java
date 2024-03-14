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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.stereotype.Component;

@Component
public class C8TasklistClient {

	public HttpResponse callGraphQLService(String url, String query) throws URISyntaxException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		URI uri = new URIBuilder(request.getUri()).addParameter("query", query).build();
		request.setUri(uri);
		return client.execute(request);
	}

}
