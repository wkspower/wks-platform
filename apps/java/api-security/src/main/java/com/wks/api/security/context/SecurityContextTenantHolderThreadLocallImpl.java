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
package com.wks.api.security.context;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public final class SecurityContextTenantHolderThreadLocallImpl implements SecurityContextTenantHolder {

	private ThreadLocal<String> tenantId = new ThreadLocal<>();

	@Override
	public Optional<String> getTenantId() {
		return Optional.of(tenantId.get());
	}

	@Override
	public void setTenantId(final String tenantId) {
		this.tenantId.set(tenantId);
	}

	@Override
	public void clear() {
		tenantId.remove();
	}

}
