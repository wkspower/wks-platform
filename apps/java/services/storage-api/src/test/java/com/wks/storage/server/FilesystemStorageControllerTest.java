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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.wks.storage.config.StorageConfig;

public class FilesystemStorageControllerTest {

	private FilesystemStorageController controller;

	@TempDir
	private Path basePath;

	@BeforeEach
	public void setup() {
		StorageConfig config = new StorageConfig();
		config.setFilesystemBasePath(basePath.toString());

		controller = new FilesystemStorageController();
		controller.setConfig(config);
	}

	@Test
	public void shouldWriteUploadedFileAndReadItBack() throws IOException {
		byte[] content = "hello-wks".getBytes(StandardCharsets.UTF_8);
		MultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", content);

		ResponseEntity<Void> uploadResponse = controller.upload("app", "dir/file.txt", file);
		assertEquals(HttpStatus.CREATED, uploadResponse.getStatusCode());
		assertTrue(Files.exists(basePath.resolve("app").resolve("dir").resolve("file.txt")));

		ResponseEntity<Resource> downloadResponse = controller.download("app", "dir/file.txt");
		assertEquals(HttpStatus.OK, downloadResponse.getStatusCode());

		try (InputStream in = downloadResponse.getBody().getInputStream()) {
			assertArrayEquals(content, in.readAllBytes());
		}
	}

	@Test
	public void shouldReturnNotFoundForMissingObject() throws IOException {
		ResponseEntity<Resource> downloadResponse = controller.download("app", "missing.txt");
		assertEquals(HttpStatus.NOT_FOUND, downloadResponse.getStatusCode());
	}

	@Test
	public void shouldRejectPathTraversal() {
		byte[] content = "x".getBytes(StandardCharsets.UTF_8);
		MultipartFile file = new MockMultipartFile("file", "evil.txt", "text/plain", content);

		assertThrows(IllegalArgumentException.class, () -> controller.upload("app", "../../evil.txt", file));
	}

}
