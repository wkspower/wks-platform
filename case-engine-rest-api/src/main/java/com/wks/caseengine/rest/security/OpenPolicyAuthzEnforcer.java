package com.wks.caseengine.rest.security;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpEntity;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenPolicyAuthzEnforcer implements AccessDecisionVoter<Object> {

	private String opaAuthURL;
	private RestTemplate restTemplate;

	public OpenPolicyAuthzEnforcer(String opaAuthURL) {
		this.opaAuthURL = opaAuthURL;
		this.restTemplate = new RestTemplate();
	}

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public int vote(Authentication authentication, Object obj, Collection<ConfigAttribute> attributes) {
		if (!(obj instanceof FilterInvocation)) {
			return ACCESS_ABSTAIN;
		}

		FilterInvocation filter = (FilterInvocation) obj;
		HttpServletRequest request = filter.getRequest();
		
		Map<String, String> headers = new HashMap<String, String>();
		for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
			String header = headerNames.nextElement();
			headers.put(header, request.getHeader(header));
		}

		String[] path = request.getRequestURI().replaceAll("^/|/$", "").split("/");
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("method", request.getMethod().toUpperCase());
		input.put("path", path[0]);
		
		if (authentication.getCredentials() instanceof Jwt) {
			Jwt jwt = (Jwt) authentication.getCredentials();
			input.put("realm_access", jwt.getClaimAsMap("realm_access"));
		}
		
		HttpEntity<?> body = new HttpEntity<>(new OpenPolicyRequest(input));
		OpenPolicyResponse response = restTemplate.postForObject(this.opaAuthURL, body, OpenPolicyResponse.class);
		
		if (!response.getResult()) {
			log.info("Denied: " + response.getResult() + ", Input: " + input);
			return ACCESS_DENIED;
		}
		
		log.info("Allowed: " + response.getResult() + ", Input: " + input);
		return ACCESS_GRANTED;
	}

}