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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.storage.config.StorageConfig;

@Service("FilesystemBucketService")
@ConditionalOnProperty(name = "driver.storage.factoryclass", havingValue = "filesystem")
public class FilesystemBucketService implements com.wks.storage.service.BucketService {

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Autowired
	private StorageConfig config;

	@Override
	public String createAssignedTenant() {
		String bucketByTenant = tenantHolder.getTenantId().get();

		Path base = Paths.get(config.getFilesystemBasePath()).toAbsolutePath().normalize();
		Path bucketDir = base.resolve(bucketByTenant).normalize();

		if (!bucketDir.startsWith(base)) {
			throw new IllegalArgumentException("Resolved bucket path escapes storage base path");
		}

		try {
			Files.createDirectories(bucketDir);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create bucket directory: " + bucketDir, e);
		}

		return bucketByTenant;
	}

	@Override
	public String createObjectWithPath(String dir, String fileName) {
		return String.format("%s/%s", dir, fileName);
	}

	public void setConfig(StorageConfig config) {
		this.config = config;
	}

	public void setTenantHolder(SecurityContextTenantHolder tenantHolder) {
		this.tenantHolder = tenantHolder;
	}

}
