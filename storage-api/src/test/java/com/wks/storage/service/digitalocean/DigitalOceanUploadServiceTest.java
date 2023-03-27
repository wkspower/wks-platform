package com.wks.storage.service.digitalocean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.storage.config.StorageConfig;
import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.model.UploadFileUrl;
import com.wks.storage.service.BucketService;

@ExtendWith(MockitoExtension.class)
public class DigitalOceanUploadServiceTest {

	@InjectMocks
	private DigitalOceanUploadService service;
	
	@Mock
	private BucketService bucketService;
	
	@Mock
	private MinioClientDelegate client;
	
	private StorageConfig configs;

	@BeforeEach
	public void setup() {
		configs = new StorageConfig();
		configs.setBucketPrefix("wks");
		configs.setPort(-1);
		configs.setUploadsProtocol("https");
		configs.setUploadsBackendUrl("localhost");
		service.setConfig(configs);
	}
	
	@Test
	public void shouldCreateSignedUrlWithoutDir() throws Exception {
		when(bucketService.createAssignedTenant()).thenReturn("wks-app");
		when(client.getPresignedPostFormData(isNotNull())).thenReturn(new HashMap<String, String>());
		
		UploadFileUrl upload = service.createPresignedPostFormData("file.csv", "application/csv");
		
		assertNotNull(upload);
		assertEquals("https://wks-app.localhost", upload.getUrl());
	}
	
	@Test
	public void shouldCreateSignedUrlWithtDir() throws Exception {
		when(bucketService.createAssignedTenant()).thenReturn("wks-app");
		when(bucketService.createObjectWithPath("dirpath", "file.csv")).thenReturn("dirpath/file.csv");
		when(client.getPresignedPostFormData(isNotNull())).thenReturn(new HashMap<String, String>());
		
		UploadFileUrl upload = service.createPresignedPostFormData("dirpath", "file.csv", "application/csv");
		
		assertNotNull(upload);
		assertEquals("https://wks-app.localhost", upload.getUrl());
	}
	
}
