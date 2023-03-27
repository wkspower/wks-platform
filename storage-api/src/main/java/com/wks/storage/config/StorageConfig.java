package com.wks.storage.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wks.storage.driver.MinioClientDelegate;
import com.wks.storage.driver.MinioClientDelegateImpl;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class StorageConfig {
	
	@Value("${driver.storage.endpoint.url}")
	private String endpoint;
	
	@Value("${driver.storage.endpoint.port}")
	private int port;
	
	@Value("${driver.storage.endpoint.secure}")
	private boolean secure;
	
	@Value("${driver.storage.endpoint.signing.region}")
	private String signingRegion;
	
	@Value("${driver.storage.endpoint.bucket.prefix}")
	private String bucketPrefix;
	
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
	
	@Value("${driver.storage.uploads.backend.protocol}")
	private String uploadsProtocol;
		
	@Value("${driver.storage.uploads.backend.port}")
	private int uploadsPort;
	
	@Bean
	@Qualifier("DigitalOceanClient")
	public MinioClientDelegate creatMinioClient() {
		MinioClient client = MinioClient.builder()
										.endpoint(endpoint, 443, true)
										.credentials(accessKey, secretKey)
										.region(signingRegion)
										.build();
		return new MinioClientDelegateImpl(client);
	}
	
	@Bean
	@Qualifier("MinioClient")
	public MinioClientDelegate createClient() {
		MinioClient client = MinioClient.builder()
										.endpoint(endpoint, port, secure)
										.credentials(accessKey, secretKey)
										.build();
		return new MinioClientDelegateImpl(client);
	}

	public String getUploadsBackendUrl(String bucketName) {
		return String.format("%s/%s", uploadsBackendUrl, bucketName);
	}
	
}
