package com.wks.api.security;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpEntity;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OpenPolicyAuthzEnforcer implements AccessDecisionVoter<Object> {

	private String opaAuthURL;
	private RestTemplate restTemplate;
	private HandlerInputResolver handler;

	public OpenPolicyAuthzEnforcer(String opaAuthURL) {
		this(opaAuthURL, new BearerTokenHandlerInputResolver());
	}
	
	public OpenPolicyAuthzEnforcer(String opaAuthURL, HandlerInputResolver handler) {
		this.opaAuthURL = opaAuthURL;
		this.restTemplate = new RestTemplate();
		this.handler = handler;
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
		
		Map<String, Object> input = handler.resolver(request, authentication);

		HttpEntity<?> body = new HttpEntity<>(new OpenPolicyRequest(input));
		OpenPolicyResponse response = restTemplate.postForObject(this.opaAuthURL, body, OpenPolicyResponse.class);

		if (!response.getResult()) {
			log.debug("Denied with Input -> {}", input);
			return ACCESS_DENIED;
		}

		log.debug("Allowed with Input -> {}", input);
		return ACCESS_GRANTED;
	}

}