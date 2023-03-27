package com.wks.storage.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.storage.mocks.MockStorageServiceFactory;
import com.wks.storage.model.DownloadFileUrl;
import com.wks.storage.model.UploadFileUrl;
import com.wks.storage.service.DownloadService;
import com.wks.storage.service.UploadService;

@ExtendWith(MockitoExtension.class)
public class FilesControllerTest {
	
	@InjectMocks
	private FilesController controller;
	
	private MockStorageServiceFactory factory;
	
	@Mock
	private DownloadService downloadService;

	@Mock
	private UploadService uploadService;
	
	@BeforeEach
	public void setup() {
		factory = new MockStorageServiceFactory();
		factory.setDownloadService(downloadService);
		factory.setUploadService(uploadService);
		controller.setFactory(factory);
	}

	@Test
	public void shouldCreateDownloadUrlWithoutDirPath() throws Exception {
		when(downloadService.createPresignedObjectUrl("file", "application/json")).thenReturn(new DownloadFileUrl());
		
		DownloadFileUrl description = controller.downloadFile("file", "application/json");
		
		assertNotNull(description);
	}
	
	@Test
	public void shouldCreateDownloadUrlWithtDirPath() throws Exception {
		when(downloadService.createPresignedObjectUrl("dir", "file", "application/json")).thenReturn(new DownloadFileUrl());
		
		DownloadFileUrl description = controller.downloadFileWithDir("dir", "file", "application/json");
		
		assertNotNull(description);
	}
	
	@Test
	public void shouldCreateUploadUrl() throws Exception {
		when(uploadService.createPresignedPostFormData("file", "application/json")).thenReturn(new UploadFileUrl());
		
		UploadFileUrl description = controller.upload("file", "application/json");
		
		assertNotNull(description);
	}
	
	@Test
	public void shouldCreateUploadUrlWithDirPath() throws Exception {
		when(uploadService.createPresignedPostFormData("dir", "file", "application/json")).thenReturn(new UploadFileUrl());
		
		UploadFileUrl description = controller.uploadWithDir("dir", "file", "application/json");
		
		assertNotNull(description);
	}

}
