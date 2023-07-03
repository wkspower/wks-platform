package com.wks.api.security.mocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

public class MockSecurityContext implements SecurityContext {

	private static final long serialVersionUID = 1L;

	private Authentication authz;

	public MockSecurityContext(String org, String allowedOrigem) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("org", org);
		claims.put("allowed-origins", Arrays.asList(allowedOrigem));
		this.authz = new MockAuthentication(claims);
	}

	@Override
	public Authentication getAuthentication() {
		return authz;
	}

	@Override
	public void setAuthentication(Authentication authentication) {
		this.authz = authentication;
	}

}
