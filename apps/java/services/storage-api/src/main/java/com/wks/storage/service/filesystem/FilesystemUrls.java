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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.wks.storage.config.StorageConfig;

/**
 * Builds browser-reachable URLs that point at the filesystem storage endpoints
 * exposed by {@link FilesystemStorageController}. URLs are derived from the same
 * upload backend protocol/host/port used by the MinIO driver.
 */
public final class FilesystemUrls {

	static final String BASE_PATH = "/storage/filesystem";

	private FilesystemUrls() {
	}

	/**
	 * @param config     storage configuration providing protocol/host/port
	 * @param operation  endpoint operation ({@code uploads} or {@code downloads})
	 * @param bucketName tenant/bucket name
	 * @param objectName object name (possibly a {@code dir/fileName} path)
	 * @return absolute URL of the form
	 *         {@code <protocol>://<host><port>/storage/filesystem/<bucket>/<operation>?object=<object>}
	 */
	public static String objectUrl(StorageConfig config, String operation, String bucketName, String objectName) {
		String port = config.getUploadsPort() > 0 ? ":" + config.getUploadsPort() : "";

		return String.format("%s://%s%s%s/%s/%s?object=%s", config.getUploadsProtocol(), config.getUploadsBackendUrl(),
				port, BASE_PATH, encode(bucketName), operation, encode(objectName));
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

}
