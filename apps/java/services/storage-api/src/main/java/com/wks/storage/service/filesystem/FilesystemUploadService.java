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

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.wks.storage.config.StorageConfig;
import com.wks.storage.model.UploadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.UploadService;

@Service("FilesystemUploadService")
@ConditionalOnProperty(name = "driver.storage.factoryclass", havingValue = "filesystem")
public class FilesystemUploadService implements UploadService {

	@Autowired
	private StorageConfig config;

	@Autowired
	@Qualifier("FilesystemBucketService")
	private BucketService bucketService;

	@Override
	public UploadFileUrl createPresignedPostFormData(String fileName, String contentType) {
		return createUrl(null, fileName);
	}

	@Override
	public UploadFileUrl createPresignedPostFormData(String dir, String fileName, String contentType) {
		return createUrl(dir, fileName);
	}

	private UploadFileUrl createUrl(String dir, String fileName) {
		String bucketName = bucketService.createAssignedTenant();

		String objectName = fileName;
		if (dir != null && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}

		String uploadUrl = FilesystemUrls.objectUrl(config, "uploads", bucketName, objectName);

		return new UploadFileUrl(uploadUrl, Collections.<String, String>emptyMap());
	}

	public void setConfig(StorageConfig config) {
		this.config = config;
	}

	public void setBucketService(BucketService bucketService) {
		this.bucketService = bucketService;
	}

}
