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

public final class SecurityContextTenantHolderImpl implements SecurityContextTenantHolder {

	private Optional<String> tenantId = Optional.empty();
	private Optional<String> userId = Optional.empty();

	@Override
	public Optional<String> getTenantId() {
		return Optional.of(tenantId.get());
	}

	@Override
	public void setTenantId(final String tenantId) {
		this.tenantId = Optional.of(tenantId);
	}
	
	@Override
	public Optional<String> getUserId() {
		return Optional.of(userId.get());
	}

	@Override
	public void setUserId(String userId) {
		this.userId = Optional.of(userId);
	}


	@Override
	public void clear() {
		this.tenantId = null;
		this.userId = null;
		
	}

}
