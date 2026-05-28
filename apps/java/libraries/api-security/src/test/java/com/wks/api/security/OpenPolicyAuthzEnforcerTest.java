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
package com.wks.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.wks.api.security.mocks.MockAuthentication;

class OpenPolicyAuthzEnforcerTest {

	private static final String OPA_URL = "http://opa.test/v1/data/wks/authz/allow";

	private RestTemplate restTemplate;
	private OpenPolicyAuthzEnforcer enforcer;

	@BeforeEach
	void setUp() {
		enforcer = new OpenPolicyAuthzEnforcer(OpenPolicyAuthzEnforcerConfig.builder()
				.opaAuthURL(OPA_URL).actuatorEnabled(true).swaggerEnabled(true).build());
		restTemplate = mock(RestTemplate.class);
		ReflectionTestUtils.setField(enforcer, "restTemplate", restTemplate);
	}

	@Test
	void allowsActuatorWithoutCallingOpa() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
		AuthorizationDecision decision = enforcer.authorize(authSupplier(), new RequestAuthorizationContext(request));
		assertThat(decision.isGranted()).isTrue();
	}

	@Test
	void allowsSwaggerWithoutCallingOpa() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
		AuthorizationDecision decision = enforcer.authorize(authSupplier(), new RequestAuthorizationContext(request));
		assertThat(decision.isGranted()).isTrue();
	}

	@Test
	void allowsWhenOpaSaysTrue() {
		when(restTemplate.postForObject(eq(OPA_URL), any(), eq(OpenPolicyResponse.class)))
				.thenReturn(response(true));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cases");

		AuthorizationDecision decision = enforcer.authorize(authSupplier(), new RequestAuthorizationContext(request));

		assertThat(decision.isGranted()).isTrue();
	}

	@Test
	void deniesWhenOpaSaysFalse() {
		when(restTemplate.postForObject(eq(OPA_URL), any(), eq(OpenPolicyResponse.class)))
				.thenReturn(response(false));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cases");

		AuthorizationDecision decision = enforcer.authorize(authSupplier(), new RequestAuthorizationContext(request));

		assertThat(decision.isGranted()).isFalse();
	}

	@Test
	void deniesWhenOpaReturnsNullBody() {
		when(restTemplate.postForObject(eq(OPA_URL), any(), eq(OpenPolicyResponse.class)))
				.thenReturn(null);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cases");

		AuthorizationDecision decision = enforcer.authorize(authSupplier(), new RequestAuthorizationContext(request));

		assertThat(decision.isGranted()).isFalse();
	}

	@Test
	void failsClosedWhenOpaDown() {
		when(restTemplate.postForObject(eq(OPA_URL), any(), eq(OpenPolicyResponse.class)))
				.thenThrow(new ResourceAccessException("connection refused"));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cases");

		assertThatThrownBy(() -> enforcer.authorize(authSupplier(), new RequestAuthorizationContext(request)))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void deniesWhenAuthenticationSupplierThrows() {
		Supplier<? extends Authentication> throwing = () -> {
			throw new AuthenticationCredentialsNotFoundException("no auth");
		};
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cases");

		AuthorizationDecision decision = enforcer.authorize(throwing, new RequestAuthorizationContext(request));

		assertThat(decision.isGranted()).isFalse();
	}

	private static Supplier<? extends Authentication> authSupplier() {
		return () -> new MockAuthentication("wks", "http://localhost");
	}

	private static OpenPolicyResponse response(boolean result) {
		OpenPolicyResponse response = new OpenPolicyResponse();
		ReflectionTestUtils.setField(response, "result", result);
		return response;
	}
}
