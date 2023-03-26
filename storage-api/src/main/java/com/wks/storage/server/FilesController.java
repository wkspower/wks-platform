package com.wks.storage.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
	public DownloadFileUrl downloadFileWithDir(@PathVariable String dir, @PathVariable String fileName, @RequestParam(name="content-type", required=true) String contentType) throws Exception {
		return downloadService().createPresignedObjectUrl(dir, fileName, contentType);
	}
	
	@GetMapping(value = "/storage/files/downloads/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public DownloadFileUrl downloadFile(@PathVariable String fileName, @RequestParam(name="content-type", required=true) String contentType) throws Exception {
		return downloadService().createPresignedObjectUrl(fileName, contentType);
	}

	@GetMapping(value = "/storage/files/{dir}/uploads/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public UploadFileUrl uploadWithDir(@PathVariable String dir, @PathVariable String fileName, @RequestParam(name="content-type", required=true) String contentType) throws Exception {
		return uploadService().createPresignedPostFormData(dir, fileName, contentType);
	}

	@GetMapping(value = "/storage/files/uploads/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public UploadFileUrl upload(@PathVariable String fileName, @RequestParam(name="content-type",required=true) String contentType) throws Exception {
		return uploadService().createPresignedPostFormData( fileName, contentType);
	}
	
	private DownloadService downloadService() {
		return factory.getFactory().getDownloadService();
	}

	private UploadService uploadService() {
		return factory.getFactory().getUploadService();
	}
	
}
