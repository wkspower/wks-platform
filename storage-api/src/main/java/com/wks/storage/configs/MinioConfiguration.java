package com.wks.storage.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import io.minio.MinioClient;

@Configuration
public class MinioConfiguration {
	
	@Value("${minio.endpoint.url}")
	private String endpoint;
	
	@Value("${minio.endpoint.port}")
	private int port;
	
	@Value("${minio.endpoint.secure}")
	private boolean secure;
	
	@Value("${minio.credencials.accesskey}")
	private String accessKey;
	
	@Value("${minio.credencials.secretkey}")
	private String secretKey;
	
	@Value("${uploads.backend.url}")
	private String uploadsBackendUrl;
	
	@Value("${uploads.file.min.size}")
	private int uploadsFileMinSize;
	
	@Value("${uploads.file.max.size}")
	private int uploadsFileMaxSize;
	
	@Bean
	public MinioClient getMinioClient() {
		return MinioClient.builder()
										.endpoint(endpoint, port, secure)
										.credentials(accessKey, secretKey)
										.build();
	}
	
	@Bean
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		return restTemplate;
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
