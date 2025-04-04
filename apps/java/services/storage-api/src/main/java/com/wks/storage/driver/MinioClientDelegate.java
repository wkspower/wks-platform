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
package com.wks.storage.driver;

import java.util.Map;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.PostPolicy;

public interface MinioClientDelegate {

	boolean bucketExists(BucketExistsArgs args);

	void makeBucket(MakeBucketArgs args);

	String getPresignedObjectUrl(GetPresignedObjectUrlArgs args);

	Map<String, String> getPresignedPostFormData(PostPolicy policy);

	GetObjectResponse getObject(GetObjectArgs args);

}
