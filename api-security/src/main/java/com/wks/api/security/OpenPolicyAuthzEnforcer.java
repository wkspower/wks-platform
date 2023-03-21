package com.wks.api.security;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
		this.handler = handler;
		this.restTemplate = createRestTemplate();
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
	
	private RestTemplate createRestTemplate() {
	 	try {
	 	    TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
	 	    SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
	 	    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
	 	    CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
	 	    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	 	    requestFactory.setHttpClient(httpClient);
	 	    return new RestTemplate(requestFactory);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
	}

}