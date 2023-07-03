package com.wks.caseengine.client.invoker.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class HttpBasicAuth implements Authentication {
	private String username;
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void applyToParams(MultiValueMap<String, String> queryParams, HttpHeaders headerParams,
			MultiValueMap<String, String> cookieParams) {
		if (username == null && password == null) {
			return;
		}
		String str = (username == null ? "" : username) + ":" + (password == null ? "" : password);
		headerParams.add(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8)));
	}
}
