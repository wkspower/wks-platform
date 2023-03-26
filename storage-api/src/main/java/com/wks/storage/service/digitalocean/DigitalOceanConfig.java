package com.wks.storage.service.digitalocean;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class DigitalOceanConfig {
	
	@Value("${driver.storage.endpoint.url}")
	private String endpoint;
	
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
	
	@Bean("DigitalOceanClient")
	public AmazonS3 getDigitalOceanClient()  {
			AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
	        AmazonS3 s3client = AmazonS3ClientBuilder
									                .standard()
									                .withCredentials(new AWSStaticCredentialsProvider(credentials))
									                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, signingRegion))
									                .build();
	        return s3client;
	}
	
	@Bean("DigitalOceanPresignerClient")
	public S3Presigner getDigitalOceanPresignerClient() throws Exception  {
	    AwsCredentials creds =  AwsBasicCredentials.create(accessKey, secretKey);
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(creds);
		return S3Presigner.builder()
									        .region(Region.US_EAST_1)
									        .endpointOverride(new URI("https://".concat(endpoint)))
									        .credentialsProvider(provider)
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
	
	public String getBucketPrefix() {
		return bucketPrefix;
	}

}
