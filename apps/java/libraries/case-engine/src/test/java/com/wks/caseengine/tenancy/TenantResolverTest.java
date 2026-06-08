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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.api.security.context.SecurityContextTenantHolder;

@ExtendWith(MockitoExtension.class)
public class TenantResolverTest {

	@Mock
	private SecurityContextTenantHolder holder;

	@Test
	void multiTenantShouldReturnContextTenantWhenPresent() {
		when(holder.getTenantId()).thenReturn(Optional.of("tenant_123"));

		MultiTenantResolver resolver = new MultiTenantResolver(holder, "public");

		assertEquals("tenant_123", resolver.resolveTenant());
		assertTrue(resolver.isMultiTenant());
		assertEquals("public", resolver.defaultTenant());
	}

	@Test
	void multiTenantShouldThrowWhenNoContextTenant() {
		when(holder.getTenantId()).thenReturn(Optional.empty());

		MultiTenantResolver resolver = new MultiTenantResolver(holder, "public");

		assertThrows(IllegalArgumentException.class, () -> resolver.resolveTenant());
	}

	@Test
	void singleTenantShouldReturnContextTenantWhenPresent() {
		when(holder.getTenantId()).thenReturn(Optional.of("tenant_123"));

		SingleTenantResolver resolver = new SingleTenantResolver(holder, "public");

		assertEquals("tenant_123", resolver.resolveTenant());
		assertFalse(resolver.isMultiTenant());
	}

	@Test
	void singleTenantShouldReturnDefaultWhenNoContextTenant() {
		when(holder.getTenantId()).thenReturn(Optional.empty());

		SingleTenantResolver resolver = new SingleTenantResolver(holder, "public");

		assertEquals("public", resolver.resolveTenant());
		assertEquals("public", resolver.defaultTenant());
	}

}
