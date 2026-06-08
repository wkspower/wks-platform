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

/**
 * Strategy for resolving the tenant (database / schema) for the current context.
 * The active implementation is selected by {@code wks.tenancy.multi-tenant}.
 */
public interface TenantResolver {

	/**
	 * Tenant for the current context. Multi-tenant: the context tenant, else fail
	 * closed. Single-tenant: the context tenant, else the default.
	 */
	String resolveTenant();

	String defaultTenant();

	boolean isMultiTenant();

}
