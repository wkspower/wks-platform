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
package com.wks.emailtocase.security;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.emailtocase.caseemail.Organization;
import com.wks.emailtocase.repository.OrganizationRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ApiKeyAuthzEnforcer implements AccessDecisionVoter<Object> {

	@Autowired
	private OrganizationRepository repository;

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Override
	public boolean supports(ConfigAttribute cfg) {
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

		AntPathRequestMatcher antMatcher = new AntPathRequestMatcher("/actuator/*");
		if (antMatcher.matches(request)) {
			return ACCESS_ABSTAIN;
		}

		try {
			MailServerInputRequestResolver handler = new MailServerInputRequestResolver();

			Map<String, Object> params = handler.resolver(request, null);

			String tenantId = (String) params.get("org");
			if (tenantId == null || tenantId.isBlank()) {
				log.error("Could't find tenantId by subdomain, it was expected to be filled but it is empty {}",
						tenantId);
				return ACCESS_DENIED;
			}

			tenantHolder.setTenantId(tenantId);

			Organization organization = repository.get();

			if (!Objects.equals(organization.getMailReceiveApiKey(), request.getParameter("apiKey"))) {
				log.error("The API key was not found or not the expected value.");
				return ACCESS_DENIED;
			}

			return ACCESS_GRANTED;
		} finally {
			tenantHolder.clear();
		}
	}

}
