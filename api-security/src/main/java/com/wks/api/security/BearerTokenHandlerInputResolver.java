package com.wks.api.security;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import com.wks.api.security.utils.HttpUtils;

public final class BearerTokenHandlerInputResolver implements HandlerInputResolver {

	@Override
	public Map<String, Object> resolver(HttpServletRequest request, Authentication authentication) {
		return inputResolver(request, authentication);
	}
	
	private Map<String, Object> inputResolver(HttpServletRequest request, Authentication authentication) {
		Map<String, String> headers = new HashMap<String, String>();
		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
			String header = headerNames.nextElement();
			headers.put(header, request.getHeader(header));
		}
		
		String[] path = request.getRequestURI().replaceAll("^/|/$", "").split("/");
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("method", HttpUtils.getMethod(request));
		input.put("path", path[0]);
		input.put("host", HttpUtils.getHost(request.getHeader("origin")));

		if (authentication != null && authentication.getCredentials() instanceof Jwt) {
			Jwt jwt = (Jwt) authentication.getCredentials();
			input.put("org", jwt.getClaim("org"));
			input.put("allowed_origin", getAllowedOrigin(jwt));
			input.put("realm_access", jwt.getClaimAsMap("realm_access"));
		}
		
		return input;
	}

	@SuppressWarnings("unchecked")
	private String getAllowedOrigin(Jwt jwt) {
		return HttpUtils.getHost(((List<String>) jwt.getClaim("allowed-origins")).get(0));
	}
	
}
