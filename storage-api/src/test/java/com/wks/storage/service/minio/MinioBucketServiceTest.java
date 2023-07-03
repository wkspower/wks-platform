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
package com.wks.storage.service.minio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.storage.driver.MinioClientDelegate;

@ExtendWith(MockitoExtension.class)
public class MinioBucketServiceTest {

	@InjectMocks
	private MinioBucketService service;

	@Mock
	private MinioClientDelegate client;

	@Mock
	private SecurityContextTenantHolder holder;

	@Test
	public void shouldCreateBucketNameWithTentant() throws Exception {
		when(holder.getTenantId()).thenReturn(Optional.of("app"));

		String bucket = service.createAssignedTenant();

		assertEquals("app", bucket);
		verify(client).makeBucket(notNull());
	}

}
