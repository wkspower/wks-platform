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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.service.BucketService;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;

@Service("MinioBucketService")
public class MinioBucketService implements BucketService {

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Autowired
	@Qualifier("MinioClient")
	private MinioClientDelegate client;

	@Override
	public String createAssignedTenant() throws Exception {
		String bucketByTenant = tenantHolder.getTenantId().get();

		boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucketByTenant).build());
		if (!found) {
			client.makeBucket(MakeBucketArgs.builder().bucket(bucketByTenant).build());
		}

		return bucketByTenant;
	}

	@Override
	public String createObjectWithPath(String dir, String fileName) throws Exception {
		return String.format("%s/%s", dir, fileName);
	}

}
