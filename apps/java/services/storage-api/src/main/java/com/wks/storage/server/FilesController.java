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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.storage.model.DownloadFileUrl;
import com.wks.storage.model.UploadFileUrl;
import com.wks.storage.service.DownloadService;
import com.wks.storage.service.StorageServiceFactory;
import com.wks.storage.service.UploadService;

@RestController
@RequestMapping
public class FilesController {

	@Autowired
	private StorageServiceFactory factory;

	@GetMapping(value = "/storage/files/{dir}/downloads/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DownloadFileUrl> downloadFileWithDir(@PathVariable(required = true) String dir,
			@PathVariable(required = true) String fileName,
			@RequestParam(name = "content-type", required = true) String contentType) {
		return ResponseEntity.ok(downloadService().createPresignedObjectUrl(dir, fileName, contentType));
	}

	@GetMapping(value = "/storage/files1/{dir}/downloads/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<?> downloadFileWithDir1(@PathVariable(required = true) String dir,
			@PathVariable(required = true) String fileName,
			@RequestParam(name = "content-type", required = true) String contentType) {
		System.out.println("ratnesh controller line 46");
		return downloadService().downloadObj(dir, fileName, contentType);
	}

	@GetMapping(value = "/storage/files/{vertical}/{site}/{plant}/downloads/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<?> downloadFileWithNestedStructure(@PathVariable(required = true) String vertical,
	@PathVariable(required = true) String site,@PathVariable(required = true) String plant, @PathVariable(required = true) String fileName,
			@RequestParam(name = "content-type", required = true) String contentType) {
		System.out.println("ratnesh controller line 46");
		System.out.println("1") ;
		System.out.println("vertical"+vertical) ;
		System.out.println("site"+site) ;
		System.out.println("plant"+plant) ;
		
		
		return downloadService().downloadFile(vertical, site, plant, fileName, contentType);
	}

	@GetMapping(value = "/storage/files/downloads/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DownloadFileUrl> downloadFile(@PathVariable(required = true) String fileName,
			@RequestParam(name = "content-type", required = true) String contentType) {
		return ResponseEntity.ok(downloadService().createPresignedObjectUrl(fileName, contentType));
	}

	@GetMapping(value = "/storage/files/{dir}/uploads/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UploadFileUrl> uploadWithDir(@PathVariable(required = true) String dir,
			@PathVariable(required = true) String fileName,
			@RequestParam(name = "content-type", required = true) String contentType) {
		return ResponseEntity.ok(uploadService().createPresignedPostFormData(dir, fileName, contentType));
	}

	@GetMapping(value = "/storage/files/uploads/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UploadFileUrl> upload(@PathVariable(required = true) String fileName,
			@RequestParam(name = "content-type", required = true) String contentType) {
		return ResponseEntity.ok(uploadService().createPresignedPostFormData(fileName, contentType));
	}

	private DownloadService downloadService() {
		return factory.getFactory().getDownloadService();
	}

	private UploadService uploadService() {
		return factory.getFactory().getUploadService();
	}

	public void setFactory(StorageServiceFactory factory) {
		this.factory = factory;
	}

}
