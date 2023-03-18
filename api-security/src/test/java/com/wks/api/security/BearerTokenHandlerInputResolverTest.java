package com.wks.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.wks.api.security.mocks.MockAuthentication;

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
		
		assertFalse(input.isEmpty());
		assertEquals(new HashMap<String, Object>(){{
			put("path", "files");
			put("method", "GET");
			put("realm_access", null);
			put("org", "wks");
			put("host", "localhost");
			put("allowed_origin", "localhost");
		}}, input);
	}

}
