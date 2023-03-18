package com.wks.api.security.context;

import java.util.Optional;

public interface SecurityContextTenantHolder {

	Optional<String> getTenantId();

	void setTenantId(String tenantId);

	void clear();
	
}
