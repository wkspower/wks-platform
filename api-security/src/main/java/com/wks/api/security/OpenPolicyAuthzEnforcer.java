package com.wks.api.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class OpenPolicyAuthzEnforcer implements AccessDecisionVoter<Object> {

	private String opaAuthURL;
	private RestTemplate restTemplate;
	private HandlerInputResolver handler;
	private List<AntPathRequestMatcher> matchers;

	public OpenPolicyAuthzEnforcer(String opaAuthURL) {
		this(opaAuthURL, new BearerTokenHandlerInputResolver());
	}

	public OpenPolicyAuthzEnforcer(String opaAuthURL, HandlerInputResolver handler) {
		this.opaAuthURL = opaAuthURL;
		this.handler = handler;
		this.restTemplate = createRestTemplate();
		this.matchers = Arrays.asList(new AntPathRequestMatcher("/actuator/**"),
				new AntPathRequestMatcher("/swagger-ui/**"), new AntPathRequestMatcher("/v3/api-docs/**"));
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

		if (matchers.stream().filter(f -> f.matches(request)).count() > 0) {
			return ACCESS_GRANTED;
		}

		Map<String, Object> input = handler.resolver(request, authentication);

		HttpEntity<?> body = new HttpEntity<>(new OpenPolicyRequest(input));
		OpenPolicyResponse response = restTemplate.postForObject(this.opaAuthURL, body, OpenPolicyResponse.class);
		if (response == null) {
			throw new RuntimeException("Error connecting to OPA Server");
		}

		if (!response.getResult()) {
			log.debug("Denied with Input -> {}", input);
			return ACCESS_DENIED;
		}

		log.debug("Allowed with Input -> {}", input);
		return ACCESS_GRANTED;
	}

	private RestTemplate createRestTemplate() {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		return new RestTemplate(requestFactory);
	}

}