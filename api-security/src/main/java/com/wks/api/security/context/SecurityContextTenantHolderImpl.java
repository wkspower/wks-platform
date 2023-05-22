package com.wks.api.security.context;

import java.util.Optional;

public final class SecurityContextTenantHolderImpl implements SecurityContextTenantHolder {

	private Optional<String> tenantId = Optional.empty();

	@Override
	public Optional<String> getTenantId() {
		return Optional.of(tenantId.get());
	}

	@Override
	public void setTenantId(final String tenantId) {
		this.tenantId = Optional.of(tenantId);
	}

	@Override
	public void clear() {
		tenantId = null;
	}

}
