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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wks.storage.config.StorageConfig;
import com.wks.storage.model.DownloadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.DownloadService;

@Service("FilesystemDownloadService")
public class FilesystemDownloadService implements DownloadService {

	@Autowired
	private StorageConfig config;

	@Autowired
	@Qualifier("FilesystemBucketService")
	private BucketService bucketService;

	@Override
	public DownloadFileUrl createPresignedObjectUrl(String fileName, String contentType) {
		return createUrl(null, fileName);
	}

	@Override
	public DownloadFileUrl createPresignedObjectUrl(String dir, String fileName, String contentType) {
		return createUrl(dir, fileName);
	}

	private DownloadFileUrl createUrl(String dir, String fileName) {
		String bucketName = bucketService.createAssignedTenant();

		String objectName = fileName;
		if (dir != null && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}

		String downloadUrl = FilesystemUrls.objectUrl(config, "downloads", bucketName, objectName);

		return new DownloadFileUrl(downloadUrl);
	}

	public void setConfig(StorageConfig config) {
		this.config = config;
	}

	public void setBucketService(BucketService bucketService) {
		this.bucketService = bucketService;
	}

}
