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

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.wks.storage.config.StorageConfig;
import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.model.UploadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.UploadService;

import io.minio.PostPolicy;

@Service("MinioUploadService")
public class MinioUploadService implements UploadService {

	@Autowired
	private StorageConfig config;

	@Autowired
	@Qualifier("MinioClient")
	private MinioClientDelegate client;

	@Autowired
	@Qualifier("MinioBucketService")
	private BucketService bucketService;

	@Override
	public UploadFileUrl createPresignedPostFormData(String fileName, String contentType) {
		return createPresigned(null, fileName, contentType);
	}

	@Override
	public UploadFileUrl createPresignedPostFormData(String dir, String fileName, String contentType) {
		return createPresigned(dir, fileName, contentType);
	}

	private UploadFileUrl createPresigned(String dir, String fileName, String contentType) {
		String bucketName = bucketService.createAssignedTenant();

		String objectName = fileName;
		if (dir != null && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}

		PostPolicy policy = new PostPolicy(bucketName, ZonedDateTime.now().plusMinutes(5));
		policy.addEqualsCondition("key", objectName);
		policy.addStartsWithCondition("Content-Type", contentType.split("/")[0]);
		policy.addContentLengthRangeCondition(config.getUploadsFileMinSize(), config.getUploadsFileMaxSize());

		Map<String, String> formData = client.getPresignedPostFormData(policy);

		String port = config.getUploadsPort() > 0 ? ":" + config.getUploadsPort() : "";

		String callBackUrl = String.format("%s://%s%s/%s", config.getUploadsProtocol(), config.getUploadsBackendUrl(),
				port, bucketName);

		return new UploadFileUrl(callBackUrl, formData);
	}

	public void setConfig(StorageConfig config) {
		this.config = config;
	}

}
