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
package com.wks.storage.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.storage.config.StorageConfig;
import com.wks.storage.service.filesystem.FilesystemPaths;

/**
 * Serves and accepts file bytes for the filesystem storage driver. Since the
 * filesystem driver cannot presign URLs (unlike MinIO/DigitalOcean), storage-api
 * handles the upload/download transfer itself, writing to and reading from
 * {@code <basePath>/<bucket>/<object>}.
 */
@RestController
public class FilesystemStorageController {

	@Autowired
	private StorageConfig config;

	@PostMapping(value = "/storage/filesystem/{bucket}/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> upload(@PathVariable String bucket,
			@RequestParam(name = "object") String object,
			@RequestParam(name = "file") MultipartFile file) throws IOException {
		return store(bucket, object, file);
	}

	@PutMapping(value = "/storage/filesystem/{bucket}/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> uploadPut(@PathVariable String bucket,
			@RequestParam(name = "object") String object,
			@RequestParam(name = "file") MultipartFile file) throws IOException {
		return store(bucket, object, file);
	}

	@GetMapping(value = "/storage/filesystem/{bucket}/downloads")
	public ResponseEntity<Resource> download(@PathVariable String bucket,
			@RequestParam(name = "object") String object) throws IOException {
		Path target = FilesystemPaths.resolve(config.getFilesystemBasePath(), bucket, object);

		if (!Files.exists(target) || Files.isDirectory(target)) {
			return ResponseEntity.notFound().build();
		}

		String contentType = Files.probeContentType(target);
		MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType)
				: MediaType.APPLICATION_OCTET_STREAM;

		InputStream in = Files.newInputStream(target);
		Resource resource = new InputStreamResource(in);

		return ResponseEntity.ok()
				.contentType(mediaType)
				.contentLength(Files.size(target))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + target.getFileName().toString() + "\"")
				.body(resource);
	}

	private ResponseEntity<Void> store(String bucket, String object, MultipartFile file) throws IOException {
		Path target = FilesystemPaths.resolve(config.getFilesystemBasePath(), bucket, object);

		Files.createDirectories(target.getParent());

		try (InputStream in = file.getInputStream()) {
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		}

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	public void setConfig(StorageConfig config) {
		this.config = config;
	}

}
