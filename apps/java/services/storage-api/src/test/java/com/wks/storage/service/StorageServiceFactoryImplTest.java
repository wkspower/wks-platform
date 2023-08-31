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
package com.wks.storage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StorageServiceFactoryImplTest {

	@InjectMocks
	private StorageServiceFactoryImpl storageServiceFactory;

	@Mock
	private ServiceFactory minio;

	@Mock
	private ServiceFactory digitalOcean;

	@Test
	public void shouldCreateFactoryForMinioDriver() {
		ServiceFactory factory = storageServiceFactory.getFactory("minio");

		assertSame(factory, minio);
	}

	@Test
	public void shouldCreateFactoryForDigitalOceanDriver() {
		ServiceFactory factory = storageServiceFactory.getFactory("do");

		assertSame(factory, digitalOcean);
	}

	@Test
	public void shouldThrowExceptionWhenInvalidDriver() {
		IllegalArgumentException exception = assertThrowsExactly(IllegalArgumentException.class,
				() -> storageServiceFactory.getFactory("unknow"));

		assertNotNull(exception);
		assertEquals("Factory name 'unknow' not found", exception.getMessage());
	}

}
