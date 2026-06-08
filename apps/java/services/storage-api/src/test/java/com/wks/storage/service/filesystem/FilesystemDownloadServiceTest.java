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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.storage.config.StorageConfig;
import com.wks.storage.model.DownloadFileUrl;
import com.wks.storage.service.BucketService;

@ExtendWith(MockitoExtension.class)
public class FilesystemDownloadServiceTest {

	@InjectMocks
	private FilesystemDownloadService service;

	@Mock
	private BucketService bucketService;

	private StorageConfig config;

	@BeforeEach
	public void setup() {
		config = new StorageConfig();
		config.setUploadsProtocol("http");
		config.setUploadsBackendUrl("localhost");
		config.setUploadsPort(8085);
		service.setConfig(config);
	}

	@Test
	public void shouldCreateDownloadUrlWithoutDir() {
		when(bucketService.createAssignedTenant()).thenReturn("app");

		DownloadFileUrl download = service.createPresignedObjectUrl("file.csv", "application/csv");

		assertNotNull(download);
		assertEquals("http://localhost:8085/storage/filesystem/app/downloads?object=file.csv", download.getUrl());
	}

	@Test
	public void shouldCreateDownloadUrlWithDir() {
		when(bucketService.createAssignedTenant()).thenReturn("app");
		when(bucketService.createObjectWithPath("dirpath", "file.csv")).thenReturn("dirpath/file.csv");

		DownloadFileUrl download = service.createPresignedObjectUrl("dirpath", "file.csv", "application/csv");

		assertNotNull(download);
		assertEquals("http://localhost:8085/storage/filesystem/app/downloads?object=dirpath%2Ffile.csv",
				download.getUrl());
	}

}
