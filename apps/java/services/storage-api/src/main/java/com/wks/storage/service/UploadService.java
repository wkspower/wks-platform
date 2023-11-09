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
package com.wks.storage.service;

import com.wks.storage.model.UploadFileUrl;

public interface UploadService {

	UploadFileUrl createPresignedPostFormData(String dir, String fileName, String contentType);

	UploadFileUrl createPresignedPostFormData(String fileName, String contentType);

}
