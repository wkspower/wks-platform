package com.wks.storage.service.minio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class MinioConfig {
	
	@Value("${driver.storage.endpoint.url}")
	private String endpoint;
	
	@Value("${driver.storage.endpoint.port}")
	private int port;
	
	@Value("${driver.storage.endpoint.secure}")
	private boolean secure;
	
	@Value("${driver.storage.accesskey}")
	private String accessKey;
	
	@Value("${driver.storage.secretkey}")
	private String secretKey;
	
	@Value("${driver.storage.uploads.backend.url}")
	private String uploadsBackendUrl;
	
	@Value("${driver.storage.uploads.file.min.size}")
	private int uploadsFileMinSize;
	
	@Value("${driver.storage.uploads.file.max.size}")
	private int uploadsFileMaxSize;
	
	@Bean
	public MinioClient getMinioClient() {
		return MinioClient.builder()
										.endpoint(endpoint, port, secure)
										.credentials(accessKey, secretKey)
										.build();
	}
	
	public String getUploadsBackendUrl() {
		return uploadsBackendUrl;
	}

	public String getUploadsBackendUrl(String bucketName) {
		return String.format("%s/%s", uploadsBackendUrl, bucketName);
	}
	
	public int getUploadsFileMinSize() {
		return uploadsFileMinSize;
	}
	
	public int getUploadsFileMaxSize() {
		return uploadsFileMaxSize;
	}

}
