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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper that resolves and validates filesystem locations for the filesystem
 * storage driver, guarding against path traversal so resolved paths always stay
 * under the configured base path.
 */
public final class FilesystemPaths {

	private FilesystemPaths() {
	}

	/**
	 * Resolves {@code <basePath>/<bucket>/<object>} ensuring the normalized result
	 * never escapes the base path.
	 *
	 * @param basePath configured storage base path
	 * @param bucket   tenant/bucket name
	 * @param object   object name (may contain a {@code dir/fileName} path)
	 * @return the resolved, normalized absolute path
	 */
	public static Path resolve(String basePath, String bucket, String object) {
		Path base = Paths.get(basePath).toAbsolutePath().normalize();
		Path resolved = base.resolve(bucket).resolve(object).normalize();

		if (!resolved.startsWith(base)) {
			throw new IllegalArgumentException("Resolved path escapes storage base path");
		}

		return resolved;
	}

}
