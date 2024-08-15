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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wks.api.security.mocks.MockAuthentication;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class BearerTokenHandlerInputResolverTest {

	@InjectMocks
	private BearerTokenHandlerInputResolver resolver;

	private MockHttpServletRequest request;

	@BeforeEach
	public void setup() {
		request = new MockHttpServletRequest();
	}

	@Test
	@SuppressWarnings("serial")
	public void shouldGetInputParsedFromBearerToken() {
		MockAuthentication authz = new MockAuthentication("wks", "http://localhost:3000");
		request.setRequestURI("/files/1?id=1");
		request.setMethod("get");
		request.addHeader("origin", "http://localhost:3000");

		Map<String, Object> input = resolver.resolver(request, authz);

		assertThat(input).containsAllEntriesOf(new HashMap<>() {
			{
				put("path", "files");
				put("method", "GET");
				put("realm_access", null);
				put("org", "wks");
				put("host", "localhost");
				put("allowed_origin", "localhost");
			}
		});
	}

}
