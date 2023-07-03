package com.wks.emailtocase.mocks;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;

public class MockSecurityContext implements SecurityContext {

	private static final long serialVersionUID = 1L;

	private Authentication authz;

	public MockSecurityContext(String org, String allowedOrigem) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("org", org);
		claims.put("allowed-origins", Arrays.asList(allowedOrigem));
		this.authz = new AuthenticationMock(claims);
	}

	@Override
	public Authentication getAuthentication() {
		return authz;
	}

	@Override
	public void setAuthentication(Authentication authentication) {
		this.authz = authentication;
	}

	static class AuthenticationMock implements Authentication {

		private static final long serialVersionUID = 1L;

		private Jwt credentials;

		public AuthenticationMock(Map<String, Object> claims) {
			String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJvcmciOiJ3a3MifQ.oH4f1nqG55Qlznibk9AMC5y3CHvixL45tEhdN8dnBDk";
			Map<String, Object> headers = new HashMap<>();
			headers.put("alg", "HS256");
			headers.put("typ", "JWT");
			this.credentials = new Jwt(token, null, null, headers, claims);
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

}
