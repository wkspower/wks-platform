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
package com.wks.storage.service.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.storage.config.StorageConfig;

@ExtendWith(MockitoExtension.class)
public class FilesystemBucketServiceTest {

	private FilesystemBucketService service;

	@Mock
	private SecurityContextTenantHolder holder;

	@TempDir
	private Path basePath;

	@BeforeEach
	public void setup() {
		StorageConfig config = new StorageConfig();
		config.setFilesystemBasePath(basePath.toString());

		service = new FilesystemBucketService();
		service.setConfig(config);
		service.setTenantHolder(holder);
	}

	@Test
	public void shouldCreateBucketDirectoryWithTenant() throws Exception {
		when(holder.getTenantId()).thenReturn(Optional.of("app"));

		String bucket = service.createAssignedTenant();

		assertEquals("app", bucket);
		assertTrue(Files.isDirectory(basePath.resolve("app")));
	}

	@Test
	public void shouldConcatObjectWithPath() {
		assertEquals("dirpath/file.csv", service.createObjectWithPath("dirpath", "file.csv"));
	}

}
