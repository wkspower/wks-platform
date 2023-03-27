package com.wks.storage.service.minio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.model.DownloadFileUrl;
import com.wks.storage.service.BucketService;

@ExtendWith(MockitoExtension.class)
public class MinioDownloadServiceTest {

	@InjectMocks
	private MinioDownloadService service;
	
	@Mock
	private BucketService bucketService;
	
	@Mock
	private MinioClientDelegate client;
	
	@Test
	public void shouldCreateSignedUrlWithoutDir() throws Exception {
		when(bucketService.createAssignedTenant()).thenReturn("app");
		when(client.getPresignedObjectUrl(isNotNull())).thenReturn("http://localhost");
		
		DownloadFileUrl download = service.createPresignedObjectUrl("file.csv", "application/csv");
		
		assertNotNull(download);
		assertEquals("http://localhost", download.getUrl());
	}
	
	@Test
	public void shouldCreateSignedUrlWithtDir() throws Exception {
		when(bucketService.createAssignedTenant()).thenReturn("app");
		when(bucketService.createObjectWithPath("dirpath", "file.csv")).thenReturn("dirpath/file.csv");
		when(client.getPresignedObjectUrl(isNotNull())).thenReturn("http://localhost/dirpath/file.csv");
		
		DownloadFileUrl download = service.createPresignedObjectUrl("dirpath", "file.csv", "application/csv");
		
		assertNotNull(download);
		assertEquals("http://localhost/dirpath/file.csv", download.getUrl());
	}
	
}
