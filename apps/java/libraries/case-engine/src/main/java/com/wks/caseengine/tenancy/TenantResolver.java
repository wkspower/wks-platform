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

	/**
	 * Tenant for the current context, falling back to {@link #defaultTenant()}
	 * instead of failing when none is available. Used by the JPA identifier
	 * resolver: Hibernate also invokes it at bootstrap / schema generation (no
	 * request context), where it must yield a schema rather than throw. Request-time
	 * fail-closed enforcement stays on the {@link #resolveTenant()} path (Mongo).
	 */
	default String resolveTenantOrDefault() {
		try {
			return resolveTenant();
		} catch (RuntimeException ex) {
			return defaultTenant();
		}
	}

	String defaultTenant();

	boolean isMultiTenant();

}
