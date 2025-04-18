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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.model.DownloadFileUrl;
import com.wks.storage.service.BucketService;
import com.wks.storage.service.DownloadService;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;

@Service("MinioDownloadService")
public class MinioDownloadService implements DownloadService {

	@Autowired
	@Qualifier("MinioClient")
	private MinioClientDelegate client;

	@Autowired
	@Qualifier("MinioBucketService")
	private BucketService bucketService;

	@Override
	public DownloadFileUrl createPresignedObjectUrl(String fileName, String contentType) {
		return createPresigned(null, fileName, contentType);
	}

	@Override
	public DownloadFileUrl createPresignedObjectUrl(String dir, String fileName, String contentType) {
		return createPresigned(dir, fileName, contentType);
	}

	private DownloadFileUrl createPresigned(String dir, String fileName, String contentType) {
		Map<String, String> params = new HashMap<>();
		params.put("response-content-type", contentType);

		String bucketName = bucketService.createAssignedTenant();

		String objectName = fileName;
		if (dir != null && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}

		GetPresignedObjectUrlArgs signed = GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName)
				.object(objectName).expiry(1, TimeUnit.MINUTES).extraQueryParams(params).build();

		String url = client.getPresignedObjectUrl(signed);

		return new DownloadFileUrl(url);
	}

	public ResponseEntity<InputStreamResource> downloadObj(String dir, String fileName, String contentType) {
		// Generate pre-signed URL for GET (Download)
		String bucketName = bucketService.createAssignedTenant();
		String objectName = fileName;
		if (dir != null && !dir.isBlank()) {
			objectName = bucketService.createObjectWithPath(dir, fileName);
		}
		GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
		GetObjectResponse response = client.getObject(getObjectArgs);
		String fileContentType = response.headers().get(HttpHeaders.CONTENT_TYPE);
		if (fileContentType == null) {
			fileContentType = "application/octet-stream"; // Default content type for unknown files
		}
		InputStream fileInputStream = new BufferedInputStream(response); // Buffered stream for efficiency
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		headers.add(HttpHeaders.CONTENT_TYPE, fileContentType); // Use content type from MinIO response

		return ResponseEntity.ok()
				.headers(headers)
				.body(new InputStreamResource(fileInputStream));
	}

	public ResponseEntity<InputStreamResource> downloadFile(String vertical, String site, String plant, String fileName,
			String contentType) {
		// Generate pre-signed URL for GET (Download)
		String bucketName = bucketService.createAssignedTenant();
		String objectName = fileName;

		System.out.println("0") ;
		System.out.println("vertical"+vertical) ;
		System.out.println("site"+site) ;
		System.out.println("plant"+plant) ;
		
		if (vertical != null && !vertical.isBlank()) {
			objectName = bucketService.createObjectWithNestedPath(vertical, site, plant, fileName);
		}

		System.out.println("objectName"+objectName) ;

		GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucketName).object(objectName).build();
		GetObjectResponse response = client.getObject(getObjectArgs);
		String fileContentType = response.headers().get(HttpHeaders.CONTENT_TYPE);
		if (fileContentType == null) {
			fileContentType = "application/octet-stream"; // Default content type for unknown files
		}
		InputStream fileInputStream = new BufferedInputStream(response); // Buffered stream for efficiency
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		headers.add(HttpHeaders.CONTENT_TYPE, fileContentType); // Use content type from MinIO response

		return ResponseEntity.ok()
				.headers(headers)
				.body(new InputStreamResource(fileInputStream));
	}

}
