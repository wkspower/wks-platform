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
package com.wks.caseengine.tenancy;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.wks.api.security.context.SecurityContextTenantHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * Single-tenant strategy: resolve to the context tenant when present, else the
 * configured default tenant (database / schema).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "wks.tenancy.multi-tenant", havingValue = "false")
public class SingleTenantResolver implements TenantResolver {

	private final SecurityContextTenantHolder holder;

	private final String defaultTenant;

	public SingleTenantResolver(final SecurityContextTenantHolder holder,
			@Value("${wks.tenancy.default-tenant:public}") final String defaultTenant) {
		this.holder = holder;
		this.defaultTenant = defaultTenant;
	}

	@Override
	public String resolveTenant() {
		Optional<String> tenantId = holder.getTenantId();

		if (!tenantId.isEmpty()) {
			log.debug("using tenant database {}", tenantId.get());
			return tenantId.get();
		}

		log.debug("no tenant in context; using single-tenant default database {}", defaultTenant);
		return defaultTenant;
	}

	@Override
	public String defaultTenant() {
		return defaultTenant;
	}

	@Override
	public boolean isMultiTenant() {
		return false;
	}

}
