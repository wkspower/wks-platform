package com.wks.api.security.context;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public final class SecurityContextTenantHolderImpl implements SecurityContextTenantHolder {

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
