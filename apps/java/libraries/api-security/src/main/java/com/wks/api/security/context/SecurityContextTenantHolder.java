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

public interface SecurityContextTenantHolder {

	Optional<String> getTenantId();

	void setTenantId(String tenantId);
	
	Optional<String> getUserId();

	void setUserId(String userId);


	void clear();

}
