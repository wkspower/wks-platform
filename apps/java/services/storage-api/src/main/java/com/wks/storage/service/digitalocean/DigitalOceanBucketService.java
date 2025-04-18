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
package com.wks.storage.service.digitalocean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.storage.config.StorageConfig;
import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.service.BucketService;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;

@Service("DigitalOceanBucketService")
public class DigitalOceanBucketService implements BucketService {

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Autowired
	private StorageConfig config;

	@Autowired
	@Qualifier("DigitalOceanClient")
	private MinioClientDelegate client;

	@Override
	public String createAssignedTenant() {
		String bucketName = String.format("%s-%s", config.getBucketPrefix(), tenantHolder.getTenantId().get());

		boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		if (!found) {
			client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
		}

		return bucketName;
	}

	@Override
	public String createObjectWithPath(String dir, String fileName) {
		return String.format("%s/%s", dir, fileName);
	}

	public void setConfig(StorageConfig config) {
		this.config = config;
	}

	@Override
	public String createObjectWithNestedPath(String vertical, String site, String plant, String fileName) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'createObjectWithNestedPath'");
	}

}
