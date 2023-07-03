package com.wks.api.security.mocks;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class MockAuthentication implements Authentication {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJvcmciOiJ3a3MifQ.oH4f1nqG55Qlznibk9AMC5y3CHvixL45tEhdN8dnBDk";

	private Jwt credentials;

	public MockAuthentication(Map<String, Object> claims) {
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "HS256");
		headers.put("typ", "JWT");
		this.credentials = new Jwt(DEFAULT_TOKEN, null, null, headers, claims);
	}

	@SuppressWarnings("serial")
	public MockAuthentication(String org, String allowedOrigin) {
		this(new HashMap<String, Object>() {
			{
				put("org", org);
				put("allowed-origins", Arrays.asList(allowedOrigin));
			}
		});
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		return false;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
	}
}