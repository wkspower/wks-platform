package com.wks.bpm.engine.camunda.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

@Component
public class C8TasklistClient {
	
	public HttpResponse callGraphQLService(String url, String query) throws URISyntaxException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		URI uri = new URIBuilder(request.getURI()).addParameter("query", query).build();
		request.setURI(uri);
		return client.execute(request);
	}

}
